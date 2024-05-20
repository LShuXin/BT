
package com.lsx.bigtalk.ui.activity;

import android.os.Bundle;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.base.BTBaseFragmentActivity;

/** 聊天页面 */
public class ChatFragmentActivity extends BTBaseFragmentActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_fragment_activity);
    }
}
