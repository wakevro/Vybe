package com.example.richard.vybe.Adapter;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.Model.Chat;
import com.example.richard.vybe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private String TAG = "MessageAdapter";


    public int result;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    ValueEventListener feelingListener;
    ValueEventListener editListener;
    SharedPreferences sharedPreferences;
    String currentUser;

    public MessageAdapter(@NonNull Context mContext, List<Chat> mChat, String imageurl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Chat chat = mChat.get(position);

        int reactions[] = new int[] {
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        if (chat.getReaction() != -1) {
            holder.feeling.setImageResource(reactions[chat.getReaction()]);
            holder.feeling.setVisibility(View.VISIBLE);
        }

        holder.showMessage.setText(chat.getMessage());


        holder.showMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.layout_reactions);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                dialog.getWindow().setWindowAnimations(R.style.AnimationsForDialog);

                ImageView like;
                ImageView love;
                ImageView laugh;
                ImageView wow;
                ImageView sad;
                ImageView angry;
                TextView tvCopy;
                TextView tvDelete;
                TextView tvEdit;

                like = dialog.findViewById(R.id.ic_fb_like);
                love = dialog.findViewById(R.id.ic_fb_love);
                laugh = dialog.findViewById(R.id.ic_fb_laugh);
                wow = dialog.findViewById(R.id.ic_fb_wow);
                sad = dialog.findViewById(R.id.ic_fb_sad);
                angry = dialog.findViewById(R.id.ic_fb_angry);
                tvCopy = dialog.findViewById(R.id.tvCopy);
                tvDelete = dialog.findViewById(R.id.tvDelete);
                tvEdit = dialog.findViewById(R.id.tvEdit);

                if (!chat.getSender().equals(currentUser)) {
                    tvEdit.setVisibility(View.GONE);
                }

                tvCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("Message", chat.getMessage());
                        clipboardManager.setPrimaryClip(clipData);
                        dialog.dismiss();
                    }
                });

                tvDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
                        databaseReference.child(chat.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(mContext, mContext.getString(R.string.successfully_deleted), Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(mContext, mContext.getString(R.string.failed_to_delete), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });

                tvEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog editDialog = new Dialog(mContext);
                        editDialog.setContentView(R.layout.layout_edit);
                        editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        editDialog.getWindow().setWindowAnimations(R.style.AnimationsForDialog);


                        EditText etEditText;
                        ImageButton btnEditSend;

                        etEditText = editDialog.findViewById(R.id.etEditText);
                        btnEditSend = editDialog.findViewById(R.id.btnEditSend);
                        etEditText.setText(chat.getMessage());
                        btnEditSend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String newMessage = etEditText.getText().toString();
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
                                editListener = databaseReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Chat newChat = snapshot.getValue(Chat.class);
                                            if ((newChat.getId() == chat.getId())) {
                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                hashMap.put("message", newMessage);
                                                snapshot.getRef().updateChildren(hashMap);
                                                databaseReference.removeEventListener(editListener);
                                                return;
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                editDialog.dismiss();
                            }
                        });
                        editDialog.show();

                        dialog.dismiss();

                    }
                });

                like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        result = 0;
                        processReaction(result, holder, chat, reactions);
                        dialog.dismiss();
                    }
                });
                love.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        result = 1;
                        processReaction(result, holder, chat, reactions);
                        dialog.dismiss();
                    }
                });
                laugh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        result = 2;
                        processReaction(result, holder, chat, reactions);
                        dialog.dismiss();
                    }
                });
                wow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        result = 3;
                        processReaction(result, holder, chat, reactions);
                        dialog.dismiss();
                    }
                });
                sad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        result = 4;
                        processReaction(result, holder, chat, reactions);
                        dialog.dismiss();
                    }
                });
                angry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        result = 5;
                        processReaction(result, holder, chat, reactions);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


        if (imageurl.equals("")) {
            Glide.with(mContext)
                    .load(R.drawable.man)
                    .transform(new CircleCrop())
                    .into(holder.profile_image);
        } else {
            Glide.with(mContext)
                    .load(imageurl)
                    .transform(new CircleCrop())
                    .into(holder.profile_image);



        }


        if (position == mChat.size()-1) {
            if (chat.isIsseen()) {
                holder.tvSeen.setText(R.string.seen);
            } else {
                holder.tvSeen.setText(R.string.delivered);
            }
        } else {
            holder.tvSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView showMessage;
        public ImageView profile_image;
        public TextView tvSeen;
        public ImageView feeling;

        public ViewHolder(View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            tvSeen = itemView.findViewById(R.id.txt_seen);
            feeling = itemView.findViewById(R.id.feeling);


        }
    }

    @Override
    public int getItemViewType(int position) {
        sharedPreferences = mContext.getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        if (mChat.get(position).getSender().equals(currentUser)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    private void updateFeeling(Chat chat, int pos) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        feelingListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat newChat = snapshot.getValue(Chat.class);
                    Log.i(TAG, "New chat: " + newChat.getId() + ", chat: " + chat.getId()
                            + ", pos: " + pos);
                    if (newChat.getId() == chat.getId()) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("reaction", pos);
                        snapshot.getRef().updateChildren(hashMap);
                        databaseReference.removeEventListener(feelingListener);
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void processReaction(int result, ViewHolder holder, Chat chat, int[] reactions) {
        if (result != -1) {
            if (result == chat.getReaction()) {
                holder.feeling.setVisibility(View.INVISIBLE);
                updateFeeling(chat, -1);
            } else {
                holder.feeling.setVisibility(View.INVISIBLE);
                holder.feeling.setImageResource(reactions[result]);
                holder.feeling.setVisibility(View.VISIBLE);
                updateFeeling(chat, result);
            }
        }
    }

}