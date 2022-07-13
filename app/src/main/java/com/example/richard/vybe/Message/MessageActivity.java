package com.example.richard.vybe.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.Adapter.MessageAdapter;
import com.example.richard.vybe.Fragments.APIService;
import com.example.richard.vybe.Model.Chat;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.Notifications.Client;
import com.example.richard.vybe.Notifications.Data;
import com.example.richard.vybe.Notifications.MyResponse;
import com.example.richard.vybe.Notifications.Sender;
import com.example.richard.vybe.Notifications.Token;
import com.example.richard.vybe.Profile.ProfileActivity;
import com.example.richard.vybe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private String TAG = "MessageActivity";

    private ImageView profile_image;
    private TextView username;

    String currentUser;
    String userFullId;
    SharedPreferences sharedPreferences;
    DatabaseReference databaseReference;


    ImageButton btnSend;
    EditText etSend;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;

    ValueEventListener seenListener;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btnSend = findViewById(R.id.btn_send);
        etSend = findViewById(R.id.text_send);


        intent = getIntent();
        userFullId = intent.getStringExtra("userFullId");
        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = etSend.getText().toString();

                if (!msg.equals("")) {
                    sendMessage(currentUser, userFullId, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send an empty message!", Toast.LENGTH_SHORT).show();
                }

                etSend.setText("");
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userFullId);

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
                if (userProfileImageURL.equals("")) {
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

                readMessages(currentUser, userFullId, user.getProfileImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userFullId);

    }

    private void seenMessage(String userid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentUser)
                .child(userFullId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userFullId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userFullId)
                .child(currentUser);
        chatRefReceiver.child("id").setValue(currentUser);


        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = new User();

                String id = dataSnapshot.child("id").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String profileImageURL = dataSnapshot.child("profileImage").getValue().toString();

                user.setDisplay_name(name);
                user.setId(id);
                user.setProfileImageURL(profileImageURL);

                if (notify) {
                    sendNotifiaction(receiver, user.getDisplay_name(), msg);
                }
                notify = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotifiaction(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUser, R.drawable.ic_vybe, username+": "+message, "New Message",
                            userFullId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(String myid, String userid, String imageurl) {
        mchat = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : datSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userFullId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}
