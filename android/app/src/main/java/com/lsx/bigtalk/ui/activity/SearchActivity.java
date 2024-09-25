package com.lsx.bigtalk.ui.activity;

import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.StackManager;
import com.lsx.bigtalk.ui.base.BTBaseFragmentActivity;


public class SearchActivity extends BTBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StackManager.getStackManager().pushActivity(this);
        setContentView(R.layout.search_fragment_activity);
    }

    @Override
    protected void onDestroy() {
        StackManager.getStackManager().popActivity(this);
        super.onDestroy();
    }

}
