package com.lsx.bigtalk.ui.fragment;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.service.entity.ImageMessageEntity;
import com.lsx.bigtalk.service.service.IMService;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.polites.android.GestureImageView;


public class MessageImageFragment extends Fragment {
    private View curView = null;
    protected GestureImageView view;
    protected GestureImageView newView;
    private ImageMessageEntity messageInfo = null;
    private ProgressBar mProgressbar = null;
    private FrameLayout parentLayout = null;
    private IMService imService;

    public void setImService(IMService service) {
        this.imService = service;
    }

    public void setImageInfo(ImageMessageEntity imageInfo) {
        messageInfo = imageInfo;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        try {
            if (null != curView) {
                if (null != curView.getParent()) {
                    ((ViewGroup) curView.getParent()).removeView(curView);
                }
            }
            curView = inflater.inflate(R.layout.image_message_fragment, null);
            initRes(curView);
            initData();
            return curView;
        } catch (Exception e) {
            return null;
        }
    }

    private void initRes(View curView) {
        try {
            view = curView.findViewById(R.id.image);
            newView = curView.findViewById(R.id.new_image);
            parentLayout = curView.findViewById(R.id.layout);
            mProgressbar = curView.findViewById(R.id.progress_bar);
            mProgressbar.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
            newView.setVisibility(View.GONE);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentLayout.performClick();
                }
            });
            parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (isAdded()) {
                        getActivity().finish();
                        getActivity().overridePendingTransition(
                                R.anim.stay_y, R.anim.image_right_exit);
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private void initData() {
        try {

            //@ZJ 破图的展示
            String imageUrl = messageInfo.getUrl();
            if (!TextUtils.isEmpty(messageInfo.getPath()) && FileUtil.isFileExist(messageInfo.getPath())) {
                imageUrl = "file://" + messageInfo.getPath();
            }

            ImageLoaderUtil.getImageLoaderInstance().displayImage(imageUrl, view, new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .showImageOnLoading(R.drawable.bg_default_message2)
                    .showImageOnFail(R.drawable.image_image_message_error)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnFail(R.drawable.image_image_message_error)
                    .resetViewBeforeLoading(true)
                    .build(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    closeProgressDialog(loadedImage, true);
                }

                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    closeProgressDialog(null, true);
                }
            });
        } catch (Exception e) {
        }
    }

    private void closeProgressDialog(Bitmap bitmap, boolean hideProgress) {
        try {
            if (isAdded()) {
                if (hideProgress) {
                    mProgressbar.setVisibility(View.GONE);
                }
                if (null == bitmap) {
                    return;
                }
                view.setVisibility(View.GONE);
                newView.setVisibility(View.VISIBLE);
                newView.setImageBitmap(bitmap);
                newView.setClickable(true);
                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentLayout.performClick();
                    }
                });
            }
        } catch (Exception e) {
        }
    }


}