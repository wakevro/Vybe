package com.example.richard.vybe.Swipe;


import android.os.Bundle;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;

import com.example.richard.vybe.R;


public class MainActivity extends FragmentActivity {

    private String TAG = "MainActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


    }


}
