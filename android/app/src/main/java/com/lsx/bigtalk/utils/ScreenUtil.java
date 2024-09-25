package com.lsx.bigtalk.utils;

import android.content.Context;


public class ScreenUtil {
    private final Context mCtx;
    private static ScreenUtil instance;

    public static ScreenUtil instance(Context ctx) {
        synchronized (ScreenUtil.class) {
            if (null == instance) {
                instance = new ScreenUtil(ctx);
            }
            return instance;
        }
    }

    private ScreenUtil(Context ctx) {
        mCtx = ctx.getApplicationContext();
    }

    public int getScreenWidth() {
        return mCtx.getResources().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeight() {
        return mCtx.getResources().getDisplayMetrics().heightPixels;
    }

    private float getDensity(Context ctx) {
        return ctx.getResources().getDisplayMetrics().density;
    }

    public int dip2px(int dip) {
        float density = getDensity(mCtx);
        // + 0.5, why?
        return (int)(dip * density + 0.5);
    }

    public int px2dip(int px) {
        float density = getDensity(mCtx);
        return (int)((px - 0.5) / density);
    }

    public int getStatusBarHeight() {
        return 0;
    }
}