package com.guoziwei.klinelib.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Transformer;
import com.guoziwei.klinelib.R;
import com.guoziwei.klinelib.model.HisData;
import com.guoziwei.klinelib.util.DataUtils;
import com.guoziwei.klinelib.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * kline
 * Created by guoziwei on 2017/10/26.
 */
public class KLineView extends BaseView implements CoupleChartGestureListener.OnAxisChangeListener {


    public static final int NORMAL_LINE = 0;
    /**
     * average line
     */
    public static final int AVE_LINE = 1;
    /**
     * hide line
     */
    public static final int INVISIABLE_LINE = 6;

    public static final int MA5 = 5;
    public static final int MA10 = 10;
    public static final int MA20 = 20;
    public static final int MA30 = 30;

    public static final int K = 31;
    public static final int D = 32;
    public static final int J = 33;

    public static final int DIF = 34;
    public static final int DEA = 35;


    protected CustomCombinedChart mChartPrice;
    protected CustomCombinedChart mChartVolume;
    protected CustomCombinedChart mChartMacd;
    protected CustomCombinedChart mChartKdj;

    protected ChartInfoView mChartInfoView;

    /**
     * last price
     */
    private double mLastPrice;

    /**
     * 昨天收盘价格
     */
    private double mLastClose;
    private View mTitle;

    public KLineView(Context context) {
        this(context, null);
    }

    public KLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_kline_, this);
        mChartPrice = findViewById(R.id.price_chart);
        mChartVolume = findViewById(R.id.vol_chart);
        mChartMacd = findViewById(R.id.macd_chart);
        mChartKdj = findViewById(R.id.kdj_chart);
//        mChartInfoView = findViewById(R.id.k_info);
//        mChartInfoView.setChart(mChartPrice, mChartVolume, mChartMacd, mChartKdj);

        mChartPrice.setNoDataText(context.getString(R.string.loading));
        initChartPrice();
        initBottomChart(mChartVolume);
        initBottomChart(mChartMacd);
        initBottomChart(mChartKdj);
        setOffset();
        initChartListener();
    }

    public void showKdj() {
        mChartVolume.setVisibility(GONE);
        mChartMacd.setVisibility(GONE);
        mChartKdj.setVisibility(VISIBLE);
    }

    public void showMacd() {
        mChartVolume.setVisibility(GONE);
        mChartMacd.setVisibility(VISIBLE);
        mChartKdj.setVisibility(GONE);
    }

    public void showVolume() {
        mChartMacd.setVisibility(GONE);
        mChartKdj.setVisibility(GONE);
        mChartVolume.setVisibility(VISIBLE);
    }

    protected void initChartPrice() {
        mChartPrice.setBorderWidth(1);
        mChartPrice.setDrawBorders(true);
        mChartPrice.setDragEnabled(true);
        mChartPrice.setScaleEnabled(true);
        mChartPrice.setScaleYEnabled(false);
        mChartPrice.getLegend().setEnabled(false);
        mChartPrice.setAutoScaleMinMaxEnabled(true);
        mChartPrice.setDragDecelerationEnabled(false);
        mChartPrice.setBorderColor(getResources().getColor(R.color.silver));
        mChartPrice.setDrawGridBackground(true);

        //marker
        KLineChartXMarkerView mvx = new KLineChartXMarkerView(getContext(), mData);
        mvx.setChartView(mChartPrice);
        mChartPrice.setXMarker(mvx);
        LineChartYMarkerView mv = new LineChartYMarkerView(getContext(), 2);
        mv.setChartView(mChartPrice);
        mChartPrice.setMarker(mv);

        //x
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
        YAxis axisLeftPrice = mChartPrice.getAxisLeft();
        axisLeftPrice.setDrawGridLines(false);
        axisLeftPrice.setDrawAxisLine(false);
        axisLeftPrice.setDrawLabels(false);
        int[] colorArray = {mDecreasingColor, mDecreasingColor, mAxisColor, mIncreasingColor, mIncreasingColor};
        Transformer leftYTransformer = mChartPrice.getRendererLeftYAxis().getTransformer();
        ColorContentYAxisRenderer leftColorContentYAxisRenderer = new ColorContentYAxisRenderer(mChartPrice.getViewPortHandler(), mChartPrice.getAxisLeft(), leftYTransformer);
        leftColorContentYAxisRenderer.setLabelColor(colorArray);
        leftColorContentYAxisRenderer.setLabelInContent(true);
        leftColorContentYAxisRenderer.setUseDefaultLabelXOffset(false);
        mChartPrice.setRendererLeftYAxis(leftColorContentYAxisRenderer);

        //right y
        YAxis axisRightPrice = mChartPrice.getAxisRight();
        axisRightPrice.setDrawGridLines(false);
        axisRightPrice.setDrawAxisLine(false);
        axisRightPrice.setDrawLabels(false);
        Transformer rightYTransformer = mChartPrice.getRendererRightYAxis().getTransformer();
        ColorContentYAxisRenderer rightColorContentYAxisRenderer = new ColorContentYAxisRenderer(mChartPrice.getViewPortHandler(), mChartPrice.getAxisRight(), rightYTransformer);
        rightColorContentYAxisRenderer.setLabelInContent(true);
        rightColorContentYAxisRenderer.setUseDefaultLabelXOffset(false);
        rightColorContentYAxisRenderer.setLabelColor(colorArray);
        mChartPrice.setRendererRightYAxis(rightColorContentYAxisRenderer);
    }

    private void initChartListener() {
        mChartPrice.setOnChartGestureListener(new CoupleChartGestureListener(this, mChartPrice, mChartVolume, mChartMacd, mChartKdj));
        mChartVolume.setOnChartGestureListener(new CoupleChartGestureListener(this, mChartVolume, mChartPrice));
        mChartMacd.setOnChartGestureListener(new CoupleChartGestureListener(this, mChartMacd, mChartPrice));
        mChartKdj.setOnChartGestureListener(new CoupleChartGestureListener(this, mChartKdj, mChartPrice));
        mChartPrice.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartVolume, mChartMacd, mChartKdj));
//        mChartVolume.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartPrice));
//        mChartMacd.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartPrice));
//        mChartKdj.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartPrice));

        //不使用 Fragment 无法点击 bar 显示 marker
        mChartPrice.setOnTouchListener(new ChartInfoViewHandler(mChartPrice));
    }

    public void initData(List<HisData> hisDatas) {
        mData.clear();
        mData.addAll(DataUtils.calculateHisData(hisDatas));
        mChartPrice.setRealCount(mData.size());

        ArrayList<CandleEntry> lineCJEntries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> ma5Entries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> ma10Entries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> ma20Entries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> ma30Entries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> paddingEntries = new ArrayList<>(INIT_COUNT);

        for (int i = 0; i < mData.size(); i++) {
            HisData hisData = mData.get(i);
            lineCJEntries.add(new CandleEntry(i, (float) hisData.getHigh(), (float) hisData.getLow(), (float) hisData.getOpen(), (float) hisData.getClose()));

            if (!Double.isNaN(hisData.getMa5())) {
                ma5Entries.add(new Entry(i, (float) hisData.getMa5()));
            }

            if (!Double.isNaN(hisData.getMa10())) {
                ma10Entries.add(new Entry(i, (float) hisData.getMa10()));
            }

            if (!Double.isNaN(hisData.getMa20())) {
                ma20Entries.add(new Entry(i, (float) hisData.getMa20()));
            }

            if (!Double.isNaN(hisData.getMa30())) {
                ma30Entries.add(new Entry(i, (float) hisData.getMa30()));
            }
        }

        if (!mData.isEmpty() && mData.size() < MAX_COUNT)
            for (int i = mData.size(); i < MAX_COUNT; i++)
                paddingEntries.add(new Entry(i, (float) mData.get(mData.size() - 1).getClose()));

        LineData lineData = new LineData(
                setLine(INVISIABLE_LINE, paddingEntries),
                setLine(MA5, ma5Entries),
                setLine(MA10, ma10Entries),
                setLine(MA20, ma20Entries),
                setLine(MA30, ma30Entries));
        CandleData candleData = new CandleData(setKLine(NORMAL_LINE, lineCJEntries));
        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);
        combinedData.setData(candleData);
        mChartPrice.setData(combinedData);

        mChartPrice.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartPrice.notifyDataSetChanged();
        moveToLast(mChartPrice);
        initChartVolumeData();
        initChartMacdData();
        initChartKdjData();

        mChartPrice.getXAxis().setAxisMaximum(combinedData.getXMax() + 0.5f);
        mChartVolume.getXAxis().setAxisMaximum(mChartVolume.getData().getXMax() + 0.5f);
        mChartMacd.getXAxis().setAxisMaximum(mChartMacd.getData().getXMax() + 0.5f);
        mChartKdj.getXAxis().setAxisMaximum(mChartKdj.getData().getXMax() + 0.5f);

        mChartPrice.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);
        mChartVolume.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);
        mChartMacd.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);
        mChartKdj.zoom(MAX_COUNT * 1f / INIT_COUNT, 0, 0, 0);

        HisData hisData = getLastData();
        setDescription(mChartVolume, "成交量 " + hisData.getVol());
        setDescription(mChartPrice, String.format(Locale.getDefault(), "MA5:%.2f  MA10:%.2f  MA20:%.2f  MA30:%.2f",
                hisData.getMa5(), hisData.getMa10(), hisData.getMa20(), hisData.getMa30()));
        setDescription(mChartMacd, String.format(Locale.getDefault(), "MACD:%.2f  DEA:%.2f  DIF:%.2f",
                hisData.getMacd(), hisData.getDea(), hisData.getDif()));
        setDescription(mChartKdj, String.format(Locale.getDefault(), "K:%.2f  D:%.2f  J:%.2f",
                hisData.getK(), hisData.getD(), hisData.getJ()));
    }


    private BarDataSet setBar(ArrayList<BarEntry> barEntries, int type) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "vol");
        barDataSet.setHighLightColor(getResources().getColor(R.color.darkBlueGrey));
        barDataSet.setDrawValues(false);
        barDataSet.setVisible(type != INVISIABLE_LINE);
        barDataSet.setHighlightEnabled(type != INVISIABLE_LINE);
        barDataSet.setColors(getResources().getColor(R.color.paleRed), getResources().getColor(R.color.topaz));
        return barDataSet;
    }

    @android.support.annotation.NonNull
    private LineDataSet setLine(int type, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setDrawValues(false);
        if (type == NORMAL_LINE) {
            lineDataSetMa.setColor(getResources().getColor(R.color.normal_line_color));
            lineDataSetMa.setCircleColor(ContextCompat.getColor(getContext(), R.color.normal_line_color));
        } else if (type == K) {
            lineDataSetMa.setColor(getResources().getColor(R.color.k));
            lineDataSetMa.setCircleColor(mTransparentColor);
        } else if (type == D) {
            lineDataSetMa.setColor(getResources().getColor(R.color.d));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == J) {
            lineDataSetMa.setColor(getResources().getColor(R.color.j));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == DIF) {
            lineDataSetMa.setColor(getResources().getColor(R.color.dif));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == DEA) {
            lineDataSetMa.setColor(getResources().getColor(R.color.dea));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == AVE_LINE) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ave_color));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == MA5) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma5));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == MA10) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma10));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == MA20) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma20));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else if (type == MA30) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma30));
            lineDataSetMa.setCircleColor(mTransparentColor);
            lineDataSetMa.setHighlightEnabled(false);
        } else {
            lineDataSetMa.setVisible(false);
            lineDataSetMa.setHighlightEnabled(false);
        }
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setCircleRadius(1f);

        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setDrawCircleHole(false);

        lineDataSetMa.setHighLightColor(getResources().getColor(R.color.darkBlueGrey));

        return lineDataSetMa;
    }

    public CandleDataSet setKLine(int type, ArrayList<CandleEntry> lineEntries) {
        CandleDataSet set = new CandleDataSet(lineEntries, "KLine" + type);
        set.setDrawIcons(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowColor(Color.DKGRAY);
        set.setShadowWidth(0.75f);
        set.setDecreasingColor(mDecreasingColor);
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setShadowColorSameAsCandle(true);
        set.setIncreasingColor(mIncreasingColor);
        set.setIncreasingPaintStyle(Paint.Style.FILL);
        set.setHighLightColor(getResources().getColor(R.color.darkBlueGrey));
        set.setNeutralColor(ContextCompat.getColor(getContext(), R.color.paleRed));
        set.setDrawValues(true);
        set.setValueTextSize(10);
        set.setHighlightEnabled(true);
        set.setVisible(type == NORMAL_LINE);
        return set;
    }

    private void initChartVolumeData() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<BarEntry> paddingEntries = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            HisData t = mData.get(i);
            barEntries.add(new BarEntry(i, (float) t.getVol(), t));
        }
        int maxCount = MAX_COUNT;
        if (!mData.isEmpty() && mData.size() < maxCount)
            for (int i = mData.size(); i < maxCount; i++)
                paddingEntries.add(new BarEntry(i, 0));

        BarData barData = new BarData(setBar(barEntries, NORMAL_LINE), setBar(paddingEntries, INVISIABLE_LINE));
        barData.setBarWidth(0.75f);
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        mChartVolume.setData(combinedData);

        mChartVolume.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartVolume.notifyDataSetChanged();
//        mChartVolume.moveViewToX(combinedData.getEntryCount());
        moveToLast(mChartVolume);

    }

    private void initChartMacdData() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<BarEntry> paddingEntries = new ArrayList<>();
        ArrayList<Entry> difEntries = new ArrayList<>();
        ArrayList<Entry> deaEntries = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            HisData t = mData.get(i);
            barEntries.add(new BarEntry(i, (float) t.getMacd()));
            difEntries.add(new Entry(i, (float) t.getDif()));
            deaEntries.add(new Entry(i, (float) t.getDea()));
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
        LineData lineData = new LineData(setLine(DIF, difEntries), setLine(DEA, deaEntries));
        combinedData.setData(lineData);
        mChartMacd.setData(combinedData);

        mChartMacd.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartMacd.notifyDataSetChanged();
//        mChartMacd.moveViewToX(combinedData.getEntryCount());
        moveToLast(mChartMacd);
    }

    private void initChartKdjData() {
        ArrayList<Entry> kEntries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> dEntries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> jEntries = new ArrayList<>(INIT_COUNT);
        ArrayList<Entry> paddingEntries = new ArrayList<>(INIT_COUNT);

        for (int i = 0; i < mData.size(); i++) {
            kEntries.add(new Entry(i, (float) mData.get(i).getK()));
            dEntries.add(new Entry(i, (float) mData.get(i).getD()));
            jEntries.add(new Entry(i, (float) mData.get(i).getJ()));
        }
        if (!mData.isEmpty() && mData.size() < MAX_COUNT)
            for (int i = mData.size(); i < MAX_COUNT; i++)
                paddingEntries.add(new Entry(i, (float) mData.get(mData.size() - 1).getK()));


        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setLine(K, kEntries));
        sets.add(setLine(D, dEntries));
        sets.add(setLine(J, jEntries));
        sets.add(setLine(INVISIABLE_LINE, paddingEntries));
        LineData lineData = new LineData(sets);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);
        mChartKdj.setData(combinedData);

        mChartMacd.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartKdj.notifyDataSetChanged();
        moveToLast(mChartKdj);
    }


    /**
     * 刷新最后一个点的价格（不增加数据）
     */
    public void refreshData(float price) {
        if (price <= 0 || price == mLastPrice) {
            return;
        }
        mLastPrice = price;
        CombinedData data = mChartPrice.getData();
        if (data == null)
            return;
        LineData lineData = data.getLineData();
        if (lineData != null) {
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set.removeLast()) {
                set.addEntry(new Entry(set.getEntryCount(), price));
            }
        }
        CandleData candleData = data.getCandleData();
        if (candleData != null) {
            ICandleDataSet set = candleData.getDataSetByIndex(0);
            if (set.removeLast()) {
                HisData hisData = mData.get(mData.size() - 1);
                hisData.setClose(price);
                hisData.setHigh(Math.max(hisData.getHigh(), price));
                hisData.setLow(Math.min(hisData.getLow(), price));
                set.addEntry(new CandleEntry(set.getEntryCount(), (float) hisData.getHigh(), (float) hisData.getLow(), (float) hisData.getOpen(), price));

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
        CombinedData combinedData = mChartPrice.getData();
        LineData priceData = combinedData.getLineData();
        ILineDataSet padding = priceData.getDataSetByIndex(0);
        ILineDataSet ma5Set = priceData.getDataSetByIndex(1);
        ILineDataSet ma10Set = priceData.getDataSetByIndex(2);
        ILineDataSet ma20Set = priceData.getDataSetByIndex(3);
        ILineDataSet ma30Set = priceData.getDataSetByIndex(4);
        CandleData kData = combinedData.getCandleData();
        ICandleDataSet klineSet = kData.getDataSetByIndex(0);
        IBarDataSet volSet = mChartVolume.getData().getBarData().getDataSetByIndex(0);
        IBarDataSet macdSet = mChartMacd.getData().getBarData().getDataSetByIndex(0);
        ILineDataSet difSet = mChartMacd.getData().getLineData().getDataSetByIndex(0);
        ILineDataSet deaSet = mChartMacd.getData().getLineData().getDataSetByIndex(1);
        LineData kdjData = mChartKdj.getData().getLineData();
        ILineDataSet kSet = kdjData.getDataSetByIndex(0);
        ILineDataSet dSet = kdjData.getDataSetByIndex(1);
        ILineDataSet jSet = kdjData.getDataSetByIndex(2);

        if (mData.contains(hisData)) {
            int index = mData.indexOf(hisData);
            klineSet.removeEntry(index);
            padding.removeFirst();
            // ma比较特殊，entry数量和k线的不一致，移除最后一个
            ma5Set.removeLast();
            ma10Set.removeLast();
            ma20Set.removeLast();
            ma30Set.removeLast();
            volSet.removeEntry(index);
            macdSet.removeEntry(index);
            difSet.removeEntry(index);
            deaSet.removeEntry(index);
            kSet.removeEntry(index);
            dSet.removeEntry(index);
            jSet.removeEntry(index);
            mData.remove(index);
        }
        mData.add(hisData);
        mChartPrice.setRealCount(mData.size());
        int klineCount = klineSet.getEntryCount();
        klineSet.addEntry(new CandleEntry(klineCount, (float) hisData.getHigh(), (float) hisData.getLow(), (float) hisData.getOpen(), (float) hisData.getClose()));
        volSet.addEntry(new BarEntry(volSet.getEntryCount(), hisData.getVol(), hisData));

        macdSet.addEntry(new BarEntry(macdSet.getEntryCount(), (float) hisData.getMacd()));
        difSet.addEntry(new Entry(difSet.getEntryCount(), (float) hisData.getDif()));
        deaSet.addEntry(new Entry(deaSet.getEntryCount(), (float) hisData.getDea()));

        kSet.addEntry(new Entry(kSet.getEntryCount(), (float) hisData.getK()));
        dSet.addEntry(new Entry(dSet.getEntryCount(), (float) hisData.getD()));
        jSet.addEntry(new Entry(jSet.getEntryCount(), (float) hisData.getJ()));

        // 因为ma的数量会少，所以这里用kline的set数量作为x
        if (!Double.isNaN(hisData.getMa5())) {
            ma5Set.addEntry(new Entry(klineCount, (float) hisData.getMa5()));
        }
        if (!Double.isNaN(hisData.getMa10())) {
            ma10Set.addEntry(new Entry(klineCount, (float) hisData.getMa10()));
        }
        if (!Double.isNaN(hisData.getMa20())) {
            ma20Set.addEntry(new Entry(klineCount, (float) hisData.getMa20()));
        }
        if (!Double.isNaN(hisData.getMa30())) {
            ma30Set.addEntry(new Entry(klineCount, (float) hisData.getMa30()));
        }


        mChartPrice.getXAxis().setAxisMaximum(combinedData.getXMax() + 1.5f);
        mChartVolume.getXAxis().setAxisMaximum(mChartVolume.getData().getXMax() + 1.5f);
        mChartMacd.getXAxis().setAxisMaximum(mChartMacd.getData().getXMax() + 1.5f);
        mChartKdj.getXAxis().setAxisMaximum(mChartKdj.getData().getXMax() + 1.5f);


        mChartPrice.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartVolume.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartMacd.setVisibleXRange(MAX_COUNT, MIN_COUNT);
        mChartKdj.setVisibleXRange(MAX_COUNT, MIN_COUNT);

        mChartPrice.notifyDataSetChanged();
        mChartPrice.invalidate();
        mChartVolume.notifyDataSetChanged();
        mChartVolume.invalidate();
        mChartMacd.notifyDataSetChanged();
        mChartMacd.invalidate();
        mChartKdj.notifyDataSetChanged();
        mChartKdj.invalidate();


        setDescription(mChartPrice, String.format(Locale.getDefault(), "MA5:%.2f  MA10:%.2f  MA20:%.2f  MA30:%.2f",
                hisData.getMa5(), hisData.getMa10(), hisData.getMa20(), hisData.getMa30()));
        setDescription(mChartVolume, "成交量 " + hisData.getVol());
        setDescription(mChartMacd, String.format(Locale.getDefault(), "MACD:%.2f  DEA:%.2f  DIF:%.2f",
                hisData.getMacd(), hisData.getDea(), hisData.getDif()));
        setDescription(mChartKdj, String.format(Locale.getDefault(), "K:%.2f  D:%.2f  J:%.2f",
                hisData.getK(), hisData.getD(), hisData.getJ()));

    }

    /**
     * 对齐两张图表
     */
    private void setOffset() {
        int chartHeight = getResources().getDimensionPixelSize(R.dimen.bottom_chart_height_bottom_marge);
        mChartPrice.setViewPortOffsets(0, 0, 0, chartHeight);
        mChartVolume.setViewPortOffsets(0, 0, 0, 0);
        mChartMacd.setViewPortOffsets(0, 0, 0, 0);
        mChartKdj.setViewPortOffsets(0, 0, 0, 0);
    }

    /**
     * 添加基准线
     */
    public void setLimitLine(double lastClose) {
        LimitLine limitLine = new LimitLine((float) lastClose);
        limitLine.enableDashedLine(5, 10, 0);
        limitLine.setLineColor(getResources().getColor(R.color.limit_color));
        mChartPrice.getAxisLeft().addLimitLine(limitLine);
    }

    public void setLimitLine() {
        setLimitLine(mLastClose);
    }

    public void setLastClose(double lastClose) {
        mLastClose = lastClose;
        setLimitLine();
        mChartPrice.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartVolume, mChartMacd, mChartKdj));
        mChartVolume.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartPrice, mChartMacd, mChartKdj));
        mChartMacd.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartPrice,  mChartVolume, mChartKdj));
        mChartKdj.setOnChartValueSelectedListener(new InfoViewListener(mLastClose, mData, mTitle, mChartInfoView, mChartPrice,   mChartVolume, mChartMacd));
    }


    @Override
    public void onAxisChange(BarLineChartBase chart) {
        float lowestVisibleX = chart.getLowestVisibleX();
        if (lowestVisibleX <= chart.getXAxis().getAxisMinimum())
            return;
        int maxX = (int) chart.getHighestVisibleX();
        int x = Math.min(maxX, mData.size() - 1);
        HisData hisData = mData.get(x < 0 ? 0 : x);
        setDescription(mChartPrice, String.format(Locale.getDefault(), "MA5:%.2f  MA10:%.2f  MA20:%.2f  MA30:%.2f",
                hisData.getMa5(), hisData.getMa10(), hisData.getMa20(), hisData.getMa30()));
        setDescription(mChartVolume, "成交量 " + hisData.getVol());
        setDescription(mChartMacd, String.format(Locale.getDefault(), "MACD:%.2f  DEA:%.2f  DIF:%.2f",
                hisData.getMacd(), hisData.getDea(), hisData.getDif()));
        setDescription(mChartKdj, String.format(Locale.getDefault(), "K:%.2f  D:%.2f  J:%.2f",
                hisData.getK(), hisData.getD(), hisData.getJ()));

    }

    public void setTitle(View title, ChartInfoView k_info) {
        mTitle = title;
        mChartInfoView = k_info;
        mChartInfoView.setTitle(title);
        mChartInfoView.setChart(mChartPrice, mChartVolume, mChartMacd, mChartKdj);
    }
}
