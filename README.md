# SpotifyReccomendation Labeling Application

## Description

This application is part of a machine learning based application for the classification of songs and producing recommendations.

This application is used for labeling the dataset, which consists of different songs from the Spotify Developer API (https://developer.spotify.com/).

The application pulls the last 20 songs that the user listened to. The user now has the possibility to swipe(similiar to tinder swiping) the songs left(dislike) and right(like) and optionally add them to his spotify library or add them to a playlist.

## Usage
To use this application for your own project you have to change the following things.

1. Go to https://developer.spotify.com/ and create a project which provides you a unique **ClientID. ex. "3a7b0154a0fd4a868e41d59834f97bd3"**

2. Add the redirect URI on your project whitelist in the project dashboard in your spotify project. **"com.example.hyferion.spotifyrec://callback"**

3. Change the ClientID in the **SplashActivity.java** to your own unique ClientID.

4. Connect your own Firebase Database to the application. For more information see https://firebase.google.com/docs/android/setup#console

Now you should be able to start the application for your own usage.

If you have any question feel free to contact me under silas.stulz@gmail.com
