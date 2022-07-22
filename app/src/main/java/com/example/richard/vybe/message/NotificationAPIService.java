package com.example.richard.vybe.message;

import com.example.richard.vybe.BuildConfig;
import com.example.richard.vybe.message.notifications.FirebaseResponse;
import com.example.richard.vybe.message.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationAPIService {

    String FIREBASE_KEY = BuildConfig.FIREBASE_KEY;
    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=" + FIREBASE_KEY
            }
    )

    @POST("fcm/send")
    Call<FirebaseResponse> sendNotification(@Body Sender body);
}