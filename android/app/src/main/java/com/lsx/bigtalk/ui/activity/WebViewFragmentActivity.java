package com.lsx.bigtalk.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.ui.base.TTBaseFragmentActivity;
import com.lsx.bigtalk.ui.fragment.WebviewFragment;

public class WebViewFragmentActivity extends TTBaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(IntentConstant.WEBVIEW_URL)) {
            WebviewFragment.setUrl(intent.getStringExtra(IntentConstant.WEBVIEW_URL));
        }
        setContentView(R.layout.tt_fragment_activity_webview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
