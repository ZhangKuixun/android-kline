package com.guoziwei.klinelib.chart.list;

import android.content.res.Resources;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.guoziwei.klinelib.R;

import java.util.List;


/**
 * Created by ${张奎勋} on 2018/6/29.
 * 分时：价格 and 数量
 */

public class BusinessAdapter extends BaseQuickAdapter<BusinessBean, BaseViewHolder> {

    private final boolean mIn;
    private final int mPaleRed;
    private final int mTopaz;
    private final int mDarkBlueGrey;

    public BusinessAdapter(int layout, List<BusinessBean> data, boolean in, Resources resources) {
        super(layout, data);
        mIn = in;
        mPaleRed = resources.getColor(R.color.paleRed);
        mTopaz = resources.getColor(R.color.topaz);
        mDarkBlueGrey = resources.getColor(R.color.darkBlueGrey);
    }

    @Override
    protected void convert(final BaseViewHolder holder, BusinessBean item) {
        holder.setText(R.id.price, String.valueOf(item.price));
        holder.setTextColor(R.id.number, mIn ? mPaleRed : mTopaz);

        holder.setText(R.id.number, String.valueOf(item.number));
        holder.setTextColor(R.id.number, mDarkBlueGrey);
    }
}
