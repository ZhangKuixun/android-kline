package com.guoziwei.klinelib.chart;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.utils.Transformer;
import com.guoziwei.klinelib.R;
import com.guoziwei.klinelib.model.HisData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/14 0014.
 */

class BaseView extends LinearLayout {

    protected String mDateFormat = "yyyy-MM-dd HH:mm";

    protected int mDecreasingColor;
    protected int mIncreasingColor;
    protected int mAxisColor;
    protected int mTransparentColor;


    public int MAX_COUNT = 150;
    public int MIN_COUNT = 10;
    public int INIT_COUNT = 80;

    protected List<HisData> mData = new ArrayList<>(300);

    public BaseView(Context context) {
        this(context, null);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAxisColor = ContextCompat.getColor(getContext(), R.color.axis_color);
        mTransparentColor = ContextCompat.getColor(getContext(), android.R.color.transparent);
        mDecreasingColor = ContextCompat.getColor(getContext(), R.color.decreasing_color);
        mIncreasingColor = ContextCompat.getColor(getContext(), R.color.increasing_color);
    }

    //底部柱状图
    protected void initBottomChart(AppCombinedChart chart) {
        chart.setScaleEnabled(true);//启用/禁用缩放图表上的两个轴。
        chart.setDrawBorders(true);//启用/禁用绘制图表边框（chart周围的线）
        chart.setBorderColor(getResources().getColor(R.color.silver));
        chart.setBorderWidth(1);
        Legend lineChartLegend = chart.getLegend();
        lineChartLegend.setEnabled(false);

        //volume x y轴
        XAxis xAxisVolume = chart.getXAxis();
        xAxisVolume.setDrawLabels(false);
        xAxisVolume.setDrawAxisLine(false);
        xAxisVolume.setDrawGridLines(false);

        //左边y
        YAxis axisLeftVolume = chart.getAxisLeft();
        axisLeftVolume.setDrawGridLines(false);
        axisLeftVolume.setDrawAxisLine(false);
        axisLeftVolume.setSpaceBottom(0f);

        //右边y
        YAxis axisRightVolume = chart.getAxisRight();
        axisRightVolume.setDrawLabels(false);
        axisRightVolume.setDrawGridLines(false);
        axisRightVolume.setDrawAxisLine(false);
    }

    protected void moveToLast(AppCombinedChart chart) {
        if (mData.size() > INIT_COUNT) {
            chart.moveViewToX(mData.size() - INIT_COUNT);
        }
    }

    protected void moveToLast(AppLineChart chart) {
        if (mData.size() > INIT_COUNT) {
            chart.moveViewToX(mData.size() - INIT_COUNT);
        }
    }

    /**
     * set the count of k chart
     */
    public void setCount(int init, int max, int min) {
        INIT_COUNT = init;
        MAX_COUNT = max;
        MIN_COUNT = min;
    }

    protected void setDescription(Chart chart, String text) {
        Description description = chart.getDescription();
//        float dx = chart.getWidth() - chart.getViewPortHandler().offsetRight() - description.getXOffset();
//        description.setPosition(dx, description.getTextSize());
        description.setText(text);
    }

    public HisData getLastData() {
        if (mData != null && !mData.isEmpty()) {
            return mData.get(mData.size() - 1);
        }
        return null;
    }

    public void setDateFormat(String mDateFormat) {
        this.mDateFormat = mDateFormat;
    }
}
