package com.guoziwei.kline;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guoziwei.klinelib.chart.TimeLineView;
import com.guoziwei.klinelib.chart.list.BusinessAdapter;
import com.guoziwei.klinelib.chart.list.BusinessBean;
import com.guoziwei.klinelib.model.HisData;

import java.util.ArrayList;
import java.util.List;


public class TimeLineChartFragment extends Fragment {


    private TimeLineView mTimeLineView;
    private int mType;

    public TimeLineChartFragment() {
        // Required empty public constructor
    }

    public static TimeLineChartFragment newInstance(int type) {
        TimeLineChartFragment fragment = new TimeLineChartFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt("type");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mTimeLineView = new TimeLineView(getContext());
        mTimeLineView.findViewById(R.id.back).setOnClickListener(this::back);
        recycler();

        mTimeLineView.setDateFormat("HH:mm");
        int count = 121;
        mTimeLineView.setCount(count, count, 36);
        initData();
        return mTimeLineView;
    }

    protected void initData() {
        final List<HisData> hisData = Util.get1Day(getContext());
        mTimeLineView.setLastClose(hisData.get(0).getClose());
        mTimeLineView.initData(hisData);
    }

    private void back(View view) {
        Log.e("kevin", "back");
    }

    public void recycler() {
        RecyclerView recyclerIn = mTimeLineView.findViewById(com.guoziwei.klinelib.R.id.recyclerIn);
        RecyclerView recyclerOut = mTimeLineView.findViewById(com.guoziwei.klinelib.R.id.recyclerOut);

        configureAdapter(recyclerIn, true);
        configureAdapter(recyclerOut, false);
    }

    private void configureAdapter(RecyclerView recyclerView, boolean type) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<BusinessBean> businessBeans = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            BusinessBean bean = new BusinessBean();
            bean.price = 0.7668;
            bean.number = 11655.38;
            businessBeans.add(i, bean);
        }
        BusinessAdapter adapter = new BusinessAdapter(com.guoziwei.klinelib.R.layout.business, businessBeans, type, getResources());
        recyclerView.setAdapter(adapter);
    }
}
