package com.lsx.bigtalk.app;

import android.app.Application;
import android.content.Intent;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.lsx.bigtalk.utils.Logger;


public class IMApplication extends Application {

	private final Logger logger = Logger.getLogger(IMApplication.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		logger.i("IMApplication onCreate");
		startIMService();
		ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
	}

	private void startIMService() {
		logger.i("IMApplication startIMService");
		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

    public static boolean gifRunning = true;//gif是否运行
}
