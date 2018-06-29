package com.guoziwei.klinelib.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.guoziwei.klinelib.R;
import com.guoziwei.klinelib.model.HisData;
import com.guoziwei.klinelib.util.DateUtils;

import java.util.Locale;

/**
 * 分时图点击的信息
 * Created by guoziwei on 2017/9/25.
 */

public class LineChartInfoView extends ChartInfoView {

//    private TextView mTvPrice;
    private TextView mTvRange;
    private TextView mTvVol;
    private TextView mTvDate;

    public LineChartInfoView(Context context) {
        this(context, null);
    }

    public LineChartInfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_line_chart_info_, this);
        mTvDate = findViewById(R.id.tv_date);
//        mTvPrice = findViewById(R.id.tv_price);
        mTvRange = findViewById(R.id.tv_rangeN);
        mTvVol = findViewById(R.id.tv_volN);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setData(double lastClose, HisData data) {
        mTvDate.setText(DateUtils.formatTime(data.getDate()));
//        mTvPrice.setText(DoubleUtil.formatDecimal(data.getClose()));
//        mTvRange.setText(String.format(Locale.getDefault(), "%.2f%%", (data.getClose()- data.getOpen()) / data.getOpen() * 100));
        mTvRange.setText(String.format(Locale.getDefault(), "%.2f%%", (data.getClose() - lastClose) / lastClose * 100));
        mTvVol.setText(data.getVol() + "");
        removeCallbacks(mRunnable);
        postDelayed(mRunnable, 2000);
    }

}
