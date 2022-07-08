# ONE TO ONE CHAT FEATURE


> This features allow users to have real-time multiple personal chats with different users while having several features such as delivered or seen, online or offline, instant cusomized notifications and emoji reaction.

## :memo: Required Features?

1. [REAL TIME SENDING AND RECEIVING MESSAGES](#Sending-and-Receiving-messages)
2. [DELIVERED AND SEEN STAUS](#Delivered-and-Seen-status)
3. [ONLINE AND OFFLINE STATUS](#Online-and-Offline-Status)
4. [INSTANT CUSTOMIZED NOTIFICATIONS](#Instant-Notifications)
5. [ADD REACTIONS TO MESSAGE](#Add-reactions-to-message)
6. [SEARCH AND MESSAGE ANY USER OFF THE APP BY NAME](#Search-and-Message-any-user-off-the-app-by-name)



## Sending and Receiving messages

To start sending message, I am going to create a MessageActivity. In this activity, after getting a non-empty text from the user, on click of the send button, I am going to make an instance to Firebase realtime database and create a child called "Chats". To send this message to a particular user, I am also going to create a database reference to the "Users" child in my database and make sure that I have the Id of both the current user which is the sender and the Id of the receiver, which is the User whose chat is opened. Unlike regular group chats, I am going to create an Hashmap containing the message, the sender, and the receiver and push this Hashmap to the the "Chats" child. This "Chats" child is going to contain all messages sent by all users.

After this, I am going to create a MessageAdapter and a UserAdapter that extends a recycler view adapter. I will then create a Chat class, that has a sender, receiver and message adapter.

To know where I place each chat either left or right, I will create two constants MSG_TYPE_LEFT = 0 and MSG_TYPE_RIGHT = 1. In the getItemViewType, I intend to check for the position of the chat. for example, if the the List<Chat> the current position sender is equals to the current user then I return MSG_TYPE_RIGHT, else I return MSG_TYPE_LEFT.

I might face an error here, which might be Firebase return the users= in the wrong position. I will have to figure out a solution for that.

After that, I in the onBindViewHolder, I intend to get the position of the Chat and and show the text. To show the text, I have to make sure there is VISBILITY for only the MSG_TYPE returned.

In the MessageActivity, I will create a readMessages function that takes three parameter, currentUserId, OtherUserId, and ImageUrl. In this function, I will create a new ArrayList<>() of Chat class called mchat. I will the make a reference to the "Chats" FirebaseInstance and call the addValueeventListener

In the listener, I will clear the mchat and the datsnapshot children and assign it ot a new Chat class.

I will the check if the current user is the receiver and the sender is the other user id OR if the receiver is the other user id and the current user is the sender. If true, I will add the chat to the mchat and update the message adapter and set the recycler to the message adapter.

I will call the readMessage function on a Database reference to the "Users" child.

I am going to create a ChatsFragment that will display users the current users has chats with.



## Delivered and Seen status

For this feature, I will add a ValueEventListener called seenListener in the MessageActivity and create a seenMessage function that takes a String userid parameter.

In this function, I will make a database reference to "Chats" and add assign seenListener to the reference.addValueEventListener. In the onDataChanged method, I will run a for loop of datasnapshots of the children and create a new chat from the snapshot.getValue().

I will then check if the chat.getReceiver equals the current user and the chat.getSender equals the user id. If true, I will create an Hashmap<String, Object> and put("isseen", true) and updateChildren with the hashMap.

In the sendMessage function I will ('isseen', false) to the hashmap. I will also call reference.removeEventListener(seenListener) to stop listening will the activity is on Pause. Back in the Chat class, I will add a boolean of isseen to the Chat parameter.

To visualize this, in the onBindViewHolder of the MessageAdapter, I will check if the position is the last message, then set the text view to "Seen", else "Delivered"

## Online and Offline Status

In the User class, I will add status as a parameter, and in the ProfileActivity, which is the activity with the Chats, Users, and Profile fragments, I will create a status function which will have a single String parameter of status.

In this function I intend to get a Database reference to the current user in the "Users" child. I will then create an Hashmap<String, Object> and put("status", status). After that, I will updateChildres of the database referencewith the hashmap.

In this Profile Activity, I will override the onResume method and call the status("online") and onPause i will call status("offline").

In the UserAdapter, I will create a boolean ischat add this as a parameter to the UserAdapter.

In the onBindViewHolder of the UserAdapter, I am going to check if ischat is true, if true, I will check if the user.getStatus() equals "online", the I will update the online image in my layout.xml file to be visible else I will the offine image to be visible.

Back in the UsersFragment, I will add a false argument in the UserAdapter so the online status is not visible there and in the ChatsFragment I will add a true argument instead.

## Instant Notifications


For the notifications, I intend to use 3 libraries, retrofit, retrofit gson converter and firebase messaging. As this is different from the Chat, i will create a new Package called Notifications and create 5 classes, a Token, Data, MyResponse, Sender, and a Client class.

In the client class create a getClient method with returns a Retrofit, and create a new retrofit if the retrofit is null.

I intend to use FirebaseIdService to refresh the token, but after some research I learnt FirebaseIdService id depracted so I will research more on using the new FirebaseMesssaging. to update the token, I intend to reference the Firebase instance and create a child("Tokens"), this is where I will store the tokens.

I will call the updateToken function in the ChatsFragment, where I will create a new token and save the token for the specific user.

I will also create an APIService interface with headers of content-type and authorization key. To get the key, I will activate Firebase cloud messaging and get the server key.

In this interface I will create a POST method and sendNotification(), the sendNotification will will take a Sender body. After researching more about FirebaseMessaging, I intend to use it send the notifications. to send the notification, I will use my custom app icon, a "New Message" title and the body of the message.

In the MessageActivity, I will call the APIService interface and make it call Client.getClient(). i will then define sendNotification() with parameters of receiver, username, and message. In this function I will make a call to the "Tokens" child in the database and get the token.

I will then make a Query to make I sure I get the right token for the right user. I will can an addValueeventListener to the query and get the token and create a new Data, with the current user, icon image, and message body.

After that, I will call create a new Sender that takes the data, and the gotten token and call apiService.sendNotification(sender). I will then check if the resonse code is 200, if true, check if the respond.body().success is equal 1, if true too, then I will make a Toast notifying the user that the attempt to send notification failed.

Right at the top of MessageActivity, I will make a boolean notify = false and set notify = false below where I called sendNotification.\

After researching and implement the FirebaseMessaging, the notification should but there is a know issue with notifications not working with Oreo devices which I also plan on fixing.


## Add reactions to message

For the reactions, I plan to implement a design library called pgreze android reactions. In the onBindViewHolder of the MessageActivity, I will create a new ReactionsConfig with all the reactions images as the arguments.

After that, I will create a new ReactionPopup, then right where I bind the sent message, I will set a new onTouchListener to the sent message and onTouch I will call popup.onTouch with the event as the argument. I will do the same where I bind the received message.

In the ReactionPopUp I will bind the reaction to the goten reaction and set the Visibilty to VISIBLE.

I will then make a database reference and set the feeling to the selected feeling.

## Search and Message any user off the app by name

In the UsersFragment, I will access the EditText I will create for searching users and add and addTextChangedListener to it. on the onTextChanged method, I will call a searchUsers function which takes a String parameter and pass an argument of charSequence.toString().

I will then define the searchUsers function. In the function I will make use of a query which is assigned to the Users child. I will add an addValueEventlistener to query and onDatachange, I will call clear the list of users.

After that I will run a for loop of DataSnapshot and create a user for each datasnapshot. Then, I will assert user and current user is not null to prevent NullException error. I will then check if the user.getId() is equals to the current user. If true, I will add the user to the mUsers list. I will the create a UserAdapter and set the adapter to the recycler view.

Right before I clear the list of users in onDataChanged I will check if the search box is empty.

The searching may work but an issue may arrise if the user searches in lowercase and the searched user is in Uppercase. To fix this, I will sure to convert the charSequence.toString() to lowercase and and make sure in my query I am searching for the lowercase version of the usernames saved in my database. To save a lowercase version I will go right where I saved other info such as name, id, imageUrl and call hashmap.put("search", username.toLowercase) and add this lower case version to my User class.