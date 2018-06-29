package com.guoziwei.kline.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.guoziwei.kline.FullScreenChartActivity;
import com.guoziwei.kline.R;
import com.guoziwei.klinelib.util.DisplayUtils;

import java.lang.ref.WeakReference;

public class HoursWindow extends PopupWindow {

    private final View mView;
    private final WeakReference<Activity> mContext;
    private IHoursListener onSelectHours;

    public HoursWindow(FullScreenChartActivity context, String[] data) {
        super(context);
        setSelectHoursClick(context);
        mContext = new WeakReference<>(context);

        mView = View.inflate(context, R.layout.p_hours, null);

        configPopUp();

        bindView(data);
    }

    private void bindView(String[] data) {
        TextView hour = mView.findViewById(R.id.p_hour);
        TextView twoHours = mView.findViewById(R.id.p_2hours);
        TextView fourHours = mView.findViewById(R.id.p_4hours);
        TextView sixHours = mView.findViewById(R.id.p_6hours);
        TextView twelveHours = mView.findViewById(R.id.p_12hours);

        TextView[] views = {hour, twoHours, fourHours, sixHours, twelveHours};
        for (int i = 0; i < data.length; i++)
            views[i].setText(data[i]);

        hour.setOnClickListener(v -> {
            dismiss();
            onSelectHours.onSelectHoursClick(1);
        });
        twoHours.setOnClickListener(v -> {
            dismiss();
            onSelectHours.onSelectHoursClick(2);
        });
        fourHours.setOnClickListener(v -> {
            dismiss();
            onSelectHours.onSelectHoursClick(4);
        });
        sixHours.setOnClickListener(v -> {
            dismiss();
            onSelectHours.onSelectHoursClick(6);
        });
        twelveHours.setOnClickListener(v -> {
            dismiss();
            onSelectHours.onSelectHoursClick(12);
        });
    }

    private void configPopUp() {
        setContentView(mView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(DisplayUtils.dip2px(mContext.get(), 36));
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0));
        setAnimationStyle(0);
    }

    private void setSelectHoursClick(IHoursListener listener) {
        this.onSelectHours = listener;
    }

    public interface IHoursListener {
        void onSelectHoursClick(int selected);
    }
}