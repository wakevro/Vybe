package com.example.richard.vybe;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.android.volley.toolbox.Volley;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

    }
}

