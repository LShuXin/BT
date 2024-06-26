package com.lsx.bigtalk.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


public class BubbleImageView extends AppCompatImageView {
    protected String imageUrl = null;
    protected boolean isAttachedOnWindow = false;
    protected ImageLoadingCallback imageLoadingCallback;


    public BubbleImageView(Context context) {
        super(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageLoadingCallback(ImageLoadingCallback callback) {
        this.imageLoadingCallback = callback;
    }

    public void setImageUrl(final String url) {
        this.imageUrl = url;
        if (isAttachedOnWindow) {
            if (!TextUtils.isEmpty(this.imageUrl)) {
                ImageAware imageAware = new ImageViewAware(this, false);
                ImageLoaderUtil.getImageLoaderInstance().displayImage(this.imageUrl, imageAware, new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .showImageOnLoading(R.drawable.default_message_image2)
                        .showImageOnFail(R.drawable.message_image_error)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .delayBeforeLoading(100)
                        .build(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        if (imageLoadingCallback != null) {
                            String cachePath = ImageLoaderUtil.getImageLoaderInstance().getDiskCache().get(imageUri).getPath();//这个路径其实已不再更新
                            imageLoadingCallback.onLoadingComplete(cachePath, view, loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (imageLoadingCallback != null) {
                            imageLoadingCallback.onLoadingStarted(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        if (imageLoadingCallback != null) {
                            imageLoadingCallback.onLoadingCanceled(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        if (imageLoadingCallback != null) {
                            imageLoadingCallback.onLoadingFailed(imageUri, view);
                        }
                    }
                });
            }
        } else {
            this.setImageResource(R.drawable.default_message_image2);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedOnWindow = true;
        setImageUrl(this.imageUrl);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedOnWindow = false;
        ImageLoaderUtil.getImageLoaderInstance().cancelDisplayTask(this);
    }

    public interface ImageLoadingCallback {
        void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

        void onLoadingStarted(String imageUri, View view);

        void onLoadingCanceled(String imageUri, View view);

        void onLoadingFailed(String imageUri, View view);
    }


}
