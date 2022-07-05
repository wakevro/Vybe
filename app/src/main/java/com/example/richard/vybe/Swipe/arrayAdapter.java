package com.example.richard.vybe.Swipe;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.R;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<Song> {

    MediaPlayer mediaPlayer;

    public arrayAdapter(Context context, int resourceId, List<Song> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Song card_item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = convertView.findViewById(R.id.name);
        TextView artist = convertView.findViewById(R.id.artist);
        ImageView image = convertView.findViewById(R.id.image);
        CardView cardView = convertView.findViewById(R.id.cardView);

        name.setText(card_item.getName());
        artist.setText(card_item.getArtist());

        Glide.with(convertView.getContext())
                .load(card_item.getImageURL())
                .into(image);

        return convertView;

    }



}
