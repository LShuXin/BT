package com.lsx.bigtalk.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.service.entity.ImageMessageEntity;
import com.lsx.bigtalk.ui.widget.GifLoadTask;
import com.lsx.bigtalk.ui.widget.GifView;

/**
 * Created by zhujian on 15/3/26.
 */
public class GifImageRenderView extends  BaseMsgRenderView {
    private GifView messageContent;

    public GifView getMessageContent()
    {
        return messageContent;
    }
    public static GifImageRenderView inflater(Context context,ViewGroup viewGroup,boolean isMine){
        int resource = isMine? R.layout.mine_gif_message_item_view :R.layout.others_gif_message_item_view;
        GifImageRenderView gifRenderView = (GifImageRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        gifRenderView.setMine(isMine);
        return gifRenderView;
    }

    public GifImageRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = findViewById(R.id.message_image);
    }

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity,Context context) {
        super.render(messageEntity, userEntity,context);
        ImageMessageEntity imageMessage = (ImageMessageEntity) messageEntity;
        String url = imageMessage.getUrl();
        new GifLoadTask() {
            @Override
            protected void onPostExecute(byte[] bytes) {
                messageContent.setBytes(bytes);
                messageContent.startAnimation();
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
        }.execute(url);
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**----------------set/get---------------------------------*/

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }


    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }


}
