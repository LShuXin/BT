package com.lsx.bigtalk.ui.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lsx.bigtalk.R;

/**
 * {@code @Description}
 * @author Nana
 * @date 2014-4-10
 */
public abstract class BTBaseActivity extends Activity {
    protected ViewGroup appBarLayout;
    protected LinearLayout appBarRoot;
    protected ViewGroup appBar;
    protected ImageView topLeftBtnImageView;
    protected TextView topLeftBtnTitleTextView;
    protected ImageView topRightBtnImageView;
    protected TextView topCenterTitleTextView;
    protected float x1, y1, x2, y2 = 0;

    /**
     * 初始化页面基础布局，初始化对页面基础布局所涉及组件的引用
     * */
    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        appBarLayout = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.app_bar, null);
        appBarRoot = appBarLayout.findViewById(R.id.app_bar_root);
        appBar = appBarLayout.findViewById(R.id.app_bar);

        topLeftBtnImageView = appBarLayout.findViewById(R.id.left_btn);
        topLeftBtnTitleTextView = appBarLayout.findViewById(R.id.left_txt);

        topCenterTitleTextView = appBarLayout.findViewById(R.id.base_activity_title);

        topRightBtnImageView = appBarLayout.findViewById(R.id.right_btn);

        topCenterTitleTextView.setVisibility(View.GONE);
        topRightBtnImageView.setVisibility(View.GONE);
        topLeftBtnTitleTextView.setVisibility(View.GONE);
        topLeftBtnImageView.setVisibility(View.GONE);

        setContentView(appBarLayout);
    }

    /**
     * 设置导航栏返回按钮的文案
     * */
    protected void setTopLeftBtnTitleText(String text) {
        if (null == text) {
            return;
        }
        topLeftBtnTitleTextView.setText(text);
        topLeftBtnTitleTextView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置导航栏标题
     * */
    protected void setTopCenterTitleTextText(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 12) {
            title = title.substring(0, 11) + "...";
        }
        topCenterTitleTextView.setText(title);
        topCenterTitleTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int id) {
        String strTitle = getResources().getString(id);
        setTitle(strTitle);
    }

    /**
     * 设置导航栏返回按钮图片资源
     * */
    protected void setTopLeftBtnImage(int resId) {
        if (resId <= 0) {
            return;
        }

        topLeftBtnImageView.setImageResource(resId);
        topLeftBtnImageView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置导航栏右侧 action 按钮图片资源
     * */
    protected void setTopRightBtnImage(int resId) {
        if (resId <= 0) {
            return;
        }

        topRightBtnImageView.setImageResource(resId);
        topRightBtnImageView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置导航栏背景
     * */
    protected void setAppBarImage(int resId) {
        if (resId <= 0) {
            return;
        }
        appBar.setBackgroundResource(resId);
    }
}
