package com.lsx.bigtalk.ui.fragment;

import android.view.View;
import android.widget.ProgressBar;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.base.TTBaseFragment;

public abstract class MainFragment extends TTBaseFragment {
    private ProgressBar progressbar;

    public void init(View curView) {
        progressbar = curView.findViewById(R.id.progress_bar);
    }

    public void showProgressBar() {
        progressbar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressbar.setVisibility(View.GONE);
    }

}
