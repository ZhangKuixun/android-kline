package com.guoziwei.klinelib.chart;

import android.annotation.SuppressLint;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.guoziwei.klinelib.model.HisData;

import java.util.List;

/**
 * Created by dell on 2017/9/28.
 * 多表联动缩放
 */
public class InfoViewListener implements OnChartValueSelectedListener {

    private final List<HisData> mList;
    private final double mLastClose;//昨日收盘价
    private final ChartInfoView mInfoView;//选中后的提示信息

    private View mTitle;
    private Chart[] mOtherChart;//如果其他图表不为空，2秒后高亮将消失。

    public InfoViewListener(double lastClose, List<HisData> list, ChartInfoView infoView) {
        mLastClose = lastClose;
        mList = list;
        mInfoView = infoView;
    }

    public InfoViewListener(double lastClose, List<HisData> list, View title, ChartInfoView infoView, Chart... otherChart) {
        mLastClose = lastClose;
        mList = list;
        mTitle = title;
        mInfoView = infoView;
        mOtherChart = otherChart;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int x = (int) e.getX();

        if (x < mList.size()) {

            mInfoView.setVisibility(View.VISIBLE);
            if (mTitle != null)
                mTitle.setVisibility(View.GONE);

            mInfoView.setData(mLastClose, mList.get(x));
        }

        if (mOtherChart != null)
            for (Chart aMOtherChart : mOtherChart)
                aMOtherChart.highlightValues(new Highlight[]{new Highlight(h.getX(), Float.NaN, h.getDataSetIndex())});
    }

    @Override
    public void onNothingSelected() {
        mInfoView.setVisibility(View.GONE);
        if (mTitle != null)
            mTitle.setVisibility(View.VISIBLE);

        if (mOtherChart != null)
            for (Chart aMOtherChart : mOtherChart)
                aMOtherChart.highlightValues(null);
    }
}
