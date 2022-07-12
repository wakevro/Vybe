package com.example.richard.vybe.Profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.Fragments.ChatsFragment;
import com.example.richard.vybe.Fragments.ProfileFragment;
import com.example.richard.vybe.Fragments.UsersFragment;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private String TAG = "ProfileActivity";


    private DatabaseReference reference;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    private String name;
    private String userProfileImageURL;

    private ImageView profile_image;
    private TextView tvUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.username);

        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        requestQueue = Volley.newRequestQueue(this);

        reference = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", ""));

        reference.addValueEventListener(new ValueEventListener() {
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

                tvUsername.setText(user.getDisplay_name());
                userProfileImageURL = user.getProfileImageURL();

                if (getBaseContext() == null) {
                    return;
                }

                if (userProfileImageURL == null) {
                    Glide.with(getBaseContext())
                            .load(R.drawable.man)
                            .transform(new CircleCrop())
                            .into(profile_image);
                } else {
                    Glide.with(getBaseContext())
                            .load(userProfileImageURL)
                            .transform(new CircleCrop())
                            .into(profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });


        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        viewPageAdapter.addFragment(new ChatsFragment(), "Chats");
        viewPageAdapter.addFragment(new UsersFragment(), "Users");
        viewPageAdapter.addFragment(new ProfileFragment(), "Profile");

        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);


    }


    class ViewPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPageAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        String currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");
        reference = FirebaseDatabase.getInstance().getReference().child(currentUser);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}