package com.lsx.bigtalk.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.utils.CommonUtil;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by zhujian on 15/1/14.
 */
public class IMBaseImageView extends ImageView {

    protected String imageUrl=null;
    protected int imageId;
    protected boolean isAttachedOnWindow=false;
    protected String avatarAppend= null;
    protected int defaultImageRes = R.drawable.image_default_user_avatar;
    protected int corner=0;

    public IMBaseImageView(Context context) {
        super(context);
    }

    public IMBaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IMBaseImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAvatarAppend(String appendStr){
        this.avatarAppend=appendStr;
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
        if (isAttachedOnWindow){
            if (!TextUtils.isEmpty(this.imageUrl)&&this.imageUrl.equals(CommonUtil.matchUrl(this.imageUrl))) {
                if (!TextUtils.isEmpty(avatarAppend)){
                    if(! this.imageUrl.contains(avatarAppend)) {
                        this.imageUrl += avatarAppend;
                    }
                }
                ImageLoaderUtil.getImageLoaderInstance().displayImage(this.imageUrl, this, ImageLoaderUtil.getAvatarOptions(corner, defaultImageRes));
            }else{
                String defaultUri="drawable://" + defaultImageRes;
                if(imageId!=0)
                {
                     defaultUri="drawable://" + imageId;
                }
                ImageLoaderUtil.getImageLoaderInstance().displayImage(defaultUri, this, ImageLoaderUtil.getAvatarOptions(corner, defaultImageRes));
            }
        }
    }

    public void setImageId(int id) {
        this.imageId=id;
    }

    public void setDefaultImageRes(int defaultImageRes) {
        this.defaultImageRes = defaultImageRes;

    }

    public void setCorner(int corner) {
        this.corner = corner;
    }

    @Deprecated
    public void loadImage(String imageUrl,ImageSize imageSize,ImageLoaddingCallback imageLoaddingCallback){
        ImageLoaderUtil.getImageLoaderInstance().loadImage(imageUrl,imageSize,new ImageLoaddingListener(imageLoaddingCallback));
    }

    public int getCorner() {
        return corner;
    }

    public int getDefaultImageRes() {
        return defaultImageRes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedOnWindow = true;
        setImageUrl(this.imageUrl);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedOnWindow=false;
        ImageLoaderUtil.getImageLoaderInstance().cancelDisplayTask(this);
    }

    private static class ImageLoaddingListener extends SimpleImageLoadingListener {
        private final ImageLoaddingCallback imageLoaddingCallback;

        public ImageLoaddingListener(ImageLoaddingCallback callback) {
            this.imageLoaddingCallback=callback;
        }
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage){
            imageLoaddingCallback.onLoadingComplete(imageUri,view,loadedImage);
        }

    }

    public interface ImageLoaddingCallback {
        void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);
    }
}
