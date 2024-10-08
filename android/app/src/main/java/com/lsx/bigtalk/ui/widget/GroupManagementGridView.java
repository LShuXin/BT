package com.lsx.bigtalk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.GridView;

import com.lsx.bigtalk.utils.ScreenUtil;

/**
 * Created by zhujian on 15/1/19.
 */
public class GroupManagementGridView extends GridView {
    private final Context ctx;
    public GroupManagementGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }
    public GroupManagementGridView(Context context) {
        super(context);
        this.ctx = context;
    }
    public GroupManagementGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.ctx = context;
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metric);
        int height =metric.heightPixels;
        int expandSpec = MeasureSpec.makeMeasureSpec(
        height-ScreenUtil.instance(ctx).dip2px(250), MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
