package com.guoziwei.klinelib.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.guoziwei.klinelib.R;

/**
 * Created by dell on 2017/6/22.
 */

public class CustomCombinedChart extends CombinedChart {

    private IMarker mXMarker;
    private float mYCenter;
    //绘图区域背景色
    private Paint mBackgroundPaint;

    public CustomCombinedChart(Context context) {
        this(context, null);
    }

    public CustomCombinedChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCombinedChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.WHITE);
    }

    @Override
    protected void init() {
        super.init();
        mRenderer = new CustomCombinedChartRenderer(this, mAnimator, mViewPortHandler);
    }

    public void setXMarker(IMarker marker) {
        mXMarker = marker;
    }

    @Override
    public void setData(CombinedData data) {
        try {
            super.setData(data);
        } catch (ClassCastException e) {
            // ignore
        }
        ((CustomCombinedChartRenderer) mRenderer).createRenderers();
        mRenderer.initBuffers();
    }

    @Override
    protected void drawMarkers(Canvas canvas) {
        if (mMarker == null || mXMarker == null || !isDrawMarkersEnabled() || !valuesToHighlight())
            return;

        for (Highlight highlight : mIndicesToHighlight) {

            IDataSet set = mData.getDataSetByIndex(highlight.getDataSetIndex());

            Entry e = mData.getEntryForHighlight(highlight);
            int entryIndex = set.getEntryIndex(e);

            // 确保条目不为空
            if (e == null || entryIndex > set.getEntryCount() * mAnimator.getPhaseX())
                continue;

            float[] pos = getMarkerPosition(highlight);

            // 检查界限
            if (!mViewPortHandler.isInBounds(pos[0], pos[1]))
                continue;

            // 回调更新内容
            mXMarker.refreshContent(e, highlight);
            mMarker.refreshContent(e, highlight);

            // draw marker
            MarkerView xMarker = mXMarker instanceof LineChartXMarkerView ? (LineChartXMarkerView) mXMarker : (KLineChartXMarkerView) mXMarker;
            LineChartYMarkerView yMarker = (LineChartYMarkerView) mMarker;
            int chartHeight = getResources().getDimensionPixelSize(R.dimen.bottom_chart_height_bottom_marge);
            mXMarker.draw(canvas, pos[0] - (xMarker.getMeasuredWidth() / 2), getMeasuredHeight() - chartHeight);
            mMarker.draw(canvas, 0, pos[1] - yMarker.getMeasuredHeight() / 2);
//            mMarker.draw(canvas, getMeasuredWidth() - yMarker.getMeasuredWidth() * 1.05f, pos[1] - yMarker.getMeasuredHeight() / 2);
        }
    }

    protected void drawDescription(Canvas c) {

        // check if description should be drawn
        if (mDescription != null && mDescription.isEnabled()) {

            MPPointF position = mDescription.getPosition();

            mDescPaint.setTypeface(mDescription.getTypeface());
            mDescPaint.setTextSize(mDescription.getTextSize());
            mDescPaint.setColor(mDescription.getTextColor());
            mDescPaint.setTextAlign(mDescription.getTextAlign());

            float x, y;

            // if no position specified, draw on default position
            if (position == null) {
                x = getWidth() - mViewPortHandler.offsetRight() - mDescription.getXOffset();
                y = mDescription.getTextSize() + mViewPortHandler.offsetTop() + mDescription.getYOffset();
            } else {
                x = position.x;
                y = position.y;
            }

            c.drawText(mDescription.getText(), x, y, mDescPaint);
        }
    }


    /**
     * 重写这两个方法，为了让开盘价和涨跌幅剧中显示
     * Performs auto scaling of the axis by recalculating the minimum and maximum y-values based on the entries currently in view.
     */
    protected void autoScale() {

        final float fromX = getLowestVisibleX();
        final float toX = getHighestVisibleX();

        mData.calcMinMaxY(fromX, toX);

        mXAxis.calculate(mData.getXMin(), mData.getXMax());

        // calculate axis range (min / max) according to provided data

        if (mAxisLeft.isEnabled()) {
            if (mYCenter == 0) {
                mAxisLeft.calculate(mData.getYMin(YAxis.AxisDependency.LEFT),
                        mData.getYMax(YAxis.AxisDependency.LEFT));
            } else {
                float yMin = mData.getYMin(YAxis.AxisDependency.LEFT);
                float yMax = mData.getYMax(YAxis.AxisDependency.LEFT);
                float interval = (float) Math.max(Math.abs(mYCenter - yMax), Math.abs(mYCenter - yMin));
                yMax = (float) Math.max(yMax, (mYCenter + interval));
                yMin = (float) Math.min(yMin, (mYCenter - interval));
                mAxisLeft.calculate(yMin, yMax);
            }
        }

        if (mAxisRight.isEnabled()) {
            if (mYCenter == 0) {
                mAxisRight.calculate(mData.getYMin(YAxis.AxisDependency.RIGHT),
                        mData.getYMax(YAxis.AxisDependency.RIGHT));
            } else {
                float yMin = mData.getYMin(YAxis.AxisDependency.RIGHT);
                float yMax = mData.getYMax(YAxis.AxisDependency.RIGHT);
                float interval = (float) Math.max(Math.abs(mYCenter - yMax), Math.abs(mYCenter - yMin));
                yMax = (float) Math.max(yMax, (mYCenter + interval));
                yMin = (float) Math.min(yMin, (mYCenter - interval));
                mAxisRight.calculate(yMin, yMax);
            }
        }

        calculateOffsets();
    }

    @Override
    protected void drawGridBackground(Canvas c) {
        super.drawGridBackground(c);
        c.drawRect(mViewPortHandler.getContentRect(), mBackgroundPaint);
    }

    /**
     * 重写这两个方法，为了让开盘价和涨跌幅剧中显示
     */
    @Override
    protected void calcMinMax() {

        mXAxis.calculate(mData.getXMin(), mData.getXMax());

        if (mYCenter == 0) {
            // calculate axis range (min / max) according to provided data
            mAxisLeft.calculate(mData.getYMin(YAxis.AxisDependency.LEFT), mData.getYMax(YAxis.AxisDependency.LEFT));
            mAxisRight.calculate(mData.getYMin(YAxis.AxisDependency.RIGHT), mData.getYMax(YAxis.AxisDependency
                    .RIGHT));
        } else {
            float yLMin = mData.getYMin(YAxis.AxisDependency.LEFT);
            float yLMax = mData.getYMax(YAxis.AxisDependency.LEFT);
            float interval = (float) Math.max(Math.abs(mYCenter - yLMax), Math.abs(mYCenter - yLMin));
            yLMax = (float) Math.max(yLMax, (mYCenter + interval));
            yLMin = (float) Math.min(yLMin, (mYCenter - interval));
            mAxisLeft.calculate(yLMin, yLMax);

            float yRMin = mData.getYMin(YAxis.AxisDependency.RIGHT);
            float yRMax = mData.getYMax(YAxis.AxisDependency.RIGHT);
            float rinterval = (float) Math.max(Math.abs(mYCenter - yRMax), Math.abs(mYCenter - yRMin));
            yRMax = (float) Math.max(yRMax, (mYCenter + rinterval));
            yRMin = (float) Math.min(yRMin, (mYCenter - rinterval));
            mAxisRight.calculate(yRMin, yRMax);
        }
    }

    /**
     * 设置图表中Y居中的值
     */
    public void setYCenter(float YCenter) {
        mYCenter = YCenter;
    }
}
