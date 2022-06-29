package com.example.richard.vybe.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.PlaylistService;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RequestQueue requestQueue;
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private PlaylistService playlistService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_settings);
        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(this);
        playlistService = new PlaylistService(requestQueue, sharedPreferences);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        getPlaylists();
    }


    public void getPlaylists() {
        playlistService.get(() -> grabPlaylist());
    }

    public void grabPlaylist() {
        playlists = playlistService.getPlaylists();
        mAdapter = new PlaylistAdapter(playlists, this);
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }
}
