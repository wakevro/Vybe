RICHARD ABHULIMHEN - README
===

# VYBE

## Table of Contents
1. [Overview](#Overview)
2. [Product Spec](#Product-Spec)
3. [Wireframes](#Wireframes)
4. [Screen Snapshots](#Screen-Snapshots)
5. [App Features](#App-Features)
6. [Quick Demos](#Quick-Demos)

## Overview
### Description
This is a Spotify playlist generator app that generates music based on user's mood using sentiment analysis. It has the functionality of playing music remotely if Spotify is installed on the current device. Users could also actively communicate with other users with a chat feature that allow users send, receive, edit, copy, and react to messages as well as receive notifications for messages. Users can also view other users around them with an interactive map that allows the user to dynamically change the radius of the users shown.

### App Evaluation

- **Category:** Digital Music / Utility / Social
- **Mobile:** This app would be primarily developed for mobile.
- **Story:** This is a spotify playlist generator app that generates music based on user's mood. User writes a sentence, then the app analyze the mood of the user using sentiment analysis and generates a swipe view of recommendations. User then have of option of swiping left or right to add a recommended song to a new playlist.
- **Market:** Anyone that listens to music on Spotify could enjoy this app. Ability to create playlist based on user's mood.
- **Habit:** Users can swipe through recommended music. Users can generate music by inputting current mood. Users can create playlist. Users can chat other users and view users around their location.
- **Scope:**  First we would start with recommending music to people based on bood, then perhaps this could evolve into a music sharing application as well to broaden its usage. Large potential for use with spotify.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

- Users are able to login/sign-up/logout from the application
- Users are able to sync with Spotify.
- Users are able to type a sentence and generate music recommendations.
- Users are able to swipe through recommendations to add or remove songs to playlist.
- Users can edit/delete a playlist.
- Automatically create playlist and sync with Spotify.
- Users can connect with other users.
- Users can view other users on a map.

**Optional Nice-to-have Stories**

- Users can have a profile displaying playlist.

### 2. Screen Archetypes

* Login Screen
  * Users are able to authenticate with Spotify.

* Playlist Screen
  * Users can select from view and select from playlists.

* Mood Screen
  * Users can input mood.

* Swipe Music Screen
  * Users can swipe through music to add or remove music from recommended music.

* Create Playlist Screen
  * Users can create playlist with liked swiped songs.

* Chat Screen
  * Users can view chats with other users.

* Map Screen
  * Users can view other users on a map.



### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home Screen
* Mood Screen
* Swipe Screen
* Create Playlist Screen
* Chat Screen
* Users Screen
* Profile Screen
* Map Screen

## Wireframes
<p float="left">
  <img src="https://imgur.com/btzGJL2.jpg" width=500 />
  <img src="https://imgur.com/OJCQ4kN.jpg" width=500 />
</p>

## Screen Snapshots
![](https://imgur.com/NpFlmWj.jpg)
![](https://imgur.com/5BvX9Al.jpg)
![](https://imgur.com/zG93OYb.jpg)

## :memo: App Features
- **Sentiment Analysis:** The app has a feature that asks the user to describe their current mood. On getting the user's input, I send their input to an API I created which analyses the users's text using Textblob library and returns a polarity sentiment. After that, I fetch songs from the user's Spotify and pass each song through an algorithm I created to analyze the song's mood. This algorithm was derived using Linear regression by plotting graphs of the song's valence, danceability, and energy against collected data of perceived sentiments. After analyzing the graph, I was able to deduce an algorithm that analyses the songs and return a mood for the songs. Any song that matches the user's derived mood is then displayed in Tinder-like swipe interface.

- **Comprehensive Chat 💬:** The app gives users the ability to connect with other users through 1 to 1 chat features that has variety of user-friendly features. Some of the features include:
  - Realtime chat: This allow users to receive and send messages which instantly delivers without delay. This was made possible by utilizing Firebase's realtime database.
  - Instant Notification: Users receive instant notifications for incoming messsages. The notification shows the detail of the message and the sender. On click of the notification, the chat containing the message opens.
  - Seen and Delivered status: Users can see if their message has been seen or if it was only delivered and yet to be seen by the receiver.
  - Online and Offline status: Users can view the online/offline of other users they have chats with.
  - Reactions, Copy, Edit, and Delete: Users can react to, copy, edit, and delete messages. Users have the ability to edit only messages sent by themselves and not that of other users.
  >For more info about the chat feature, kindly view: [CHAT FEATURE](https://github.com/wakevro/Vybe/blob/main/CHAT%20FEATURE.md)

- **Interactive Map 📍:** The app provides an interactive map interface which allows to view and connect with other users based on location proximity. Users can dynamically increase and decrease the radius of the location in which they want to view other users. For the map, users have the ability to save their and thn view other users based on their saved location.

- **Play, Like, Create and Share Playlist 🎵:** Users have the ability to view all playlist present in their Spotify accounts. They can also view the contents of every playlist.
  - To play the contents of the playlist, if the user has Spotify installed, the app remotely connects to the installed Spotify and play the contents. If there is no Spotify app installed, a Toast informing the user that no Spotify app is installed. However, all users can play a preview of songs in the Swipe interface regardless of if Spotify is installed.
  - Users can also active like and dislike songs from their Spotify liked songs within this app by double tapping on the song.
  - Users can also create Playlists which is synced to their Spotify and share the playlist to other apps or send to any user they have chats with.
  >For more info about working with Spotify, kindly view: [SPOTIFY DOCUMENTATION](https://github.com/wakevro/Vybe/blob/main/DOCUMENTATION.md)

## Quick Demos

<p float="left">
  <img src="https://imgur.com/m7T08iq.jpg" width=200 />
  <img src="https://imgur.com/ePkXjzX.jpg" width=200 />
  <img src="https://imgur.com/WuUCXlF.png" width=200 />
</p>

<p float="left">
  <img src="https://imgur.com/sdvIPY5.png" width=200 />
  <img src="https://imgur.com/5wfyTAS.png" width=200 />
  <img src="https://imgur.com/Yh8Wl0Z.png" width=200 />
</p>
