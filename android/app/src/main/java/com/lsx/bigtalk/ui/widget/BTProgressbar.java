package com.lsx.bigtalk.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.lsx.bigtalk.R;


public class BTProgressbar extends LinearLayout {
    private final ProgressBar mProgressBar;
    private final TextView mLoadingText;
    private final Button mRefreshButton;
    private boolean mbTextShow = true;

    public interface OnRefreshBtnListener {
        void onRefresh();
    }

    public BTProgressbar(Context context) {
        this(context, null);
    }

    public BTProgressbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.progress_bar_view, this, true);
        mProgressBar = findViewById(R.id.progress_bar);
        mLoadingText = findViewById(R.id.loading_text);
        mRefreshButton = findViewById(R.id.refresh_button);

        hideProgress();
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        if (mbTextShow) {
            mLoadingText.setVisibility(View.VISIBLE);
        }
        mRefreshButton.setVisibility(View.GONE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        mRefreshButton.setVisibility(View.GONE);
    }

    public void showRefreshBtn() {
        mRefreshButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
    }

    public void setRefreshBtnListener(final OnRefreshBtnListener listener) {
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                listener.onRefresh();
            }
        });
    }

    public void setShowText(boolean bShow) {
        mbTextShow = bShow;
    }

    public void setText(String text) {
        mLoadingText.setText(text);
    }
}
