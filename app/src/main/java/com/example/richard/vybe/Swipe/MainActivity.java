package com.example.richard.vybe.Swipe;


import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.richard.vybe.R;


public class MainActivity extends FragmentActivity {

    FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager mViewPager;

    private String TAG = "MainActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "STARTED MAIN ACTIVITY");

        mViewPager = (ViewPager) findViewById(R.id.pager);
//        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager());
        Log.d(TAG, "ADAPTER VALUE " + fragmentPagerAdapter);
        mViewPager.setAdapter(fragmentPagerAdapter);


    }


}
