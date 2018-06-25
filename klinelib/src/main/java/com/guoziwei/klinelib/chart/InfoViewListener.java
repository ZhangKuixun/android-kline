package com.guoziwei.klinelib.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.guoziwei.klinelib.model.HisData;
import com.guoziwei.klinelib.util.DisplayUtils;

import java.util.List;

/**
 * Created by dell on 2017/9/28.
 * 多表联动缩放
 */
public class InfoViewListener implements OnChartValueSelectedListener {

    private List<HisData> mList;
    private double mLastClose;//昨日收盘价
    private ChartInfoView mInfoView;//选中后的提示信息
    private int mWidth;
    /**
     * 如果其他图表不为空，3秒后高亮将消失。
     */
    private Chart[] mOtherChart;

    public InfoViewListener(Context context, double lastClose, List<HisData> list, ChartInfoView infoView) {
        mWidth = DisplayUtils.getWidthHeight(context)[0];
        mLastClose = lastClose;
        mList = list;
        mInfoView = infoView;
    }

    public InfoViewListener(Context context, double lastClose, List<HisData> list, ChartInfoView infoView, Chart... otherChart) {
        mWidth = DisplayUtils.getWidthHeight(context)[0];
        mLastClose = lastClose;
        mList = list;
        mInfoView = infoView;
        mOtherChart = otherChart;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int x = (int) e.getX();
        if (x < mList.size()) {
            mInfoView.setVisibility(View.VISIBLE);
            mInfoView.setData(mLastClose, mList.get(x));
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInfoView.getLayoutParams();
        if (h.getXPx() < mWidth / 2) {// getXPx()高亮点的X坐标
            lp.gravity = Gravity.RIGHT;
        } else {
            lp.gravity = Gravity.LEFT;
        }
        mInfoView.setLayoutParams(lp);
        if (mOtherChart != null) {
            for (Chart aMOtherChart : mOtherChart) {
                aMOtherChart.highlightValues(new Highlight[]{new Highlight(h.getX(), Float.NaN, h.getDataSetIndex())});
            }
        }
    }

    @Override
    public void onNothingSelected() {
        mInfoView.setVisibility(View.GONE);
        if (mOtherChart != null) {
            for (int i = 0; i < mOtherChart.length; i++) {
                mOtherChart[i].highlightValues(null);
            }
        }
    }
}
