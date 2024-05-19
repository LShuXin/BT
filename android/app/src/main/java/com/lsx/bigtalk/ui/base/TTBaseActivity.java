
package com.lsx.bigtalk.ui.base;

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
 * @Description
 * @author Nana
 * @date 2014-4-10
 */
public abstract class BTBaseActivity extends Activity {
    protected ImageView topLeftBtn;
    protected ImageView topRightBtn;
    protected TextView topTitleTxt;
    protected TextView letTitleTxt;
    protected ViewGroup topBar;
    protected ViewGroup topContentView;
    protected LinearLayout baseRoot;
    protected float x1, y1, x2, y2 = 0;

    /**
     * 初始化页面基础布局，初始化对页面基础布局所涉及组件的引用
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        topContentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.tt_activity_base, null);
        topBar = topContentView.findViewById(R.id.topbar);
        topTitleTxt = topContentView.findViewById(R.id.base_activity_title);
        topLeftBtn = topContentView.findViewById(R.id.left_btn);
        topRightBtn = topContentView.findViewById(R.id.right_btn);
        letTitleTxt = topContentView.findViewById(R.id.left_txt);
        baseRoot = topContentView.findViewById(R.id.act_base_root);

        topTitleTxt.setVisibility(View.GONE);
        topRightBtn.setVisibility(View.GONE);
        letTitleTxt.setVisibility(View.GONE);
        topLeftBtn.setVisibility(View.GONE);

        setContentView(topContentView);
    }

    /**
     * 设置导航栏返回按钮的文案
     * */
    protected void setLeftText(String text) {
        if (null == text) {
            return;
        }
        letTitleTxt.setText(text);
        letTitleTxt.setVisibility(View.VISIBLE);
    }

    /**
     * 设置导航栏标题
     * */
    protected void setTitle(String title) {
        if (title == null) {
            return;
        }
        if (title.length() > 12) {
            title = title.substring(0, 11) + "...";
        }
        topTitleTxt.setText(title);
        topTitleTxt.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int id) {
        // ??
        String strTitle = getResources().getString(id);
        setTitle(strTitle);
    }

    /**
     * 设置导航栏返回按钮图片资源
     * */
    protected void setLeftButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topLeftBtn.setImageResource(resID);
        topLeftBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 设置导航栏右侧 action 按钮图片资源
     * */
    protected void setRightButton(int resID) {
        if (resID <= 0) {
            return;
        }

        topRightBtn.setImageResource(resID);
        topRightBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 设置导航栏背景
     * */
    protected void setTopBar(int resID) {
        if (resID <= 0) {
            return;
        }
        topBar.setBackgroundResource(resID);
    }
}
