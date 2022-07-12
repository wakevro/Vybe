package com.example.richard.vybe.Overview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.richard.vybe.Model.EndPoints;
import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.SpotifyConnector;
import com.example.richard.vybe.Swipe.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OverviewFragment extends Fragment {

    private String TAG = "OverviewFragment";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private SpotifyConnector spotifyConnector;
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;
    private static final String ENDPOINT = EndPoints.PLAYLISTCREATE.toString();
    private String playlistID;

    private ArrayList<Song> songs = new ArrayList<>();
    private DatabaseReference mDatabase;
    private DatabaseReference databaseReference;
    private Button btnStartPlaylist;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_overview, container, false);


        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new SongAdapter(songs, getContext());
        recyclerView.setAdapter(mAdapter);


        btnStartPlaylist = rootView.findViewById(R.id.btnStartPlaylist);
        btnStartPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getContext())
                        .inflate(
                                R.layout.layout_bottom_sheet,
                                (ConstraintLayout) rootView.findViewById(R.id.clBottomSheet)

                        );
                bottomSheetView.findViewById(R.id.btnCreatePlaylist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText etPlaylist = bottomSheetView.findViewById(R.id.etPlaylistName);
                        String userPlaylistName = etPlaylist.getText().toString();
                        if (userPlaylistName.isEmpty()) {
                            userPlaylistName = "Vybe";
                        }
                        startCreatePlaylist(userPlaylistName);
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(getContext());


        return rootView;
    }


    private void startCreatePlaylist(String playlistName) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);

        String userID = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("userid", "")).toString();
        userID = userID.replaceFirst("https://vybe-6c7cb-default-rtdb.firebaseio.com/", "");
        createPlaylist(userID, playlistName);


        // Switch to HomeFragment

        deleteTracks();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("page_number", 0);
        startActivityForResult(intent, 0);

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);
        mDatabase = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Tracks");
        spotifyConnector = new SpotifyConnector(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        songs.clear();
        getSwipedTracks();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getView() != null) {
            if (isVisibleToUser) {

                songs.clear();
                getSwipedTracks();
            } else {
            }
        }
    }


    private void getSwipedTracks() {

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collectTracks((Map<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void collectTracks(Map<String, Object> tracks) {
        for (Map.Entry<String, Object> entry : tracks.entrySet()) {
            Map value = (Map) entry.getValue();
            Song song = new Song(entry.getKey(), value.get("name").toString());
            song.setLiked((Boolean) value.get("liked"));
            song.setImageURL(value.get("img").toString());
            song.setTimestamp((Long) value.get("time"));
            song.setArtist((String) value.get("artist"));
            if (!song.getLiked()) {
                Object object = (HashMap) value.get("playlist");
                song.setPlaylist(new Playlist(song.getId(), song.getName()));
            }
            songs.add(song);

            Collections.sort(songs, (o1, o2) -> (int) (o2.getTimestamp() - o1.getTimestamp()));

            mAdapter.notifyDataSetChanged();
        }
    }

    public void createPlaylist(String userID, String playlistName){
        JSONObject jsonBodyObj = new JSONObject();
        try{
            jsonBodyObj.put("name", playlistName);
            jsonBodyObj.put("description", "Created with Vybe.");
            jsonBodyObj.put("public", "true");
            jsonBodyObj.put("collaborative", "false");
        }catch (JSONException e){
            e.printStackTrace();
        }

        final String requestBody = jsonBodyObj.toString();
        String CREATE_PLAYLIST = String.format(ENDPOINT, userID);
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.POST, CREATE_PLAYLIST, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            playlistID = response.getString("id");
                            songs.forEach((song) -> {
                                if (song.getLiked()) {
                                    Log.i(TAG, "ADDED SONG");
                                    spotifyConnector.addSongToDislikedPlaylist(song, playlistID);
                                }
                            });

                        } catch (JSONException e) {
                            Log.e(TAG, "Error whilst parsing JSONObject");
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error whilst creating playlist");
                        error.printStackTrace();

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }

        };
        queue.add(jsonRequest);
    }

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
}
