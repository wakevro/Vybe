package com.example.richard.vybe.profile;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
import com.example.richard.vybe.profile.fragments.ChatsFragment;
import com.example.richard.vybe.profile.fragments.ProfileFragment;
import com.example.richard.vybe.profile.fragments.UsersFragment;
import com.example.richard.vybe.map.MapFragment;
import com.example.richard.vybe.model.Chat;
import com.example.richard.vybe.model.User;
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


    String currentUser;
    private DatabaseReference reference;
    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    private String name;
    private String userProfileImageURL;

    private ImageView profile_image;
    private ImageView profileBack;
    private TextView tvUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        profileBack = findViewById(R.id.profileBack);
        tvUsername = findViewById(R.id.username);


        profileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");
        requestQueue = Volley.newRequestQueue(this);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", ""));

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




        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("location")) {
                    databaseReference.child("location").setValue("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser) && !chat.isIsseen()) {
                        unread ++;
                    }
                }

                if (unread == 0) {
                    viewPageAdapter.addFragment(new ChatsFragment(), "Chats");
                } else{
                    viewPageAdapter.addFragment(new ChatsFragment(), "(" + unread + ") Chats");
                }

                viewPageAdapter.addFragment(new UsersFragment(), "Users");
                viewPageAdapter.addFragment(new ProfileFragment(), "Profile");
                viewPageAdapter.addFragment(new MapFragment(), "Map");

                viewPager.setAdapter(viewPageAdapter);
                tabLayout.setupWithViewPager(viewPager);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



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
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status("offline");
    }
}