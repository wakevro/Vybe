package com.example.richard.vybe.SpotifyConnect;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.richard.vybe.Model.EndPoints;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.VolleyCallBack;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserService {

    private String TAG = "UserService";

    private static final String ENDPOINT = EndPoints.USER.toString();
    private SharedPreferences msharedPreferences;
    private RequestQueue mqueue;
    private User user;

    public UserService(RequestQueue queue, SharedPreferences sharedPreferences) {
        mqueue = queue;
        msharedPreferences = sharedPreferences;
    }

    public User getUser() {
        return user;
    }

    public void get(final VolleyCallBack callBack) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(ENDPOINT, null, response -> {
            Gson gson = new Gson();
            user = gson.fromJson(response.toString(), User.class);
            Log.i(TAG, "USER INFO: " + response.toString());
            try {
                user.setId(response.getString("id"));
                user.setDisplay_name(response.getString("display_name"));
                user.setEmail(response.getString("email"));
                user.setCountry(response.getString("country"));
                user.setEmail("email");

                JSONObject image = response.getJSONArray("images").getJSONObject(0);
                user.setProfileImageURL(image.getString("url"));
                Log.i(TAG, "USER NAME: " + user.getDisplay_name());
                Log.i(TAG, "USER IMAGE: " + user.getProfileImageURL());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callBack.onSuccess();
        }, error -> get(() -> {

        })) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = msharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        mqueue.add(jsonObjectRequest);
    }


}
