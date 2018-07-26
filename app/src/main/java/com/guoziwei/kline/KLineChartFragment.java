package com.guoziwei.kline;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guoziwei.klinelib.chart.ChartInfoView;
import com.guoziwei.klinelib.chart.KLineView;
import com.guoziwei.klinelib.model.HisData;

import java.util.List;


public class KLineChartFragment extends Fragment {


    private KLineView mKLineView;
    private int mDay;

    public KLineChartFragment() {
        // Required empty public constructor
    }

    public static KLineChartFragment newInstance(int day) {
        KLineChartFragment fragment = new KLineChartFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("day", day);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments().getInt("day");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_kline_chart_, container, false);
        mKLineView = v.findViewById(R.id.kline);
        title(v);
        click(v);

        mKLineView.setDateFormat("yyyy-MM-dd");
        int count = 121;
        mKLineView.setCount(count, count, 36);

        initData();
        return v;
    }

    private void title(View v) {
        View title = v.findViewById(R.id.rl_title);
        v.findViewById(R.id.back).setOnClickListener(v1 -> Log.e("kevin", "back"));
//        ((ChartInfoView) mKLineView.findViewById(R.id.k_info)).setTitle(title);
        ChartInfoView k_info = v.findViewById(R.id.k_info);
        mKLineView.setTitle(title, k_info);
    }

    protected void initData() {
        final List<HisData> hisData = Util.getK(getContext(), mDay);
        mKLineView.initData(hisData);
        mKLineView.setLastClose(hisData.get(0).getClose());
    }

    public void click(View v) {
        TextView sma = v.findViewById(R.id.tv_sma);
        TextView ema = v.findViewById(R.id.tv_ema);
        TextView boll = v.findViewById(R.id.tv_boll);
        TextView vol = v.findViewById(R.id.tv_vols);
        TextView macd = v.findViewById(R.id.tv_macd);
        TextView kdj = v.findViewById(R.id.tv_kdj);

        sma.setOnClickListener(v1 -> {
            setColor(ema, boll);
            sma.setTextColor(getResources().getColor(R.color.dodgerBlue));
        });
        ema.setOnClickListener(v1 -> {
            setColor(sma, boll);
            ema.setTextColor(getResources().getColor(R.color.dodgerBlue));
        });
        boll.setOnClickListener(v1 -> {
            setColor(sma, ema);
            boll.setTextColor(getResources().getColor(R.color.dodgerBlue));
        });
        vol.setOnClickListener(v1 -> {
            mKLineView.showVolume();
            setColor(macd, kdj);
            vol.setTextColor(getResources().getColor(R.color.dodgerBlue));
        });
        macd.setOnClickListener(v1 -> {
            mKLineView.showMacd();
            setColor(vol, kdj);
            macd.setTextColor(getResources().getColor(R.color.dodgerBlue));
        });
        kdj.setOnClickListener(v1 -> {
            mKLineView.showKdj();
            setColor(vol, macd);
            kdj.setTextColor(getResources().getColor(R.color.dodgerBlue));
        });
    }

    public void setColor(TextView... views) {
        Resources resources = getResources();
        for (TextView view : views) {
            view.setTextColor(resources.getColor(R.color.lightGreyBlue));
        }
    }
}
