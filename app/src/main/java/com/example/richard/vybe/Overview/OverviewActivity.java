package com.example.richard.vybe.Overview;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.Model.Song;
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

public class OverviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Song> songs = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        mDatabase = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("userid", "")).child("Tracks");

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        getSwipedTracks();
        mAdapter = new SongAdapter(songs, this);
        recyclerView.setAdapter(mAdapter);
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
            if (!song.getLiked()) {
                Object object = (HashMap) value.get("playlist");
                song.setPlaylist(new Playlist(((HashMap) object).get("id").toString(), ((HashMap) object).get("name").toString()));
            }
            songs.add(song);

            Collections.sort(songs, (o1, o2) -> (int) (o2.getTimestamp() - o1.getTimestamp()));

            mAdapter.notifyDataSetChanged();
        }
    }
}
