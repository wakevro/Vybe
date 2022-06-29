package com.example.richard.vybe.Swipe;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {

    public FragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0:
                return new SwipeFragment();

            case 1  :
                return new OverviewFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
