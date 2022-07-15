package com.example.richard.vybe.Home;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.SpotifyConnector;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class PlaylistItemsAdapter extends RecyclerView.Adapter<PlaylistItemsAdapter.MyViewHolder> {

    private ArrayList<Song> mSongs;
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String TAG = "PlaylistItemsAdapter";

        public boolean isPlaying = false;
        public TextView tvSongName;
        public TextView tvSongArtist;
        public ImageView ivSongImage;
        private DatabaseReference playlistItemDB;
        private SharedPreferences sharedPreferences;
        private SpotifyConnector spotifyConnector;
        private Song song;
        private Context context;

        public MyViewHolder(View itemView, Context context) {
            super(itemView);

            this.context = context;
            sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
            playlistItemDB = FirebaseDatabase.getInstance().getReference("Users").child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Playlist");

            spotifyConnector = new SpotifyConnector(context);
            itemView.setOnClickListener(this);

            tvSongName = itemView.findViewById(R.id.tvDetailsSongName);
            tvSongArtist = itemView.findViewById(R.id.tvDetailsSongArtist);
            ivSongImage = itemView.findViewById(R.id.ivDetailsSongImage);

        }

        @Override
        public void onClick(View view) {


            String songToPlay = song.getPlayURL();
            try {
                spotifyConnector.mSpotifyAppRemote.getPlayerApi().play(songToPlay);
                isPlaying = true;
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(context)
                        .inflate(
                                R.layout.layout_bottom_music,
                                (ConstraintLayout) itemView.findViewById(R.id.clBottomMusic)

                        );

                ImageView ivSongCover = bottomSheetView.findViewById(R.id.ivSongCover);
                LottieAnimationView ltPlayPause = bottomSheetView.findViewById(R.id.ltPlayPause);
                TextView tvSongName = bottomSheetView.findViewById(R.id.tvSongName);
                tvSongName.setText(song.getName());
                Glide.with(bottomSheetDialog.getContext()).load(song.getImageURL()).transform(new RoundedCorners(30)).into(ivSongCover);

                ltPlayPause.setMinAndMaxProgress(0.0f, 0.5f);
                ltPlayPause.playAnimation();

                ltPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isPlaying) {
                            spotifyConnector.mSpotifyAppRemote.getPlayerApi().pause();
                            ltPlayPause.setMinAndMaxProgress(0.5f, 1.0f);
                            ltPlayPause.playAnimation();
                            isPlaying = false;
                        } else {
                            spotifyConnector.mSpotifyAppRemote.getPlayerApi().resume();
                            ltPlayPause.setMinAndMaxProgress(0.0f, 0.5f);
                            ltPlayPause.playAnimation();
                            isPlaying = true;
                        }
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            } catch (NullPointerException e) {
                Toast.makeText(context, context.getString(R.string.no_spotify), Toast.LENGTH_SHORT).show();;
            }


        }


    }

    public PlaylistItemsAdapter(ArrayList<Song> songs, Context context) {
        mSongs = songs;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_playlist_details_iems, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MyViewHolder vh = new MyViewHolder(layoutView, mContext);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvSongName.setText(mSongs.get(position).getName());
        holder.tvSongArtist.setText(mSongs.get(position).getArtist());
        holder.song = mSongs.get(position);
        if (mSongs.get(position).getImageURL() != null) {
            Glide.with(mContext)
                    .load(mSongs.get(position).getImageURL())
                    .transform(new RoundedCorners(30))
                    .into(holder.ivSongImage);
        }
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }
}