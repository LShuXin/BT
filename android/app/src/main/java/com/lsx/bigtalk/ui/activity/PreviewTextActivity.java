package com.lsx.bigtalk.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;

public class PreviewTextActivity extends Activity {
    TextView txtContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_text_activity);

        txtContent = findViewById(R.id.content);

        String displayText = getIntent().getStringExtra(IntentConstant.PREVIEW_TEXT_CONTENT);
        txtContent.setText(displayText);

        ((View) txtContent.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewTextActivity.this.finish();
            }
        });
    }

}
