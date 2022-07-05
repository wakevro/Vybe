# VYBE PROJECT DOCUMENTATION

###### app: `SUMMARY`

> This is a spotify playlist generator app that generates music based on user's mood using sentiment analysis.

## :memo: Required Features?

1. [AUTHENTICATION](#Authentication)
2. [WORKING WITH ENDPOINTS](#Working-with-Endpoints)
    - [Fetching recently played songs](#Fetching-recently-played-songs)
    - [Adding a Song to liked Songs](#Adding-a-Song-to-liked-Songs)


## Authentication

I used Spotify API to authenticate, make calls, and parse the results.

Although it is a REST API and therefore works the same for every client, the authentication differs widely for iOS, Android and Web. For my app I used Android authentication. To create the calls on Android, I used the widely known volley framework ( https://github.com/google/volley).

First, I needed to create a Spotify API project to allow me authenticate with it. To do that, I headed to https://developer.spotify.com/dashboard and logged in with my Spotify account.

After registering, and accepting the terms of service, I added a redirect URI to my whitelist.

On my android project, I added the required dependencies to my gradle files. I then created an empty activity and created an **authenticateSpotify()** method
```java=
 private void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
```

Then I open an AuthenticationRequest with my ClientID, and set my requested scopes (e.g. user-read-recently-played). These are different permissions I need to request from the users, for example, the permission to read their personal information. The requested scopes will be displayed to the users and they have to grant them to my application.

Finally, I send the request. This will open Spotify (if it’s installed) or fall back to a WebView where the user has to log in. The REQUEST_CODE is just a static number (e.g. 1337) to identify the application we just started.
Created some constants and replaced them with my ClientID and RedirectURI.

```java=
private static final String CLIENT_ID = <MY CLIENT_ID>;
private static final String REDIRECT_URI = <MY REDIRECT_URI>;
private static final int REQUEST_CODE = 1337;
private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";
```

Now, when the user logs in, he will be redirected to my application. That’s where I will receive the token if the request was successful. Added the following to the SplashActivity class:

```java=

@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
```

The user gets redirected back from the Spotify API. Then my application identifies the RequestCode, so it is clear that it is redirected from Spotify. Then the application checks if it received a token and proceeds accordingly. I save the token in my persistent storage with SharedPreferences (https://developer.android.com/reference/android/content/SharedPreferences).

**Lastly, I initialize my Shared Preferences and call the authenticateSpotify() method.** Added the following code to my SplashActivity:

```java=
public class SplashActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;

    private RequestQueue queue;

    private static final String CLIENT_ID = "3a7b0154a0fd4a868e41d59834f97bd5";
    private static final String REDIRECT_URI = "com.spotifyapiexample://callback";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);


        authenticateSpotify();

        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);
    }

```

I received the valid API Token and can start using the Spotify API.

The first call I made is getting some information about the user.

I created a model for the users with the information I am going to receive from the Spotify API. Created a new class called “User“ and added the following code:

```java=
public class User {

    public String birthdate;
    public String country;
    public String display_name;
    public String email;
    public String id;
}
```

Now, to keep things structured, I created a service class for every API endpoint I am going to use. Created a new package called “Connectors” and created a new class called “UserService” and added the following code:

``` java=

public class UserService {

    private static final String ENDPOINT = "https://api.spotify.com/v1/me";
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

```

For the GET request to the Endpoint I needed my Bearer token (the one I received in the last step) as a header. No additional parameters are required.

Here, I am generating a basic GET Request with a JsonObjectRequest. I simply parse the result with Gson and my created user class. So I create a new user object, and as soon as I get a response from the API I fill the values of the object with the result. The VolleyCallBack interface helps to know when I received a valid response and I can proceed accordingly.

To get the callback working, I created a new interface called “VolleyCallBack”


```java=
public interface VolleyCallBack {

    void onSuccess();
}
```

I only need one method, to notify me when the request was successful and I received a response.

Now I need to implement my method waitForUserInfo() in my SplashActivity class.


``` java=
private void waitForUserInfo() {
    UserService userService = new UserService(queue, msharedPreferences);
    userService.get(() -> {
        User user = userService.getUser();
        editor = getSharedPreferences("SPOTIFY", 0).edit();
        editor.putString("userid", user.id);
        Log.d("STARTING", "GOT USER INFORMATION");
        // We use commit instead of apply because we need the information stored immediately
        editor.commit();
        startMainActivity();
        });
    }

private void startMainActivity() {
    Intent newintent = new Intent(SplashActivity.this, MainActivity.class);
    startActivity(newintent);
    }
```

Now if I start my application I get my token, get the user information and redirected to the MainActivity, which will only display “Hello World!”. In the background, my application fetched a valid authentication token and requested additional information about the logged-in user. The Spotify ID is saved in the persistent storage, so we can access it again later. We just need to display the information.



## Working with Endpoints

| NAME          | LINKS                                                                  |
| ----------------- |:-------------------------------------------------------------------|
| RECENTLY_PLAYED   | :link:https://api.spotify.com/v1/me/player/recently-played         |
| TRACKS            | :link:https://api.spotify.com/v1/me/tracks                         |
| PLAYLIST          | :link:https://api.spotify.com/v1/playlists/%s/tracks               |
| PLAYLISTME        | :link:https://api.spotify.com/v1/me/playlists                      |
| PLAYLISTCREATE    | :link:https://api.spotify.com/v1/users/%s/playlists                |
| AUDIOFEATURES     | :link:https://api.spotify.com/v1/audio-features/%s                 |
| FEATUREDPLAYLISTS | :link:https://api.spotify.com/v1/browse/featured-playlists         |
| USER              | :link:https://api.spotify.com/v1/me                                |

## Fetching recently played songs
Now that I received the token and the user information, it’s time to use another API endpoint. I am going to fetch the recently played songs of the user. Built a new model for my Songs, similar to the User model I already created.

``` java=
public class Song {

    private String id;
    private String name;
    
    public Song(String id, String name) {
        this.name = name;
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
```

To get the recently played tracks, I wrote two methods.

``` java=
 private void getTracks() {
    songService.getRecentlyPlayedTracks(() -> {
        recentlyPlayedTracks = songService.getSongs();
        updateSong();
    });
}

private void updateSong() {
    if (recentlyPlayedTracks.size() > 0) {
        songView.setText(recentlyPlayedTracks.get(0).getName());
        song = recentlyPlayedTracks.get(0);
    }
}
```


In my getTracks() method, I called my SongService method which will fetch the songs from the API. After I received the data I am going to update the name of the song with my updateSong() method.

I need a new class called SongService where I will fetch the songs from the Spotify API. This class is similar to the UserService I implemented and handles the request we send out to the API.

``` java=
public class SongService {
    private ArrayList<Song> songs = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;

    public SongService(Context context) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(context);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public ArrayList<Song> getRecentlyPlayedTracks(final VolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/player/recently-played";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    Gson gson = new Gson();
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int n = 0; n < jsonArray.length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            object = object.optJSONObject("track");
                            Song song = gson.fromJson(object.toString(), Song.class);
                            songs.add(song);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, error -> {
                    // TODO: Handle error

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
        return songs;
    }

}

```

## Adding a Song to liked Songs


I need to generate a PUT request. This time I have to provide a body, which has to include an array of Spotify ID strings. I will only include on ID in this array because I am going to generate a request for every song.

I edited the SongService class and add the following lines:

``` java=
   public void addSongToLibrary(Song song) {
        JSONObject payload = preparePutPayload(song);
        JsonObjectRequest jsonObjectRequest = prepareSongLibraryRequest(payload);
        queue.add(jsonObjectRequest);
    }

    private JsonObjectRequest prepareSongLibraryRequest(JSONObject payload) {
        return new JsonObjectRequest(Request.Method.PUT, "https://api.spotify.com/v1/me/tracks", payload, response -> {
        }, error -> {
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
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
```


First, I prepared the body or here called payload. I generated a new JSONArray and add a single entry with the song ID. We wrap the whole thing in a JSONObject and the payload is ready.

Now I prepare the request I sent. This time it’s a **PUT** request so the method changed. Lastly, we add the request to our queue, so it will be **asynchronously executed.**
