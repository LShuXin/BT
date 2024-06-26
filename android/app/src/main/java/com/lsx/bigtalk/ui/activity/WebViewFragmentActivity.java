package com.lsx.bigtalk.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.ui.base.BTBaseFragmentActivity;
import com.lsx.bigtalk.ui.fragment.WebViewFragment;

public class WebViewFragmentActivity extends BTBaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(IntentConstant.WEBVIEW_URL)) {
            WebViewFragment.setUrl(intent.getStringExtra(IntentConstant.WEBVIEW_URL));
        }
        setContentView(R.layout.webview_fragment_activity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
