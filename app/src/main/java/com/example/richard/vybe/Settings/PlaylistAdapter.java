package com.example.richard.vybe.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {
    private ArrayList<Playlist> mDataset;
    private Context mContext;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public ImageView imageView;
        private DatabaseReference databaseReference;
        private SharedPreferences.Editor editor;
        private SharedPreferences sharedPreferences;
        private Playlist playlist;
        private Context context;

        public MyViewHolder(View itemView, Context context) {
            super(itemView);

            this.context = context;
            sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
            databaseReference = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("userid", ""));

            itemView.setOnClickListener(this);

            title = (TextView) itemView.findViewById(R.id.title);
            imageView = (ImageView) itemView.findViewById(R.id.img);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(context, "Playlist " + playlist.getName() + " selected", Toast.LENGTH_SHORT).show();

            databaseReference.child("Playlist").child("id").setValue(playlist.getId());
            databaseReference.child("Playlist").child("name").setValue(playlist.getName());

            editor = sharedPreferences.edit();
            editor.putString("playlist", playlist.getId());
            editor.putString("playlistname", playlist.getName());
            editor.apply();
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
        holder.title.setText(mDataset.get(position).getName());
        holder.playlist = mDataset.get(position);
        if (holder.playlist.getImageURL() != null) {
            Glide.with(mContext).load(mDataset.get(position).getImageURL()).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}