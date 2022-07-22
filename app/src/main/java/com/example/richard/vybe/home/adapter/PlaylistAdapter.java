package com.example.richard.vybe.home.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.richard.vybe.home.PlaylistItemsActivity;
import com.example.richard.vybe.model.Playlist;
import com.example.richard.vybe.profile.ProfileActivity;
import com.example.richard.vybe.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {

    private String TAG = "PlaylistAdapter";

    private ArrayList<Playlist> mDataset;
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvPlaylistName;
        public TextView tvPlaylistSongSize;
        public ImageView ivPlaylistImage;
        public ImageView ivMore;
        private DatabaseReference databaseReference;
        private SharedPreferences.Editor editor;
        private SharedPreferences sharedPreferences;
        private Playlist playlist;
        private Context context;

        public MyViewHolder(View itemView, Context context) {
            super(itemView);

            this.context = context;
            sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", ""));
            itemView.setOnClickListener(this);

            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            tvPlaylistSongSize = itemView.findViewById(R.id.tvPlaylistSongSize);
            ivPlaylistImage = itemView.findViewById(R.id.ivPlaylistImage);
            ivMore = itemView.findViewById(R.id.ivMore);

            ivMore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
                    View bottomSheetView = LayoutInflater.from(context)
                            .inflate(
                                    R.layout.layout_bottom_share,
                                    (ConstraintLayout) itemView.findViewById(R.id.clBottomMusic)

                            );

                    ImageView ivPlaylistCover = bottomSheetView.findViewById(R.id.ivPlaylistCover);
                    TextView tvCopy = bottomSheetView.findViewById(R.id.tvCopyLink);
                    TextView tvShare = bottomSheetView.findViewById(R.id.tvShareLink);
                    TextView tvSend = bottomSheetView.findViewById(R.id.tvSendLink);

                    tvCopy.setOnClickListener(new View.OnClickListener() {
                        String playlistLink = "https://open.spotify.com/playlist/" + playlist.getId();
                        @Override
                        public void onClick(View view) {
                            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(context.getString(R.string.playlist_link), playlistLink);
                            clipboardManager.setPrimaryClip(clipData);
                            bottomSheetDialog.dismiss();
                        }
                    });

                    tvShare.setOnClickListener(new View.OnClickListener() {
                        String playlistLink = "https://open.spotify.com/playlist/" + playlist.getId();
                        @Override
                        public void onClick(View view) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            String Body = context.getString(R.string.check_out_playlist);
                            String Sub = playlistLink;
                            shareIntent.putExtra(Intent.EXTRA_TEXT, Body);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, Sub);
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_using_vybe)));
                            bottomSheetDialog.dismiss();
                        }
                    });

                    tvSend.setOnClickListener(new View.OnClickListener() {
                        String playlistLink = "https://open.spotify.com/playlist/" + playlist.getId();
                        @Override
                        public void onClick(View view) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("playlistLink", playlistLink);
                            editor.apply();
                            Intent intent = new Intent(context, ProfileActivity.class);
                            context.startActivity(intent);
                        }
                    });

                    Glide.with(bottomSheetDialog.getContext()).load(playlist.getImageURL()).transform(new RoundedCorners(30)).into(ivPlaylistCover);


                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                }
            });

        }

        @Override
        public void onClick(View view) {

            databaseReference.child("Playlist").child("id").setValue(playlist.getId());
            databaseReference.child("Playlist").child("name").setValue(playlist.getName());
            databaseReference.child("Playlist").child("tracks").setValue(playlist.getTotalTracks());

            editor = sharedPreferences.edit();
            editor.putString("playlist", playlist.getId());
            editor.putString("playlistname", playlist.getName());
            editor.apply();
            Intent intent = new Intent(context, PlaylistItemsActivity.class);
            intent.putExtra("playlistTitle", playlist.getName());
            context.startActivity(intent
            );

        }

    }

    public PlaylistAdapter(ArrayList<Playlist> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MyViewHolder vh = new MyViewHolder(layoutView, mContext);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.tvPlaylistName.setText(mDataset.get(position).getName());
        holder.playlist = mDataset.get(position);
        holder.tvPlaylistSongSize.setText(Integer.toString(holder.playlist.getTotalTracks()) + " songs");
        if (holder.playlist.getImageURL() != null) {
            Glide.with(mContext)
                    .load(mDataset.get(position).getImageURL())
                    .transform(new RoundedCorners(30))
                    .into(holder.ivPlaylistImage);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}