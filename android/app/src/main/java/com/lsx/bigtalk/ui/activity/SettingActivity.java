package com.lsx.bigtalk.ui.activity;

import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.base.BTBaseFragmentActivity;


public class SettingActivity extends BTBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fragment_activity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
