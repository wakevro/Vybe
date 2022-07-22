package com.example.richard.vybe.spotifyConnect;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.richard.vybe.model.EndPoints;
import com.example.richard.vybe.model.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TracksService {
    private static final String ENDPOINT = EndPoints.TRACKS.toString();
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public TracksService(RequestQueue queue, SharedPreferences sharedPreferences) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
    }

    public void put(Song song) {
        JSONObject payload = preparePutPayload(song);
        JsonObjectRequest jsonObjectRequest = prepareSongLibraryRequest(payload, Request.Method.PUT);
        mqueue.add(jsonObjectRequest);
    }

    public void delete(Song song) {
        JSONObject payload = preparePutPayload(song);
        System.out.println(payload);
        prepareSongLibraryDeleteRequest(payload);
    }


    private JSONObject preparePutPayload(Song song) {
        JSONArray idarray = new JSONArray();
        idarray.put(song.getId());
        JSONObject ids = new JSONObject();
        try {
            ids.put("ids", idarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ids;
    }


    /**
     * With OkHttp because Volley does not allow body with DELETE
     * @param payload
     */
    private void prepareSongLibraryDeleteRequest(JSONObject payload) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        okhttp3.Request request = new okhttp3.Request.Builder().url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + msharedPreferences
                        .getString("token", "")).delete(requestBody).build();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private JsonObjectRequest prepareSongLibraryRequest(JSONObject payload, int method) {
        return new JsonObjectRequest(method, ENDPOINT, payload, response -> {
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


}
