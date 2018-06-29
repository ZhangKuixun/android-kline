package com.guoziwei.klinelib.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.guoziwei.klinelib.R;
import com.guoziwei.klinelib.model.HisData;
import com.guoziwei.klinelib.util.DataUtils;
import com.guoziwei.klinelib.util.DateUtils;
import com.guoziwei.klinelib.util.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * kline
 * Created by guoziwei on 2017/10/26.
 */
public class TimeLineView extends BaseView implements CoupleChartGestureListener.OnAxisChangeListener {


    public static final int NORMAL_LINE = 0;

    public static final int NORMAL_LINE_5DAY = 5;
    /**
     * average line
     */
    public static final int AVE_LINE = 1;
    /**
     * hide line
     */
    public static final int INVISIABLE_LINE = 6;

    protected CustomLineChart mChartPrice;//内容
    protected CustomCombinedChart mChartVolume;//底部

    protected ChartInfoView mChartInfoView;//选中后的提示信息

    /**
     * 最后价格
     */
    private double mLastPrice;

    /**
     * 昨日收盘价
     */
    private double mLastClose;
    private View mTitle;

    public TimeLineView(Context context) {
        this(context, null);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_timeline_, this);
        mChartPrice = findViewById(R.id.price_chart);
        mChartVolume = findViewById(R.id.vol_chart);
        mTitle = findViewById(R.id.rl_title);
        mChartInfoView = findViewById(R.id.line_info);
        mChartInfoView.setTitle(mTitle);
        mChartInfoView.setChart(mChartPrice, mChartVolume);

        mChartPrice.setNoDataText(context.getString(R.string.loading));
        initChartPrice();//内容
        initBottomChart(mChartVolume);//底部柱状图
        setOffset();//对齐两张表
        initChartListener();//手势监听
    }

    protected void initChartPrice() {
        mChartPrice.setBorderWidth(1);
        mChartPrice.setDrawBorders(true);
        mChartPrice.setDragEnabled(true);
        mChartPrice.setScaleEnabled(true);
        mChartPrice.setScaleYEnabled(false);
        mChartPrice.getLegend().setEnabled(false);
        mChartPrice.setAutoScaleMinMaxEnabled(false);
        mChartPrice.setDragDecelerationEnabled(false);
        mChartPrice.getDescription().setEnabled(false);
        mChartPrice.setBorderColor(getResources().getColor(R.color.silver));

        //marker
        LineChartXMarkerView mvx = new LineChartXMarkerView(getContext(), mData);
        mvx.setChartView(mChartPrice);
        mChartPrice.setXMarker(mvx);
        LineChartYMarkerView mv = new LineChartYMarkerView(getContext(), 2);
        mv.setChartView(mChartPrice);
        mChartPrice.setMarker(mv);

        //x axis
        XAxis xAxisPrice = mChartPrice.getXAxis();
        xAxisPrice.setDrawLabels(true);
        xAxisPrice.setAxisMinimum(-0.5f);
        xAxisPrice.setDrawAxisLine(false);
        xAxisPrice.setDrawGridLines(false);
        xAxisPrice.setAvoidFirstLastClipping(true);
        xAxisPrice.setLabelCount(7, true);
        xAxisPrice.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisPrice.setTextColor(getResources().getColor(R.color.coolGrey));
        xAxisPrice.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (mData.isEmpty())
                    return "";

                if (value < 0)
                    value = 0;

                if (value < mData.size())
                    return DateUtils.formatDate(mData.get((int) value).getDate(), mDateFormat);

                return "";
            }
        });

        //left y
        YAxis yAxis = mChartPrice.getAxisLeft();
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawLabels(false);

        //right y
        YAxis yAxisRight = mChartPrice.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawLabels(false);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initChartListener() {
        //滚动
        mChartPrice.setOnChartGestureListener(new CoupleChartGestureListener(this, mChartPrice, mChartVolume));
        mChartVolume.setOnChartGestureListener(new CoupleChartGestureListener(this, mChartVolume, mChartPrice));
        //缩放
        mChartPrice.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle,mChartInfoView, mChartVolume));
        mChartVolume.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle,mChartInfoView, mChartPrice));
        //点击
        mChartPrice.setOnTouchListener(new ChartInfoViewHandler(mChartPrice));
        mChartVolume.setOnTouchListener(new ChartInfoViewHandler(mChartVolume));
    }


    public void initData(List<HisData> hisDatas) {

        mData.clear();
        mData.addAll(DataUtils.calculateHisData(hisDatas));//计算数据
        mChartPrice.setRealCount(mData.size());

        ArrayList<Entry> priceEntries = new ArrayList<>(INIT_COUNT);//价格
        ArrayList<Entry> aveEntries = new ArrayList<>(INIT_COUNT);//均价
        ArrayList<Entry> paddingEntries = new ArrayList<>(INIT_COUNT);

        for (int i = 0; i < mData.size(); i++) {
            priceEntries.add(new Entry(i, (float) mData.get(i).getClose()));
            aveEntries.add(new Entry(i, (float) mData.get(i).getAvePrice()));
        }
        if (!mData.isEmpty() && mData.size() < MAX_COUNT) {
            for (int i = mData.size(); i < MAX_COUNT; i++) {
                paddingEntries.add(new Entry(i, (float) mData.get(mData.size() - 1).getClose()));
            }
        }
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setLine(NORMAL_LINE, priceEntries));
        sets.add(setLine(AVE_LINE, aveEntries));
        sets.add(setLine(INVISIABLE_LINE, paddingEntries));
        LineData lineData = new LineData(sets);

        mChartPrice.setData(lineData);

        mChartPrice.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartPrice.notifyDataSetChanged();
        moveToLast(mChartPrice);
        initChartVolumeData();

        mChartPrice.getXAxis().setAxisMaximum(lineData.getXMax() + 0.5f);
        mChartVolume.getXAxis().setAxisMaximum(mChartVolume.getData().getXMax() + 0.5f);

        mChartPrice.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);
        mChartVolume.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);

        setDescription(mChartVolume, "成交量 " + getLastData().getVol());
    }


    private BarDataSet setBar(ArrayList<BarEntry> barEntries, int type) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "vol");
        barDataSet.setHighLightAlpha(120);
        barDataSet.setHighLightColor(getResources().getColor(R.color.darkBlueGrey));
        barDataSet.setDrawValues(false);
        barDataSet.setVisible(type != INVISIABLE_LINE);
        barDataSet.setHighlightEnabled(type != INVISIABLE_LINE);
        barDataSet.setColors(getResources().getColor(R.color.paleRed), getResources().getColor(R.color.topaz));//设置每条柱子的颜色
        return barDataSet;
    }


    @android.support.annotation.NonNull
    private LineDataSet setLine(int type, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setDrawValues(false);
        if (type == NORMAL_LINE) {
            lineDataSetMa.setDrawFilled(true);
            lineDataSetMa.setFillAlpha(25);
            lineDataSetMa.setFillColor(getResources().getColor(R.color.brightBlue10));
            lineDataSetMa.setColor(getResources().getColor(R.color.dodgerBlue));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighLightColor(getResources().getColor(R.color.darkBlueGrey));
        } else if (type == NORMAL_LINE_5DAY) {
            lineDataSetMa.setColor(getResources().getColor(R.color.normal_line_color));
            lineDataSetMa.setCircleColor(mTransparentColor);
        } else if (type == AVE_LINE) {
            lineDataSetMa.setColor(getResources().getColor(R.color.orangeyYellow));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else {
            lineDataSetMa.setVisible(false);
            lineDataSetMa.setHighlightEnabled(false);
        }
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);//使得 DataSet 对应指定轴，进行绘制。
        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setCircleRadius(1f);

        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setDrawCircleHole(false);

        return lineDataSetMa;
    }


    private void initChartVolumeData() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<BarEntry> paddingEntries = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            HisData t = mData.get(i);
            barEntries.add(new BarEntry(i, (float) t.getVol(), t));
        }
        int maxCount = MAX_COUNT;
        if (!mData.isEmpty() && mData.size() < maxCount) {
            for (int i = mData.size(); i < maxCount; i++) {
                paddingEntries.add(new BarEntry(i, 0));
            }
        }

        BarData barData = new BarData(setBar(barEntries, NORMAL_LINE), setBar(paddingEntries, INVISIABLE_LINE));
        barData.setBarWidth(0.75f);
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        mChartVolume.setData(combinedData);

        mChartVolume.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartVolume.notifyDataSetChanged();
        mChartVolume.moveViewToX(combinedData.getEntryCount());//将当前视口的左侧移动到指定的X位置。

    }


    /**
     * 刷新最后一个点的价格（不增加数据）
     */
    public void refreshData(float price) {
        if (price <= 0 || price == mLastPrice) {
            return;
        }
        mLastPrice = price;
        LineData lineData = mChartPrice.getData();
//        if (data == null)
//            return;
//        LineData lineData = data.getLineData();
        if (lineData != null) {
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set.removeLast()) {
                set.addEntry(new Entry(set.getEntryCount(), price));
            }
        }

        mChartPrice.notifyDataSetChanged();
        mChartPrice.invalidate();
    }

    /**
     * 图表末尾增加一个数据
     */
    public void addData(HisData hisData) {
        hisData = DataUtils.calculateHisData(hisData, mData);
//        CombinedData combinedData = mChartPrice.getData();
//        LineData priceData = combinedData.getLineData();
        LineData priceData = mChartPrice.getData();
        ILineDataSet priceSet = priceData.getDataSetByIndex(0);
        ILineDataSet aveSet = priceData.getDataSetByIndex(1);
        IBarDataSet volSet = mChartVolume.getData().getBarData().getDataSetByIndex(0);
        if (mData.contains(hisData)) {
            int index = mData.indexOf(hisData);
            priceSet.removeEntry(index);
            aveSet.removeEntry(index);
            volSet.removeEntry(index);
            mData.remove(index);
        }

        mData.add(hisData);
        mChartPrice.setRealCount(mData.size());

        priceSet.addEntry(new Entry(priceSet.getEntryCount(), (float) hisData.getClose()));
        aveSet.addEntry(new Entry(aveSet.getEntryCount(), (float) hisData.getAvePrice()));
        volSet.addEntry(new BarEntry(volSet.getEntryCount(), hisData.getVol(), hisData));

        mChartPrice.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartVolume.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartPrice.getXAxis().setAxisMaximum(priceData.getXMax() + 1.5f);
        mChartVolume.getXAxis().setAxisMaximum(mChartVolume.getData().getXMax() + 1.5f);

        mChartPrice.notifyDataSetChanged();
        mChartPrice.invalidate();
        mChartVolume.notifyDataSetChanged();
        mChartVolume.invalidate();

        setDescription(mChartVolume, "成交量 " + hisData.getVol());
    }


    /**
     * 对齐两张图表
     */
    private void setOffset() {
        mChartPrice.setViewPortOffsets(0, 0, 0, DisplayUtils.dip2px(getContext(), 15));
        mChartVolume.setViewPortOffsets(0, 0, 0, 0);
//        int chartHeight = getResources().getDimensionPixelSize(R.dimen.bottom_chart_height);
//        mChartPrice.setViewPortOffsets(0, 0, 0, chartHeight);
//        mChartVolume.setViewPortOffsets(0, 0, 0, DisplayUtils.dip2px(getContext(), 20));

//        float lineLeft = mChartPrice.getViewPortHandler().offsetLeft();
//        float barLeft = mChartVolume.getViewPortHandler().offsetLeft();
//        float lineRight = mChartPrice.getViewPortHandler().offsetRight();
//        float barRight = mChartVolume.getViewPortHandler().offsetRight();
//        float offsetLeft, offsetRight;
//        if (barLeft < lineLeft) {
//            offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
//            mChartVolume.setExtraLeftOffset(offsetLeft);
//        } else {
//            offsetLeft = Utils.convertPixelsToDp(barLeft - lineLeft);
//            mChartPrice.setExtraLeftOffset(offsetLeft);
//        }
//        if (barRight < lineRight) {
//            offsetRight = Utils.convertPixelsToDp(lineRight);
//            mChartVolume.setExtraRightOffset(offsetRight);
//        } else {
//            offsetRight = Utils.convertPixelsToDp(barRight);
//            mChartPrice.setExtraRightOffset(offsetRight);
//        }
    }

    /**
     * 添加基准线
     */
    public void setLimitLine(double lastClose) {
        LimitLine limitLine = new LimitLine((float) lastClose);
        limitLine.enableDashedLine(5, 10, 0);
        limitLine.setLineColor(getResources().getColor(R.color.silver));
        mChartPrice.getAxisLeft().addLimitLine(limitLine);
    }

    public void setLimitLine() {
        setLimitLine(mLastClose);
    }

    /**
     * 设置昨天的收盘价，用于计算涨跌幅的坐标
     */
    public void setLastClose(double lastClose) {
        mLastClose = lastClose;
        setLimitLine();
//        mChartPrice.setOnChartValueSelectedListener(new InfoViewListener(getContext(), mLastClose, mData, mChartInfoView, mChartVolume));
//        mChartVolume.setOnChartValueSelectedListener(new InfoViewListener(getContext(), mLastClose, mData, mChartInfoView, mChartPrice));
    }


    public HisData getLastData() {
        if (mData != null && !mData.isEmpty()) {
            return mData.get(mData.size() - 1);
        }
        return null;
    }


    @Override
    public void onAxisChange(BarLineChartBase chart) {
        float lowestVisibleX = chart.getLowestVisibleX();
        if (lowestVisibleX <= chart.getXAxis().getAxisMinimum())
            return;
        int maxX = (int) chart.getHighestVisibleX();
        int x = Math.min(maxX, mData.size() - 1);
        HisData hisData = mData.get(x < 0 ? 0 : x);
        setDescription(mChartVolume, "成交量 " + hisData.getVol());
    }

    //5天
    @SafeVarargs
    public final void initDatas(List<HisData>... hisDatas) {
        // 设置标签数量，并让标签居中显示
        XAxis xAxis = mChartVolume.getXAxis();
        xAxis.setLabelCount(hisDatas.length, false);
        xAxis.setAvoidFirstLastClipping(false);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(hisDatas[0].size());
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (mData.isEmpty()) {
                    return "";
                }
                if (value < 0) {
                    value = 0;
                }
                if (value < mData.size()) {
                    return DateUtils.formatDate(mData.get((int) value).getDate(), mDateFormat);
                }
                return "";
            }
        });
        mData.clear();
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        ArrayList<IBarDataSet> barSets = new ArrayList<>();

        for (List<HisData> hisData : hisDatas) {
            hisData = DataUtils.calculateHisData(hisData);
            ArrayList<Entry> priceEntries = new ArrayList<>(INIT_COUNT);
            ArrayList<Entry> aveEntries = new ArrayList<>(INIT_COUNT);
            ArrayList<Entry> paddingEntries = new ArrayList<>(INIT_COUNT);
            ArrayList<BarEntry> barPaddingEntries = new ArrayList<>(INIT_COUNT);
            ArrayList<BarEntry> barEntries = new ArrayList<>(INIT_COUNT);

            for (int i = 0; i < hisData.size(); i++) {
                HisData t = hisData.get(i);
                priceEntries.add(new Entry(i + mData.size(), (float) t.getClose()));
                aveEntries.add(new Entry(i + mData.size(), (float) t.getAvePrice()));
                barEntries.add(new BarEntry(i + mData.size(), (float) t.getVol(), t));
            }
            if (!hisData.isEmpty() && hisData.size() < INIT_COUNT / hisDatas.length) {
                for (int i = hisData.size(); i < INIT_COUNT / hisDatas.length; i++) {
                    paddingEntries.add(new Entry(i, (float) hisData.get(hisData.size() - 1).getClose()));
                    barPaddingEntries.add(new BarEntry(i, (float) hisData.get(hisData.size() - 1).getClose()));
                }
            }
            sets.add(setLine(NORMAL_LINE_5DAY, priceEntries));
            sets.add(setLine(AVE_LINE, aveEntries));
            sets.add(setLine(INVISIABLE_LINE, paddingEntries));
            barSets.add(setBar(barEntries, NORMAL_LINE));
            barSets.add(setBar(barPaddingEntries, INVISIABLE_LINE));
            barSets.add(setBar(barPaddingEntries, INVISIABLE_LINE));
            mData.addAll(hisData);
            mChartPrice.setRealCount(mData.size());
        }

        LineData lineData = new LineData(sets);

//        CombinedData combinedData = new CombinedData();
//        combinedData.setData(lineData);
        mChartPrice.setData(lineData);
        mChartPrice.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartPrice.notifyDataSetChanged();
//        mChartPrice.moveViewToX(combinedData.getEntryCount());
        moveToLast(mChartVolume);


        BarData barData = new BarData(barSets);
        barData.setBarWidth(0.75f);
        CombinedData combinedData2 = new CombinedData();
        combinedData2.setData(barData);
        mChartVolume.setData(combinedData2);
        mChartVolume.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartVolume.notifyDataSetChanged();
        mChartVolume.moveViewToX(combinedData2.getEntryCount());

        mChartPrice.getXAxis().setAxisMaximum(lineData.getXMax() + 0.5f);
        mChartVolume.getXAxis().setAxisMaximum(mChartVolume.getData().getXMax() + 0.5f);

        mChartPrice.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);
        mChartVolume.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);

        setDescription(mChartVolume, "成交量 " + getLastData().getVol());
    }

}
