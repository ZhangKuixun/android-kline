package com.guoziwei.klinelib.chart;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Description;
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
        mDecreasingColor = ContextCompat.getColor(getContext(), R.color.topaz);
        mIncreasingColor = ContextCompat.getColor(getContext(), R.color.paleRed);
    }

    //底部柱状图
    protected void initBottomChart(CustomCombinedChart chart) {
        chart.setBorderWidth(1);
        chart.setDrawBorders(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setScaleYEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDragDecelerationEnabled(false);
        chart.setBorderColor(getResources().getColor(R.color.silver));

        //x
        XAxis xAxisVolume = chart.getXAxis();
        xAxisVolume.setDrawLabels(false);
        xAxisVolume.setAxisMinimum(-0.5f);
        xAxisVolume.setDrawAxisLine(false);
        xAxisVolume.setDrawGridLines(false);
        xAxisVolume.setAvoidFirstLastClipping(true);

        //left y
        YAxis axisLeftVolume = chart.getAxisLeft();
        axisLeftVolume.setSpaceTop(0f);
        axisLeftVolume.setSpaceBottom(0f);
        axisLeftVolume.setDrawAxisLine(false);
        axisLeftVolume.setDrawGridLines(false);

        // Left Y Renderer
        Transformer leftYTransformer = chart.getRendererLeftYAxis().getTransformer();
        ColorContentYAxisRenderer renderer = new ColorContentYAxisRenderer(chart.getViewPortHandler(), chart.getAxisLeft(), leftYTransformer);
        renderer.setLabelInContent(true);
        renderer.setUseDefaultLabelXOffset(false);
        chart.setRendererLeftYAxis(renderer);

        //right y
        YAxis axisRightVolume = chart.getAxisRight();
        axisRightVolume.setDrawLabels(false);
        axisRightVolume.setDrawAxisLine(false);
        axisRightVolume.setDrawGridLines(false);
    }

    protected void moveToLast(CustomCombinedChart chart) {
        if (mData.size() > INIT_COUNT)
            chart.moveViewToX(mData.size() - INIT_COUNT);
    }

    protected void moveToLast(CustomLineChart chart) {
        if (mData.size() > INIT_COUNT)
            chart.moveViewToX(mData.size() - INIT_COUNT);
    }

    /**
     * 设置图标的可见个数，分别是初始值，最大值，最小值。
     * 比如(100,300,50)就是开始的时候100个点，最小可以缩放到300个点，最大可以放大到50个点
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

    /**
     * 设置x轴时间的格式
     */
    public void setDateFormat(String mDateFormat) {
        this.mDateFormat = mDateFormat;
    }
}
