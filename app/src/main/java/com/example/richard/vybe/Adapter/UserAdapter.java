package com.example.richard.vybe.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.Message.MessageActivity;
import com.example.richard.vybe.Model.Chat;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private String TAG = "UserAdapter";

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage;
    String currentUser;
    SharedPreferences sharedPreferences;

    public UserAdapter(@NonNull Context mContext, List<User> mUsers, boolean ischat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat =ischat;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = mUsers.get(position);
        holder.username.setText(user.getDisplay_name());
        String userProfileImageURL = user.getProfileImageURL();


        if (userProfileImageURL.equals("")) {
            Glide.with(mContext)
                    .load(R.drawable.man)
                    .transform(new CircleCrop())
                    .into(holder.profile_image);
        } else {
            Glide.with(mContext)
                    .load(userProfileImageURL)
                    .transform(new CircleCrop())
                    .into(holder.profile_image);
        }

        String userFullId = user.getDisplay_name() + " " + user.getId();

        if (ischat) {
            lastMessage(userFullId, holder.tvLastMessage);
        } else {
            holder.tvLastMessage.setVisibility(View.GONE);
        }

        Log.i(TAG, "USER: " + user.getStatus());
        if (ischat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userFullId", user.getDisplay_name() + " " + user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView tvLastMessage;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            tvLastMessage = itemView.findViewById(R.id.last_msg);
        }
    }

    private void lastMessage(String userid, TextView last_msg) {

        theLastMessage = "default";

        sharedPreferences = mContext.getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(currentUser) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(currentUser)) {
                        theLastMessage = chat.getMessage();
                    }
                }

                switch (theLastMessage) {
                    case "default" :
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);

                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
