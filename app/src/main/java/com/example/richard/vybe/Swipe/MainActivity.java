package com.example.richard.vybe.Swipe;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.richard.vybe.Home.HomeFragment;
import com.example.richard.vybe.Overview.OverviewFragment;
import com.example.richard.vybe.R;
import com.example.richard.vybe.Sentiment.SentimentFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends FragmentActivity {

    private String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;

    public double derivedSentiment;

    private ViewPager2 viewPager2;
    private FragmentStateAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);



        viewPager2 = findViewById(R.id.pager);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        pageAdapter = new ScreenSlidePageAdapter(this);
        viewPager2.setAdapter(pageAdapter);
        viewPager2.setPageTransformer(new ZoomOutPageTransformer());

        int startPage = getIntent().getIntExtra("page_number", 0);
        derivedSentiment = getIntent().getDoubleExtra("sentiment", 0);
        Log.i(TAG, "Sentiment: " + derivedSentiment);
        viewPager2.setCurrentItem(startPage);
        bottomNavigationView.setSelectedItemId(R.id.miSwipe);

        setViewPagerListener();
        setBottomNavigationView();

    }

    private void setBottomNavigationView(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.miHome){
                    viewPager2.setCurrentItem(0);

                    return true;
                }
                if (item.getItemId()==R.id.miAddMood){
                    viewPager2.setCurrentItem(1);

                    return true;
                }
                if (item.getItemId()==R.id.miSwipe){
                    viewPager2.setCurrentItem(2);
                    return true;
                }

                return true;
            }
        });


    }

    private void setViewPagerListener(){
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position){
                if (position==0){
                    bottomNavigationView.setSelectedItemId(R.id.miHome);
                }
                if (position==1){
                    bottomNavigationView.setSelectedItemId(R.id.miAddMood);
                }
                if (position==2){
                    bottomNavigationView.setSelectedItemId(R.id.miSwipe);
                }

            }
        });
    }

    private class ScreenSlidePageAdapter extends FragmentStateAdapter{
        public ScreenSlidePageAdapter(MainActivity mainActivity){super(mainActivity);}


        @NonNull
        @Override
        public Fragment createFragment(int position){
            Fragment fragment;
            switch (position){
                case 0:
                    fragment = new HomeFragment();
                    break;
                case 1:
                    fragment = new SentimentFragment();
                    break;
                case 2:
                    fragment = new SwipeFragment();
                    break;
                case 3:
                    fragment = new OverviewFragment();
                    break;
                default:
                    fragment = new SwipeFragment();
                    break;
            }
            return fragment;
        }
        @Override
        public int getItemCount(){return 4;}
    }

    private class ZoomOutPageTransformer implements ViewPager2.PageTransformer{
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position){
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1){
                view.setAlpha(0f);
            }else if (position <= 1){
                float scaleFactor = Math.max(MIN_SCALE,1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0){
                    view.setTranslationX(horzMargin - vertMargin / 2);
                }else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)/ (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            }   else {
                view.setAlpha(0f);
            }
        }
    }

}
