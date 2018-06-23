package com.guoziwei.klinelib.chart;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.highlight.Highlight;

/**
 * Created by dell on 2017/10/27.
 * 手势监听
 */

public class ChartInfoViewHandler implements View.OnTouchListener {

    private BarLineChartBase mChart;
    private final GestureDetector mDetector;

    private boolean mIsLongPress = false;

    public ChartInfoViewHandler(BarLineChartBase chart) {
        mChart = chart;
        //1、创建一个手势识别器;2、实现手势识别器回到接口-----在里面处理各个手势对应的代码
        mDetector = new GestureDetector(mChart.getContext(), new GestureDetector.SimpleOnGestureListener() {
            /**
             * 用户轻触触摸屏，尚未松开或拖动，由一个1个MotionEvent ACTION_DOWN触发
             * 注意和onDown()的区别，强调的是没有松开或者拖动的状态 (单击没有松开或者移动时候就触发此事件，再触发onLongPress事件)
             */
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                mIsLongPress = true;
                Highlight h = mChart.getHighlightByTouchPoint(e.getX(), e.getY());//通过触摸点获得亮点
                if (h != null) {
                    mChart.highlightValue(h, true);
                    mChart.disableScroll();
                }
            }

        });
    }

    /**
     * 3、将MotionEvent  事件传给GestureDetector处理
     * <p>
     * MotionEvent 可以来自OnTouchEvent(MotionEvent event) 或者来自OnTouchListener的onTouch(View view,MotionEvent event);
     * 这里使用来自OnTouchEvent(MotionEvent event)的效果是一样的
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            mIsLongPress = false;
        }
        if (mIsLongPress && event.getAction() == MotionEvent.ACTION_MOVE) {
            Highlight h = mChart.getHighlightByTouchPoint(event.getX(), event.getY());
            if (h != null) {
                mChart.highlightValue(h, true);
                mChart.disableScroll();
            }
            //将事件传递给了 GestureDetector  然后它内部将事件进行判断识别  调用监听器中的对应方法
            //最终实现事件的处理
            return true;
        }
        return false;
    }
}
