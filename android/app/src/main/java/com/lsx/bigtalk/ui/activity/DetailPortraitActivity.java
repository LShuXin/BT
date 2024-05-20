package com.lsx.bigtalk.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.utils.IMUIHelper;
import com.lsx.bigtalk.utils.Logger;
import com.lsx.bigtalk.ui.widget.ZoomableImageView;

public class DetailPortraitActivity extends Activity  {

	private final Logger logger = Logger.getLogger(DetailPortraitActivity.class);
    public static String imageUri = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_portrait_activity);
		
		Intent intent = getIntent();
		if (intent == null) {
			logger.e("detailPortrait#displayimage#null intent");
			return;
		}

		String resUri = intent.getStringExtra(IntentConstant.KEY_AVATAR_URL);
        imageUri = resUri;
		logger.d("detailPortrait#displayimage#resUri:%s", resUri);

		boolean isContactAvatar = intent.getBooleanExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, false);
		logger.d("displayimage#isContactAvatar:%s", isContactAvatar);

		final ZoomableImageView portraitView = findViewById(R.id.detail_portrait);


		if (portraitView == null) {
			logger.e("detailPortrait#displayimage#portraitView is null");
			return;
		}

		logger.d("detailPortrait#displayimage#going to load the detail portrait");


		if (isContactAvatar) {
			IMUIHelper.setEntityImageViewAvatarNoDefaultPortrait(portraitView, resUri, DBConstant.SESSION_TYPE_SINGLE, 0);
		} else {
			IMUIHelper.displayImageNoOptions(portraitView, resUri, -1, 0);
		}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                portraitView.setFinishActivity(new finishActivity() {
                    @Override
                    public void finish() {
                        if(DetailPortraitActivity.this!=null)
                        {
                            DetailPortraitActivity.this.finish();
                            overridePendingTransition(
                                    R.anim.stay_y, R.anim.image_right_exit);
                        }
                    }
                });
            }
        },500);

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    public interface finishActivity{
        void finish();
    }

}
