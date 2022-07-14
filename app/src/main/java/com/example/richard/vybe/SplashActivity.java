package com.example.richard.vybe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.richard.vybe.Model.EndPoints;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.SpotifyConnect.UserService;
import com.example.richard.vybe.Swipe.MainActivity;
import com.google.gson.Gson;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SplashActivity extends AppCompatActivity {

    private String TAG = "SplashActivity";

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;


    private static final String ENDPOINT = EndPoints.USER.toString();
    private User user;

    private RequestQueue queue;

    private static final String CLIENT_ID = "4b2696ee4bdc43b6aeb2c828e76b9374";
    private static final String REDIRECT_URI = "com.example.richard.vybe://callback";
    private static final int REQUEST_CODE = 1337;
    private int stopCount = 0;

    private String logOut = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "START SPLASH");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);




        logOut = getIntent().getStringExtra("logOut");
        Log.i(TAG, "Logout state: " + logOut);

        if (logOut != null && logOut.equals("true")) {
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-recently-played,user-library-modify,user-library-read,playlist-modify-public,playlist-modify-private,user-read-email,user-read-private,playlist-read-private,playlist-read-collaborative"});
            builder.setShowDialog(true);
            AuthorizationRequest request = builder.build();
            AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
            return;

        }



        Log.d(TAG, "JUST STARTING");
        authenticateSpotify();
        Log.d(TAG, "FINISHED AUTHENTICATING");


        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);
    }



    private void waitForUserInfo() {
        Log.i(TAG, "STARTING TO GET USER INFO.");
        UserService userService = new UserService(queue, msharedPreferences);
        Log.i(TAG, "GOTTEN USER SERVICE.");

        stopCount += 1;
        Log.i(TAG, "Stop count: " + stopCount);
        if (stopCount  < 4) {
            Log.i(TAG, "REACHED GET");
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

                    try {
                        JSONObject image = response.getJSONArray("images").getJSONObject(0);
                        user.setProfileImageURL(image.getString("url"));
                    } catch (Exception e) {
                        user.setProfileImageURL(null);
                    }

                    Log.i(TAG, "USER NAME: " + user.getDisplay_name());
                    Log.i(TAG, "USER IMAGE: " + user.getProfileImageURL());

                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("userid", user.id);
                    editor.putString("username", user.display_name);
                    String mobileTxt = user.display_name + " " + user.id;
                    String nameTxt = user.display_name;


                    Log.i(TAG, "MOBILE TXT: " + mobileTxt);
                    Log.d(TAG, "GOT USER INFORMATION");
                    editor.commit();
                    startMainActivity();
                    finish();
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }, error -> {
                Log.i(TAG, "FOUND THIS...");
                startErrorPage();

            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = msharedPreferences.getString("token", "");
                    String auth = "Bearer " + token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            try {
                queue.add(jsonObjectRequest);
            } catch (Exception e) {
                Intent restart = new Intent(this, SplashActivity.class);
                startActivity(restart);
            }

        } else {
            Log.i(TAG, "STOPPED.");
            startErrorPage();
            Toast.makeText(SplashActivity.this, "THERE IS AN ERROR!", Toast.LENGTH_SHORT).show();
        }

    }

    private void startMainActivity() {
        Intent newintent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(newintent);
        finish();
    }


    private void authenticateSpotify() {
        Log.d(TAG, "START AUTHENTICATING");
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-recently-played,user-library-modify,user-library-read,playlist-modify-public,playlist-modify-private,user-read-email,user-read-private,playlist-read-private,playlist-read-collaborative"});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.d(TAG, "START ACTIVITY RESULT");
        Log.d(TAG, "RESULT CODE: " + resultCode);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            Log.d(TAG, "FINISHED REQUEST CODE");
            Log.d(TAG, "RESULT TYPE" + response.getType().toString());

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d(TAG, "GOT AUTH TOKEN");
                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.d(TAG, "ERROR IN SWITCH CASE: " + response.getError());
                    startErrorPage();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Log.d(TAG, "DEFAULT IN SWITCH CASE");
                    finish();
                    // TODO: OPEN ERROR ACTIVITY.
            }

        }
    }


    private void startErrorPage() {
        Intent intent = new Intent(SplashActivity.this, ErrorActivity.class);
        startActivity(intent);
        finish();
    }


}
