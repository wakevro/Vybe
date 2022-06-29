package com.example.richard.vybe.Swipe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.Overview.SongAdapter;
import com.example.richard.vybe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OverviewFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Song> songs = new ArrayList<>();
    private DatabaseReference mDatabase;

    private String TAG = "OverviewFragment";


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_overview, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        getSwipedTracks();
        mAdapter = new SongAdapter(songs, getContext());
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);
        mDatabase = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("userid", "")).child("Tracks");
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
        Log.i(TAG, "START COLLECT TRACKS");
        for (Map.Entry<String, Object> entry : tracks.entrySet()) {
            Log.i(TAG, "PASSED FIRST HERE");
            Map value = (Map) entry.getValue();
            Song song = new Song(entry.getKey(), value.get("name").toString());
            song.setLiked((Boolean) value.get("liked"));
            song.setImageURL(value.get("img").toString());
            song.setTimestamp((Long) value.get("time"));
            Log.i(TAG, "PASSED SECOND HERE");
            if (!song.getLiked()) {
                Object object = (HashMap) value.get("playlist");
                Log.i(TAG, "PASSED THIRD HERE");
                song.setPlaylist(new Playlist(song.getId(), song.getName()));
                Log.i(TAG, "PASSED FOURTH HERE");
            }
            Log.i(TAG, "ADD SONG");
            songs.add(song);

            Log.i(TAG, "START COLLECTIONS");
            Collections.sort(songs, (o1, o2) -> (int) (o2.getTimestamp() - o1.getTimestamp()));

            mAdapter.notifyDataSetChanged();
        }
        Log.i(TAG, "END COLLECT TRACKS");
    }
}
