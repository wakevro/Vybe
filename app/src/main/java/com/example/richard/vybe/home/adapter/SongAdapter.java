package com.example.richard.vybe.home.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.richard.vybe.model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.spotifyConnect.SpotifyConnector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private String TAG = "SongAdapter";

    private ArrayList<Song> mDataset;
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvOverviewSongName;
        public ImageView ivOverviewSongImage;
        public TextView tvOverviewSongArtist;
        public LottieAnimationView ltLike;

        private DatabaseReference databaseReference;
        private Song song;
        private SpotifyConnector spotifyConnector;
        private Context context;
        private SharedPreferences sharedPreferences;

        public MyViewHolder(View itemView, Context context) {
            super(itemView);

            this.context = context;

            spotifyConnector = new SpotifyConnector(context);
            sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Tracks");



            tvOverviewSongArtist= itemView.findViewById(R.id.tvOverviewSongArtist);
            ivOverviewSongImage = itemView.findViewById(R.id.ivOverviewSongImage);
            tvOverviewSongName = itemView.findViewById(R.id.tvOverviewSongName);
            ltLike = itemView.findViewById(R.id.ltLike);

            ivOverviewSongImage.setOnClickListener(imageClickListener);

        }

        private View.OnClickListener imageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spotifyConnector.playSong(song);

            }
        };

    }

    public SongAdapter(ArrayList<Song> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.track, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MyViewHolder vh = new MyViewHolder(layoutView, mContext);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SPOTIFY", 0);
        Log.i(TAG, "Artist name: " + mDataset.get(position).getArtist());
        holder.tvOverviewSongName.setText(mDataset.get(position).getName());
        holder.tvOverviewSongArtist.setText(mDataset.get(position).getArtist());
        holder.song = mDataset.get(position);

        holder.ltLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLike(holder);
            }
        });

        holder.itemView.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick() {
                updateLike(holder);
            }
        });
        if (mDataset.get(position).getLiked()) {
            holder.ltLike.setMinAndMaxProgress(0.5f, 0.5f);

        } else {
            holder.ltLike.setMinAndMaxProgress(1.0f, 1.0f);
        }
        holder.ltLike.playAnimation();

        Glide.with(mContext)
                .load(mDataset.get(position).getImageURL())
                .transform(new RoundedCorners(30))
                .into(holder.ivOverviewSongImage);

    }

    public void updateLike(MyViewHolder holder) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SPOTIFY", 0);
        SpotifyConnector spotifyConnector = new SpotifyConnector(mContext);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Tracks");

        if (holder.song.getLiked()) {
            databaseReference.child(holder.song.getId()).child("liked").setValue(false);
            spotifyConnector.removeSongFromLibrary(holder.song);
            databaseReference.child(holder.song.getId()).child("playlist").child("id").setValue(sharedPreferences.getString("playlist", null));
            databaseReference.child(holder.song.getId()).child("playlist").child("name").setValue(sharedPreferences.getString("playlistname", null));
            holder.song.setLiked(false);
            holder.ltLike.setMinAndMaxProgress(0.5f, 1.0f);

        } else {
            databaseReference.child(holder.song.getId()).child("liked").setValue(true);
            spotifyConnector.saveSongToLibrary(holder.song);
            holder.song.setLiked(true);
            holder.ltLike.setMinAndMaxProgress(0.0f, 0.5f);
        }
        holder.ltLike.playAnimation();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public abstract class DoubleClickListener implements View.OnClickListener {

        // The time in which the second tap should be done in order to qualify as
        // a double click
        private static final long DEFAULT_QUALIFICATION_SPAN = 200;
        private long doubleClickQualificationSpanInMillis;
        private long timestampLastClick;

        public DoubleClickListener() {
            doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
            timestampLastClick = 0;
        }

        public DoubleClickListener(long doubleClickQualificationSpanInMillis) {
            this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis;
            timestampLastClick = 0;
        }

        @Override
        public void onClick(View v) {
            if((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
                onDoubleClick();
            }
            timestampLastClick = SystemClock.elapsedRealtime();
        }

        public abstract void onDoubleClick();

    }
}