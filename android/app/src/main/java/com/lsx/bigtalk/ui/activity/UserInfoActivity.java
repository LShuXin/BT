package com.lsx.bigtalk.ui.activity;

import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.base.BTBaseFragmentActivity;

public class UserInfoActivity extends BTBaseFragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_fragment_activity);
    }
}
