package com.lsx.bigtalk.app;

import android.app.Application;
import android.content.Intent;

import com.hjq.toast.Toaster;
import com.hjq.toast.style.WhiteToastStyle;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.storage.sp.BTSp;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.lsx.bigtalk.logs.Logger;


public class BTApplication extends Application {
	private final Logger logger = Logger.getLogger(BTApplication.class);

	public static void main(String[] args) {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	private void startIMService() {
		logger.i("IMApplication#startIMService");
		Intent intent = new Intent();
		intent.setClass(this, IMService.class);
		startService(intent);
	}

    public static boolean gifRunning = true;


	private void init() {
		Toaster.init(this, new WhiteToastStyle());
		FileUtil.init(getApplicationContext());
        try {
            BTSp.getInstance().init(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.i("IMApplication#onCreate");
		startIMService();
		ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
	}
}
