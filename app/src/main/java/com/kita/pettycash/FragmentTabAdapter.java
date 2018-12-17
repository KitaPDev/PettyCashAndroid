package com.kita.pettycash;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class FragmentTabAdapter extends FragmentPagerAdapter {
    private final List<Fragment> m_lsFragments = new ArrayList<>();
    private final List<String> m_lsFragmentTitles = new ArrayList<>();

    FragmentTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) { return m_lsFragments.get(position); }

    public void addFragment(Fragment fragment, String title) {
        m_lsFragments.add(fragment);
        m_lsFragmentTitles.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return m_lsFragmentTitles.get(position);
    }

    @Override
    public int getCount() {
        return m_lsFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}