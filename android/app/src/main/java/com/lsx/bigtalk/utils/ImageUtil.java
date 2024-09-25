package com.lsx.bigtalk.utils;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.ui.helper.PhotoHelper;


public class ImageUtil {
    private static final Logger logger = Logger.getLogger(ImageUtil.class);

	public static Bitmap getBigBitmapForDisplay(String imagePath, Context context) {
		if (null == imagePath || !new File(imagePath).exists()) {
			return null;
		}

		try {
			int degree = PhotoHelper.readPictureDegree(imagePath);
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			if (bitmap == null) {
				return null;
			}

			DisplayMetrics dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
			float scale = bitmap.getWidth() / (float) dm.widthPixels;
			Bitmap newBitMap = null;
			if (scale > 1) {
				newBitMap = zoomBitmap(bitmap, (int) (bitmap.getWidth() / scale), (int) (bitmap.getHeight() / scale));
				bitmap.recycle();
                return PhotoHelper.rotaingImageView(degree, newBitMap);
			}
            return PhotoHelper.rotaingImageView(degree, bitmap);
		} catch (Exception e) {
			logger.e(e.getMessage());
			return null;
		}
	}

	private static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		if (null == bitmap) {
			return null;
		}
		try {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) width / w);
			float scaleHeight = ((float) height / h);
			matrix.postScale(scaleWidth, scaleHeight);
            return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		} catch (Exception e) {
			logger.e(e.getMessage());
			return null;
		}
	}
}
