package com.example.richard.vybe.SpotifyConnect;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.richard.vybe.Model.EndPoints;
import com.example.richard.vybe.Model.Playlist;
import com.example.richard.vybe.Model.Song;
import com.example.richard.vybe.VolleyCallBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlaylistService {

    private String TAG = "PlaylistService";

    private DatabaseReference songDB;
    private DatabaseReference playlistItemDB;
    private static final String ENDPOINT = EndPoints.PLAYLIST.toString();
    private static final String ENDPOINTME = EndPoints.PLAYLISTME.toString();
    private static final String ENDPOINTSONGS = EndPoints.PLAYLISTITEMS.toString();
    private static final String ENDPOINTRECENTLY = EndPoints.RECENTLY_PLAYED.toString();
    private String ENDPOINTPLAYLISTITEM;
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    private String URL;
    JSONObject payload;
    private String playlistID;
    private ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<Song> songs = new ArrayList<>();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    public PlaylistService(RequestQueue queue, SharedPreferences sharedPreferences) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
        songDB = FirebaseDatabase.getInstance().getReference().child(msharedPreferences.getString("username", "") + " " + msharedPreferences.getString("userid", "")).child("Tracks");
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public ArrayList<Song> getPlaylistsSongs() {
        return songs;
    }

    public void get(final VolleyCallBack callBack) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ENDPOINTME, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                JSONArray jsonArray = response.optJSONArray("items");

                for (int n = 0; n < jsonArray.length(); n++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(n);
                        Log.i(TAG, "PLAYLIST: " + jsonObject.toString());
                        Playlist playlist = gson.fromJson(jsonObject.toString(), Playlist.class);
                        try {
                            playlist.setImageURL(jsonObject.optJSONArray("images").optJSONObject(0).getString("url"));
                        } catch (NullPointerException e) {
                            playlist.setImageURL(null);
                        }
                        playlist.setTotalTracks(jsonObject.getJSONObject("tracks").getInt("total"));
                        Log.i(TAG, "PLAYLIST TOTAL: " + playlist.getTotalTracks());

                        playlists.add(playlist);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                callBack.onSuccess();
            }
        }, error -> get(() -> {

        })) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = msharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        mqueue.add(jsonObjectRequest);

    }


    public void put(Song song, String playlistID) {
        JSONObject payload = preparePayload(song);

        URL = String.format(ENDPOINT, playlistID);

        JsonObjectRequest jsonObjectRequest = preparePutRequest(payload, Request.Method.POST);
        mqueue.add(jsonObjectRequest);
    }

    public void delete(Song song) {
        payload = preparePayload(song);
        getPlaylistIdForSong(song);
    }

    public void getPlaylistIdForSong(Song song) {
        songDB.child(song.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    playlistID = dataSnapshot.child("playlist").child("id").getValue().toString();
                    URL = String.format(ENDPOINT, playlistID);
                    deleteRequest(payload);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void deleteRequest(JSONObject payload) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        okhttp3.Request request = new okhttp3.Request.Builder().url(URL)
                .addHeader("Authorization", "Bearer " + msharedPreferences
                        .getString("token", "")).delete(requestBody).build();

        AsyncTask.execute(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                System.out.println(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    private JSONObject preparePayload(Song song) {
        JSONArray uriArray = new JSONArray();
        uriArray.put("spotify:track:" + song.getId());
        JSONObject uris = new JSONObject();
        try {
            uris.put("uris", uriArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uris;
    }


    private JsonObjectRequest preparePutRequest(JSONObject payload, int method) {
        return new JsonObjectRequest(method, URL, payload, response -> {
        }, error -> {
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = msharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
    }

    public void getSongs(final VolleyCallBack callBack) {

        playlistItemDB = FirebaseDatabase.getInstance().getReference().child(msharedPreferences.getString("username", "") + " " + msharedPreferences.getString("userid", "")).child("Playlist");

        playlistItemDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playlistID = snapshot.child("id").getValue().toString();
                Log.i(TAG, "SELECTED ID IS: " + playlistID);
                ENDPOINTPLAYLISTITEM = String.format(ENDPOINTSONGS, playlistID);
                Log.i(TAG, "ENDPOINT IS: " + ENDPOINTPLAYLISTITEM);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ENDPOINTPLAYLISTITEM, null, new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        JSONArray jsonArray = response.optJSONArray("items");

                        for (int n = 0; n < jsonArray.length(); n++) {
                            try {
                                JSONObject object = jsonArray.getJSONObject(n);
                                object = object.optJSONObject("track");
                                Song song = gson.fromJson(object.toString(), Song.class);
                                song.setArtist(object.optJSONArray("artists").optJSONObject(0).getString("name"));
                                String playURL = object.getString("uri");
                                String previewURL = object.getString("preview_url");
                                song.setPlayURL(playURL);
                                song.setPreviewURL(previewURL);
                                try {
                                    String imgUrl = object.optJSONObject("album").optJSONArray("images").optJSONObject(0).getString("url");
                                    song.setImageURL(imgUrl);
                                } catch (NullPointerException e) {
                                    song.setImageURL("");
                                }

                                songs.add(song);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        callBack.onSuccess();
                    }
                }, error -> get(() -> {

                })) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String token = msharedPreferences.getString("token", "");
                        String auth = "Bearer " + token;
                        headers.put("Authorization", auth);
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };
                mqueue.add(jsonObjectRequest);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
