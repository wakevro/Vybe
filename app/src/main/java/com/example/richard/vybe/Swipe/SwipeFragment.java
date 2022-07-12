package com.example.richard.vybe.Swipe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.richard.vybe.Model.EndPoints;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.SpotifyConnector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class SwipeFragment extends Fragment {

    private String TAG = "SwipeFragment";

    private List<Song> rowItems;
    private arrayAdapter arrayAdapter;
    private DatabaseReference songDB;
    private DatabaseReference sentimentDB;

    private SpotifyConnector spotifyConnector;
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;

    private RequestQueue queue;

    private double derivedSentiment;
    private boolean startedPlaying = false;
    private int currentProgress = 0;
    private int progressMax = 0;
    ImageButton reloadBtn;
    ImageButton settingsBtn;
    private ProgressBar progressBar;

    MediaPlayer mediaPlayer;
    private View rootView;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_swipe, container, false);

        sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);


        reloadBtn = rootView.findViewById(R.id.reload);
        settingsBtn = rootView.findViewById(R.id.settings);
        progressBar = rootView.findViewById(R.id.pbSwipeProgress);

        reloadBtn.setOnClickListener(reloadListener);
        settingsBtn.setOnClickListener(settingsListener);

        songDB = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Tracks");
        sentimentDB = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Sentiment");

        spotifyConnector = new SpotifyConnector(getContext());


        rowItems = new ArrayList<>();
        setUpSwipeCards();


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public void onStart() {
        super.onStart();

        getTracks();
    }

    @Override
    public void onResume() {
        super.onResume();
        getTracks();
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(spotifyConnector.mSpotifyAppRemote);
        autoStopSong();

    }

    @Override
    public void onPause() {
        super.onPause();
        autoStopSong();
    }



    private void setUpSwipeCards() {
        SwipeFlingAdapterView flingAdapterView = (SwipeFlingAdapterView) rootView.findViewById(R.id.frame);
        arrayAdapter = new arrayAdapter(getContext(), R.layout.item, rowItems);
        flingAdapterView.setAdapter(arrayAdapter);
        flingAdapterView.setFlingListener(flingListener);
        flingAdapterView.setOnItemClickListener(clickListener);

    }


    private void getTracks() {
        HashSet<Song> recentlyPlayedTracks = spotifyConnector.getRecentlyPlayedTracks();


        sentimentDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    derivedSentiment = Double.parseDouble(snapshot.getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        songDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (Song song : recentlyPlayedTracks) {
                    if (!dataSnapshot.hasChild(song.getId())) {
                        getAudioFeature(song);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void saveSongToFirebase(Song song, Boolean liked) {
        Long tsLong = System.currentTimeMillis() / 1000;
        String id = song.getId();


        songDB.child(id).child("id").setValue(song.getId());
        songDB.child(id).child("name").setValue(song.getName());
        songDB.child(id).child("artist").setValue(song.getArtist());
        songDB.child(id).child("img").setValue(song.getImageURL());
        songDB.child(id).child("time").setValue(tsLong);
        if (liked) {
            songDB.child(id).child("liked").setValue(true);
        } else {
            songDB.child(id).child("liked").setValue(false);
            songDB.child(id).child("playlist").child("id").setValue(sharedPreferences.getString("playlist", null));
            songDB.child(id).child("playlist").child("name").setValue(sharedPreferences.getString("playlistname", null));
        }
    }

    private SwipeFlingAdapterView.onFlingListener flingListener = new SwipeFlingAdapterView.onFlingListener() {
        @Override
        public void removeFirstObjectInAdapter() {
            Log.d(TAG, "removed object!");
            rowItems.remove(0);
            arrayAdapter.notifyDataSetChanged();
        }




        @Override
        public void onLeftCardExit(Object o) {
            Song song = (Song) o;
            saveSongToFirebase(song, false);
            spotifyConnector.removeSongFromLibrary(song);
            autoStopSong();
            updateProgressBar(song);

        }

        @Override
        public void onRightCardExit(Object o) {
            Song song = (Song) o;
            saveSongToFirebase(song, true);
            spotifyConnector.saveSongToLibrary(song);
            autoStopSong();
            updateProgressBar(song);
        }

        @Override
        public void onAdapterAboutToEmpty(int i) {
        }

        @Override
        public void onScroll(float v) {

        }
    };



    private SwipeFlingAdapterView.OnItemClickListener clickListener = new SwipeFlingAdapterView.OnItemClickListener() {

        @Override
        public void onItemClicked(int i, Object o) {
            Song song = (Song) o;
            autoPlaySong(song);
        }

    };


    private View.OnClickListener settingsListener = v -> {

    };

    private View.OnClickListener reloadListener = v -> {

        deleteTracks();
        getTracks();
        progressBar.setProgress(0);
        currentProgress = 0;
    };

    private void deleteTracks() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", ""));
        databaseReference.child("Tracks").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                }
                else {
                    Toast.makeText(getActivity(), "Failed to delete!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getAudioFeature(Song song) {
        queue = Volley.newRequestQueue(getContext());
        String endpoint = EndPoints.AUDIOFEATURES.toString();
        endpoint = String.format(endpoint, song.getId());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    try {
                        double danceability = response.getDouble("danceability");
                        double energy = response.getDouble("energy");
                        double valence = response.getDouble("valence");
                        String id = response.getString("id");
                        if (!rowItems.contains(song)) {
                            if (analyzeSong(derivedSentiment, danceability, energy, valence)) {
                                rowItems.add(song);
                                arrayAdapter.notifyDataSetChanged();
                            }
                            progressMax = rowItems.size();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // TODO: Handle error
                    Log.i(TAG, "ERROR GETTING AUDIO FEATURES..." + error.toString());
                }) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);

    }

    private boolean analyzeSong(double mood, double danceability, double energy, double valence) {
        if (mood < 0.10) {
            if (valence <= (mood + 0.1) && danceability <= (mood * 5) && energy <= (mood * 2.5)) {
                return true;
            }
        }
        else if (mood >= 0.10 && mood < 0.25) {
            if (valence <= (mood + 0.15) && danceability <= (mood * 2.5) && energy <= (mood * 2.5)) {
                return true;
            }
        }
        else if (mood >= 0.25 && mood < 0.50) {
            if (valence >= (0.10) && valence <= (mood) && danceability <= (mood * 2) && energy >= (mood * 0.75) && energy <= (mood * 1.75)) {
                return true;
            }
        }
        else if (mood >= 0.50 && mood < 0.75) {
            if (valence >= (0.2) && danceability >= (mood / 1.4) && energy >= (mood / 2)) {
                return true;
            }
        }
        else if (mood >= 0.75 && mood < 0.90) {
            if (valence >= (0.2) && danceability >= (mood /1.30) && energy >= (mood / 1.75)) {
                return true;
            }
        }
        else if (mood >= 0.90) {
            if (valence >= (0.3) && danceability >= (mood / 1.50) && energy >= (mood * 1.5)) {
                return true;
            }
        }
        return false;
    }

    private void autoPlaySong(Song song) {

        startedPlaying = true;
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        String songPreview = song.getPreviewURL();
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(songPreview);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    private void autoStopSong() {
        if (startedPlaying) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    private void updateProgressBar(Song song) {
        if (currentProgress == progressMax - 1) {
            progressBar.setProgress(0);
            currentProgress = 0;
            Intent intent = new Intent(getActivity().getBaseContext(), MainActivity.class);
            intent.putExtra("page_number", 3);
            getActivity().startActivityForResult(intent, 0);
        }
        else{
            currentProgress += 1;
            progressBar.setProgress(currentProgress);
            progressBar.setMax(progressMax);
        }

    }


}
