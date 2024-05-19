
package com.lsx.bigtalk.ui.activity;

import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.base.TTBaseFragmentActivity;

/** 聊天页面 */
public class ChatFragmentActivity extends TTBaseFragmentActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_fragment_activity_chat);
    }
}
