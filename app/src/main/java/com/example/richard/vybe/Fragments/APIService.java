package com.example.richard.vybe.Fragments;

import com.example.richard.vybe.Notifications.MyResponse;
import com.example.richard.vybe.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAti-Mm1Y:APA91bHPRjY_DUsB6DkASJ4mzp0JLe-mbpXSlRo9tx-XA3bLl_nCX5jy9u8iWyv0fhoodT2C4vn-btPQYI48hRr-sN8ifKJoNfmK_elurIJDbD86eNdO0jCuVNYGQ90Dn1Y01IXrJWeV"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
