package xc.LEDILove.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by xcgd on 2018/4/3.
 */

public class SelectorViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    public SelectorViewPagerAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments!=null ? fragments.size():0;
    }
}
