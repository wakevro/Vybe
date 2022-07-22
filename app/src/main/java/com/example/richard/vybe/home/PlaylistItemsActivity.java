package com.example.richard.vybe.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.richard.vybe.home.adapter.PlaylistItemsAdapter;
import com.example.richard.vybe.model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.spotifyConnect.PlaylistService;

import java.util.ArrayList;

public class PlaylistItemsActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RequestQueue requestQueue;
    private ArrayList<Song> songs = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private PlaylistService playlistService;

    public TextView tvPlaylistTitle;
    public Button btnPlaylistBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_items);

        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);

        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(this);
        playlistService = new PlaylistService(requestQueue, sharedPreferences);

        recyclerView = findViewById(R.id.playlist_recycler);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        String playlistTitle = getIntent().getStringExtra("playlistTitle");

        tvPlaylistTitle = this.findViewById(R.id.tvPlaylistTitle);
        btnPlaylistBack = this.findViewById(R.id.btnPlaylistBack);

        tvPlaylistTitle.setText(playlistTitle);
        btnPlaylistBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSongs();
    }

    private void getSongs() {
        songs.clear();
        playlistService.getSongs(() -> grabPlaylistSongs());
    }

    public void grabPlaylistSongs() {
        songs = playlistService.getPlaylistsSongs();
        mAdapter = new PlaylistItemsAdapter(songs, this);
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }
}