package com.lsx.bigtalk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.utils.Logger;
import com.lsx.bigtalk.ui.widget.ZoomableImageView;


public class ImagePreviewActivity extends Activity  {
	private final Logger logger = Logger.getLogger(ImagePreviewActivity.class);
    public static String imageUrl = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_preview_activity);
		
		Intent intent = getIntent();
		if (intent == null) {
			logger.e("intent is null");
			return;
		}

		imageUrl = intent.getStringExtra(IntentConstant.KEY_AVATAR_URL);
		logger.d("imageUrl:%s", imageUrl);

		boolean isAvatar = intent.getBooleanExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, false);
		logger.d("isAvatar:%s", isAvatar);

		final ZoomableImageView portraitView = findViewById(R.id.detail_portrait);
		if (portraitView == null) {
			logger.e("detailPortrait#displayimage#portraitView is null");
			return;
		}

		logger.d("detailPortrait#displayimage#going to load the detail portrait");


		if (isAvatar) {
			IMUIHelper.setEntityImageViewAvatarNoDefaultPortrait(portraitView, imageUrl, DBConstant.SESSION_TYPE_SINGLE, 0);
		} else {
			IMUIHelper.displayImageNoOptions(portraitView, imageUrl, -1, 0);
		}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                portraitView.setFinishActivity(new finishActivity() {
                    @Override
                    public void finish() {
                        ImagePreviewActivity.this.finish();
                        overridePendingTransition(
                                R.anim.stay_y, R.anim.image_right_exit);
                    }
                });
            }
        },500);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    public interface finishActivity {
        void finish();
    }

}
