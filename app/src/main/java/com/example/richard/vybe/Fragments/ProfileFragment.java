package com.example.richard.vybe.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private String TAG = "ProfileFragment";


    private ImageView image_profile;
    private TextView username;

    String currentUser;
    SharedPreferences sharedPreferences;
    DatabaseReference databaseReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);

        sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String getUser = dataSnapshot.getKey();
                User user = new User();

                String id = dataSnapshot.child("id").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String profileImageURL = dataSnapshot.child("profileImage").getValue().toString();

                user.setDisplay_name(name);
                user.setId(id);
                user.setProfileImageURL(profileImageURL);

                username.setText(user.getDisplay_name());

                String userProfileImageURL = user.getProfileImageURL();

                if (getActivity() == null) {
                    return;
                }

                if (userProfileImageURL.equals("")) {
                    Glide.with(getContext())
                            .load(R.drawable.man)
                            .transform(new CircleCrop())
                            .into(image_profile);
                } else {
                    Glide.with(getContext())
                            .load(userProfileImageURL)
                            .transform(new CircleCrop())
                            .into(image_profile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        return view;
    }
}