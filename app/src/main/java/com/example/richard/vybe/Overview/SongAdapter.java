package com.example.richard.vybe.Overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SpotifyConnect.SpotifyConnector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private String TAG = "SongAdapter";

    private ArrayList<Song> mDataset;
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvOverviewSongName;
        public ImageView ivOverviewSongImage;
        public TextView tvOverviewSongArtist;
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
            databaseReference = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Tracks");

            itemView.setOnClickListener(this);

            tvOverviewSongArtist= (TextView) itemView.findViewById(R.id.tvOverviewSongArtist);
            ivOverviewSongImage = (ImageView) itemView.findViewById(R.id.ivOverviewSongImage);
            tvOverviewSongName = (TextView) itemView.findViewById(R.id.tvOverviewSongName);

            ivOverviewSongImage.setOnClickListener(imageClickListener);

        }

        private View.OnClickListener imageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spotifyConnector.playSong(song);

            }
        };

        @Override
        public void onClick(View view) {
            if (song.getLiked()) {
                databaseReference.child(song.getId()).child("liked").setValue(false);
                spotifyConnector.removeSongFromLibrary(song);
                databaseReference.child(song.getId()).child("playlist").child("id").setValue(sharedPreferences.getString("playlist", null));
                databaseReference.child(song.getId()).child("playlist").child("name").setValue(sharedPreferences.getString("playlistname", null));
                song.setLiked(false);
                view.setBackgroundColor(context.getResources().getColor(R.color.transparent_red));
            } else {
                databaseReference.child(song.getId()).child("liked").setValue(true);
                spotifyConnector.saveSongToLibrary(song);
                song.setLiked(true);
                view.setBackgroundColor(context.getResources().getColor(R.color.transparent_green));
            }
        }
    }

    public SongAdapter(ArrayList<Song> myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public SongAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.track, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        MyViewHolder vh = new MyViewHolder(layoutView, mContext);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.i(TAG, "Artist name: " + mDataset.get(position).getArtist());
        holder.tvOverviewSongName.setText(mDataset.get(position).getName());
        holder.tvOverviewSongArtist.setText(mDataset.get(position).getArtist());
        holder.song = mDataset.get(position);
        if (mDataset.get(position).getLiked()) {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_green));


        } else {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_red));
        }
        Glide.with(mContext)
                .load(mDataset.get(position).getImageURL())
                .transform(new RoundedCorners(30))
                .into(holder.ivOverviewSongImage);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}