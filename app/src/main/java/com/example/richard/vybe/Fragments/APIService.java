package com.example.richard.vybe.Fragments;

import com.example.richard.vybe.BuildConfig;
import com.example.richard.vybe.Notifications.MyResponse;
import com.example.richard.vybe.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    String FIREBASE_KEY = BuildConfig.FIREBASE_KEY;
    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=" + FIREBASE_KEY
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}