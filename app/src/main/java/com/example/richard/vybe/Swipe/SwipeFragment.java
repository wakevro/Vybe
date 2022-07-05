package com.example.richard.vybe.Swipe;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.SpotifyConnector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        Log.i(TAG, "SWIPE FRAGMENT STARTED");
        rootView = inflater.inflate(R.layout.fragment_swipe, container, false);

        sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);


        reloadBtn = rootView.findViewById(R.id.reload);
        settingsBtn = rootView.findViewById(R.id.settings);
        progressBar = rootView.findViewById(R.id.pbSwipeProgress);

        songDB = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Tracks");
        sentimentDB = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Sentiment");

        spotifyConnector = new SpotifyConnector(getContext());

        Log.i(TAG, "GOT SONG DB");

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

        Log.i(TAG, "On start getting tracks...");
        getTracks();
        Log.i(TAG, "On start gotten tracks...");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "On resume getting tracks...");
        getTracks();
        Log.i(TAG, "On resume gotten tracks...");
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
        Log.i(TAG, "START SWIPE CARDS SETUP");
        SwipeFlingAdapterView flingAdapterView = (SwipeFlingAdapterView) rootView.findViewById(R.id.frame);
        arrayAdapter = new arrayAdapter(getContext(), R.layout.item, rowItems);
        flingAdapterView.setAdapter(arrayAdapter);
        flingAdapterView.setFlingListener(flingListener);
        flingAdapterView.setOnItemClickListener(clickListener);
        Log.i(TAG, "END SWIPE CARD SETUP");

    }


    private void getTracks() {
        HashSet<Song> recentlyPlayedTracks = spotifyConnector.getRecentlyPlayedTracks();


        Log.i(TAG, "GETTING DERIVED SENTIMENT");
        sentimentDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    derivedSentiment = Double.parseDouble(snapshot.getValue().toString());
                    Log.i(TAG, "DERIVED SENTIMENT: " + derivedSentiment);
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



        // TODO : add progress bar as user swipes to see progress of swiped tracks

        @Override
        public void onLeftCardExit(Object o) {
//            Toast.makeText(getContext(), "Disliked", Toast.LENGTH_SHORT).show();
            Song song = (Song) o;
            saveSongToFirebase(song, false);
            spotifyConnector.removeSongFromLibrary(song);
            autoStopSong();
            updateProgressBar(song);

        }

        @Override
        public void onRightCardExit(Object o) {
//            Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
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

        }

    };



    private void deleteTracks() {
    }

    public void getAudioFeature(Song song) {


    }

    private boolean analyzeSong(double mood, double danceability, double energy, double valence) {


        return false;
    }

    private void autoPlaySong(Song song) {

    }

    private void autoStopSong() {

    }

    private void updateProgressBar(Song song) {

    }

}
