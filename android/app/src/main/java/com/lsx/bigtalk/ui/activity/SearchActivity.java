package com.lsx.bigtalk.ui.activity;

import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.imservice.manager.IMStackManager;
import com.lsx.bigtalk.ui.base.BTBaseFragmentActivity;


public class SearchActivity extends BTBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMStackManager.getStackManager().pushActivity(this);
        setContentView(R.layout.search_fragment_activity);
    }

    @Override
    protected void onDestroy() {
        IMStackManager.getStackManager().popActivity(this);
        super.onDestroy();
    }

}
