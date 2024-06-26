package com.lsx.bigtalk.ui.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.lsx.bigtalk.utils.Logger;


public class ViewPageAdapter extends PagerAdapter {
    private final List<GridView> mListViews;
    private final Logger logger = Logger.getLogger(ViewPageAdapter.class);

    public ViewPageAdapter(List<GridView> mListViews) {
        this.mListViews = mListViews;
    }

    @Override
    public int getCount() {
        return mListViews.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        try {
            container.removeView(mListViews.get(position));
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        try {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
        return arg0 == arg1;
    }

}
