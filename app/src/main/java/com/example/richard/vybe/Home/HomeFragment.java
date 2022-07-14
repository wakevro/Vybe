package com.example.richard.vybe.Home;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.example.richard.vybe.Profile.ProfileActivity;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.PlaylistService;
import com.example.richard.vybe.SpotifyConnect.UserService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();


        sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(getContext());
        playlistService = new PlaylistService(requestQueue, sharedPreferences);

        recyclerView = rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        tvUsername = rootView.findViewById(R.id.tvUsername);
        ivProfileImage = rootView.findViewById(R.id.ivProfileHome);

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                startActivity(profileIntent);
            }
        });



        UserService userService = new UserService(requestQueue, sharedPreferences);
        Log.i(TAG, "GOTTEN USER SERVICE.");
        try {
            userService.get(() -> {
                Log.i(TAG, "STARTING TO GET USER.");
                User user = userService.getUser();
                Log.i(TAG, "USER NAME: " + user.getDisplay_name());

                Log.i(TAG, "IMAGE: " + user.getProfileImageURL());

                String userName = user.getDisplay_name().split(" ")[0];
                userProfileImageURL = user.getProfileImageURL();
                tvUsername.setText(userName);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", ""));
                databaseReference.child("id").setValue(user.id);
                databaseReference.child("name").setValue(user.display_name);
                databaseReference.child("country").setValue(user.country);
                databaseReference.child("status").setValue("offline");
                databaseReference.child("search").setValue(user.display_name.toLowerCase());

                if (userProfileImageURL == null) {
                    databaseReference.child("profileImage").setValue("");
                    Glide.with(getContext())
                            .load(R.drawable.man)
                            .transform(new CircleCrop())
                            .into(ivProfileImage);
                } else {
                    databaseReference.child("profileImage").setValue(user.profileImageURL);
                    Glide.with(getContext())
                            .load(userProfileImageURL)
                            .transform(new CircleCrop())
                            .into(ivProfileImage);
                }

                progressDialog.dismiss();
            });
        } catch (Exception e) {
            Log.i(TAG, "Invalid user");
        }


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        getPlaylists();


        progressDialog.dismiss();

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
