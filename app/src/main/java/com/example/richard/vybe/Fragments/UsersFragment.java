package com.example.richard.vybe.Fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.richard.vybe.Adapter.UserAdapter;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    private String TAG = "UsersFragment";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    EditText etSearchUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        readUsers();
        progressDialog.dismiss();

        etSearchUsers = view.findViewById(R.id.search_users);
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void searchUsers(String s) {

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        String currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String getUser = snapshot.getKey();
                    User user = new User();
                    if (snapshot.hasChild("id") && snapshot.hasChild("name") && snapshot.hasChild("profileImage")) {

                        String id = snapshot.child("id").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();
                        String profileImageURL = snapshot.child("profileImage").getValue().toString();

                        user.setDisplay_name(name);
                        user.setId(id);
                        user.setProfileImageURL(profileImageURL);

                        assert user != null;
                        assert currentUser != null;

                        if (!getUser.equals(currentUser) && !getUser.equals("Chats") && !getUser.equals("ChatList") && !getUser.equals("Tokens")) {
                            mUsers.add(user);
                        }
                    }

                }

                userAdapter = new UserAdapter(getContext(), mUsers, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readUsers() {

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        String currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (etSearchUsers.getText().toString().equals("")) {

                    mUsers.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String getUser = snapshot.getKey();
                        User user = new User();

                        if (snapshot.hasChild("id") && snapshot.hasChild("name") && snapshot.hasChild("profileImage")) {
                            String id = snapshot.child("id").getValue().toString();
                            String name = snapshot.child("name").getValue().toString();
                            String profileImageURL = snapshot.child("profileImage").getValue().toString();

                            user.setDisplay_name(name);
                            user.setId(id);
                            user.setProfileImageURL(profileImageURL);

                            assert user != null;
                            assert currentUser != null;

                            if (!getUser.equals(currentUser) && !getUser.equals("Chats") && !getUser.equals("ChatList") && !getUser.equals("Tokens")) {
                                mUsers.add(user);
                            }
                        }

                    }

                    userAdapter = new UserAdapter(getContext(), mUsers, false);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}