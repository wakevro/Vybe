package com.example.richard.vybe.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.richard.vybe.Adapter.UserAdapter;
import com.example.richard.vybe.Model.Chatlist;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.Notifications.Token;
import com.example.richard.vybe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private String TAG = "ChatsFragment";

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    String currentUser;
    SharedPreferences sharedPreferences;
    DatabaseReference databaseReference;

    private List<Chatlist> usersList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        usersList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(currentUser).setValue(token1);
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String getUser = snapshot.getKey();

                    if (snapshot.hasChild("id") && snapshot.hasChild("name") && snapshot.hasChild("profileImage")) {
                        User user = new User();
                        String userid = snapshot.child("id").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();
                        String profileImageURL = snapshot.child("profileImage").getValue().toString();
                        String status = snapshot.child("status").getValue().toString();

                        user.setDisplay_name(name);
                        user.setId(userid);
                        user.setProfileImageURL(profileImageURL);
                        user.setStatus(status);

                        for (Chatlist chatlist : usersList) {
                            if ((user.getDisplay_name() + " " + user.getId()).equals(chatlist.getId())) {
                                mUsers.add(user);

                                if (mUsers.size() != 0) {
                                    for (User user1 : mUsers) {
                                        if (!user.getId().equals(user1.getId())) {

                                            if (!mUsers.contains(user)) {
                                                mUsers.add(user);
                                                break;
                                            }

                                        }
                                    }
                                } else {
                                    if (!mUsers.contains(user)) {
                                        mUsers.add(user);
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}