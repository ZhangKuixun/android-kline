package com.guoziwei.kline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.guoziwei.kline.view.HoursWindow;

public class FullScreenChartActivity extends AppCompatActivity implements HoursWindow.IHoursListener, TabLayout.OnTabSelectedListener {

    private TabLayout mTabLayout;
    private String[] minutes;
    private String[] hours;

    public static void launch(Context context, int index) {
        Intent intent = new Intent(context, FullScreenChartActivity.class);
        intent.putExtra("index", index);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_chart);
        mTabLayout = findViewById(R.id.tab);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        minutes = new String[]{getString(R.string._1m), getString(R.string._5m), getString(R.string._15m), getString(R.string._30m)};
        hours = new String[]{getString(R.string._1), getString(R.string._2), getString(R.string._4), getString(R.string._6), getString(R.string._12)};
        String[] titles = new String[]{getString(R.string._timeK), getString(R.string._dayK), getString(R.string._weekK), getString(R.string._1), getString(R.string._1m)};

        Fragment[] fragments = {
                TimeLineChartFragment.newInstance(1),
                KLineChartFragment.newInstance(1),
                KLineChartFragment.newInstance(7),
                KLineChartFragment.newInstance(30),
                KLineChartFragment.newInstance(30)
        };
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), fragments, titles);
        viewPager.setOffscreenPageLimit(fragments.length);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(getIntent().getIntExtra("index", 3));

        mTabLayout.setupWithViewPager(viewPager);
        mTabLayout.addOnTabSelectedListener(this);
    }

    public void showHoursWindow(String[] data) {
        HoursWindow hoursWindow = new HoursWindow(FullScreenChartActivity.this, data);
        hoursWindow.showAtLocation(mTabLayout, Gravity.BOTTOM, 0, mTabLayout.getMeasuredHeight());
    }

    //小时选择／分钟选择
    @Override
    public void onSelectHoursClick(int selected) {

    }

    @Override
    protected void onDestroy() {
        mTabLayout.removeOnTabSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (tab.getPosition() == 3)
            showHoursWindow(hours);
        else if (tab.getPosition() == 4)
            showHoursWindow(minutes);
    }
}
