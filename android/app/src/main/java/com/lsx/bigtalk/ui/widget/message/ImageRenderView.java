package com.lsx.bigtalk.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.service.entity.ImageMessageEntity;
import com.lsx.bigtalk.ui.widget.BubbleImageView;
import com.lsx.bigtalk.ui.widget.BTProgressbar;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.AppConstant;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
 *
 */
public class ImageRenderView extends BaseMsgRenderView {
    private final Logger logger = Logger.getLogger(ImageRenderView.class);

    // 上层必须实现的接口
    private ImageLoadListener imageLoadListener;
    private BtnImageListener btnImageListener;

    /** 可点击的view*/
    private View messageLayout;
    /**图片消息体*/
    private BubbleImageView messageImage;
    /** 图片状态指示*/
    private BTProgressbar imageProgress;

    public ImageRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static ImageRenderView inflater(Context context,ViewGroup viewGroup,boolean isMine){
        int resource = isMine?R.layout.mine_image_message_item_view :R.layout.others_image_message_item_view;
        ImageRenderView imageRenderView = (ImageRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        imageRenderView.setMine(isMine);
        imageRenderView.setParentView(viewGroup);
        return imageRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        messageLayout = findViewById(R.id.message_layout);
        messageImage = findViewById(R.id.message_image);
        imageProgress = findViewById(R.id.image_loading_progress);
        imageProgress.setShowText(false);
    }

    /**
     *
     * */

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     *
     * 对于mine。 图片send_success 就是成功了直接取地址
     * 对于sending  就是正在上传
     *
     * 对于other，消息一定是success，接受成功额
     * 2. 然后分析loadStatus 判断消息的展示状态
     */
    @Override
    public void render(final MessageEntity messageEntity,final UserEntity userEntity,Context ctx) {
        super.render(messageEntity, userEntity,ctx);
    }



    /**
     * 多端同步也不会拉到本地失败的数据
     * 只有isMine才有的状态，消息发送失败
     * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
     * 2. 图片上传成功，但是发送失败。 点击重新发送??
     * 3. 比较悲剧的是 图片上传失败和消息发送失败都是这个状态 不过可以通过另外一个状态来区别 图片load状态
     * @param entity
     */
    @Override
    public void msgFailure(final MessageEntity entity) {
        super.msgFailure(entity);
        messageImage.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                /**判断状态，重新发送resend*/
                btnImageListener.onMsgFailure();
            }
        });
        if(FileUtil.isFileExist(((ImageMessageEntity)entity).getPath()))
        {
            messageImage.setImageUrl("file://"+((ImageMessageEntity)entity).getPath());
        }
        else{
            messageImage.setImageUrl(((ImageMessageEntity)entity).getUrl());
        }
        imageProgress.hideProgress();
    }


    @Override
    public void msgStatusError(final MessageEntity entity) {
        super.msgStatusError(entity);
        imageProgress.hideProgress();
    }


    /**
     * 图片信息正在发送的过程中
     * 1. 上传图片
     * 2. 发送信息
     */
    @Override
    public void msgSending(final MessageEntity entity) {
        if(isMine())
        {
            if(FileUtil.isFileExist(((ImageMessageEntity)entity).getPath()))
            {

                messageImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                        imageProgress.hideProgress();
                    }
                });
                messageImage.setImageUrl("file://"+((ImageMessageEntity)entity).getPath());
            }
            else
            {
                //todo  文件不存在
            }
        }

    }


    /**
     * 消息成功
     * 1. 对方图片消息
     * 2. 自己多端同步的消息
     * 说明imageUrl不会为空的
     */
    @Override
    public void msgSuccess(final MessageEntity entity) {
        super.msgSuccess(entity);
        ImageMessageEntity imageMessage = (ImageMessageEntity)entity;
        final String imagePath = imageMessage.getPath();
        final String url = imageMessage.getUrl();
        int loadStatus = imageMessage.getLoadStatus();
        if(TextUtils.isEmpty(url)){
            /**消息状态异常*/
            msgStatusError(entity);
            return;
        }

        switch (loadStatus) {
            case AppConstant.MessageConstant.IMAGE_UNLOAD:{
                messageImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
                        if(imageLoadListener!=null)
                        {
                            imageLoadListener.onLoadComplete(imageUri);
                        }
                        getImageProgress().hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        getImageProgress().showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {
                        getImageProgress().hideProgress();
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                        getImageProgress().hideProgress();
                        imageLoadListener.onLoadFailed();
                    }
                });

                if(isMine())
                {
                    if(FileUtil.isFileExist(imagePath))
                    {
                        messageImage.setImageUrl("file://"+imagePath);
                    }
                    else
                    {
                        messageImage.setImageUrl(url);
                    }
                }
                else {
                    messageImage.setImageUrl(url);
                }
            }break;

            case AppConstant.MessageConstant.IMAGE_LOADING:{

            }break;

            case AppConstant.MessageConstant.IMAGE_LOADED_SUCCESS:{
                messageImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                        imageProgress.showProgress();
                    }
                });

                if(isMine())
                {
                    if(FileUtil.isFileExist(imagePath))
                    {
                        messageImage.setImageUrl("file://"+imagePath);
                    }
                    else
                    {
                        messageImage.setImageUrl(url);
                    }
                }
                else {
                    messageImage.setImageUrl(url);
                }
                messageImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(btnImageListener!=null)
                        {
                            btnImageListener.onMsgSuccess();
                        }
                    }
                });

            }break;

            //todo 图像失败了，允许点击之后重新下载
            case AppConstant.MessageConstant.IMAGE_LOADED_FAILURE:{
//                msgStatusError(imageMessage);
//                getImageProgress().hideProgress();
                messageImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        getImageProgress().hideProgress();
                        imageLoadListener.onLoadComplete(imageUri);
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        getImageProgress().showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {
                    }
                });
                messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        messageImage.setImageUrl(url);
                    }
                });
            }break;

        }
    }

    /**---------------------图片下载相关、点击、以及事件回调start-----------------------------------*/
    public interface  BtnImageListener{
        void onMsgSuccess();
        void onMsgFailure();
    }

    public void setBtnImageListener(BtnImageListener btnImageListener){
        this.btnImageListener = btnImageListener;
    }


    public interface ImageLoadListener{
        void onLoadComplete(String path);
        // 应该把exception 返回结构放进去
        void onLoadFailed();

    }

    public void setImageLoadListener(ImageLoadListener imageLoadListener){
        this.imageLoadListener = imageLoadListener;
    }

    /**---------------------图片下载相关、以及事件回调 end-----------------------------------*/


    /**----------------------set/get------------------------------------*/
    public View getMessageLayout() {
        return messageLayout;
    }

    public ImageView getMessageImage() {
        return messageImage;
    }

    public BTProgressbar getImageProgress() {
        return imageProgress;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

}
