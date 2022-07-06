package com.example.richard.vybe.Home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.PlaylistService;
import com.example.richard.vybe.SpotifyConnect.UserService;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private String TAG = "HomeFragment";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RequestQueue requestQueue;
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private PlaylistService playlistService;

    String userProfileImageURL;
    private User user;
    private TextView tvUsername;
    private ImageView ivProfileImage;




    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, container, false);

        sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(getContext());
        playlistService = new PlaylistService(requestQueue, sharedPreferences);

        recyclerView = rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        tvUsername = rootView.findViewById(R.id.tvUsername);
        ivProfileImage = rootView.findViewById(R.id.ivProfileHome);



        UserService userService = new UserService(requestQueue, sharedPreferences);
        Log.i(TAG, "GOTTEN USER SERVICE.");
        userService.get(() -> {
            Log.i(TAG, "STARTING TO GET USER.");
            User user = userService.getUser();
            Log.i(TAG, "USER NAME: " + user.getDisplay_name());

            Log.i(TAG, "IMAGE: " + user.getProfileImageURL());

            String userName = user.getDisplay_name().split(" ")[0];
            userProfileImageURL = user.getProfileImageURL();
            tvUsername.setText(userName);

            if (userProfileImageURL == null) {
                Glide.with(getContext())
                        .load(R.drawable.man)
                        .transform(new CircleCrop())
                        .into(ivProfileImage);
            } else {
                Glide.with(getContext())
                        .load(userProfileImageURL)
                        .transform(new CircleCrop())
                        .into(ivProfileImage);
            }


        });
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        getPlaylists();

    }

    public void getPlaylists() {
        playlists.clear();
        playlistService.get(() -> grabPlaylist());
    }

    public void grabPlaylist() {
        playlists = playlistService.getPlaylists();
        mAdapter = new PlaylistAdapter(playlists, getContext());
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }
}
