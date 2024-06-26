package com.lsx.bigtalk.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.config.MessageConstant;
import com.lsx.bigtalk.imservice.entity.AudioMessageEntity;
import com.lsx.bigtalk.imservice.entity.ImageMessageEntity;
import com.lsx.bigtalk.imservice.entity.RichTextMessageEntity;
import com.lsx.bigtalk.imservice.entity.TextMessageEntity;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.ui.activity.PreviewGifActivity;
import com.lsx.bigtalk.ui.activity.PreviewMessageImagesActivity;
import com.lsx.bigtalk.ui.activity.PreviewTextActivity;
import com.lsx.bigtalk.ui.helper.AudioPlayerHandler;
import com.lsx.bigtalk.ui.helper.Emoparser;
import com.lsx.bigtalk.ui.helper.listener.OnDoubleClickListener;
import com.lsx.bigtalk.ui.widget.GifView;
import com.lsx.bigtalk.ui.widget.SpeekerToast;
import com.lsx.bigtalk.ui.widget.message.AudioRenderView;
import com.lsx.bigtalk.ui.widget.message.EmojiRenderView;
import com.lsx.bigtalk.ui.widget.message.GifImageRenderView;
import com.lsx.bigtalk.ui.widget.message.ImageRenderView;
import com.lsx.bigtalk.ui.widget.message.MessageOperatePopup;
import com.lsx.bigtalk.ui.widget.message.RenderType;
import com.lsx.bigtalk.ui.widget.message.TextRenderView;
import com.lsx.bigtalk.ui.widget.message.TimeRenderView;
import com.lsx.bigtalk.utils.CommonUtil;
import com.lsx.bigtalk.utils.DateUtil;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.utils.Logger;


public class MessageAdapter extends BaseAdapter {
    private final Logger logger = Logger.getLogger(MessageAdapter.class);
    private final ArrayList<Object> msgObjectList = new ArrayList<>();
    private MessageOperatePopup currentPop;
    private final Context ctx;
    private UserEntity loginUser;
    private IMService imService;

    public MessageAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return msgObjectList.size();
    }

    @Override
    public int getViewTypeCount() {
        return RenderType.values().length;
    }
    
    @Override
    public int getItemViewType(int position) {
        try {
            RenderType type = RenderType.MESSAGE_TYPE_INVALID;
            Object obj = msgObjectList.get(position);
            if (obj instanceof Integer) {
                type = RenderType.MESSAGE_TYPE_TIME_TITLE;
            } else if (obj instanceof MessageEntity) {
                MessageEntity info = (MessageEntity) obj;
                boolean isMine = info.getFromId() == loginUser.getPeerId();
                switch (info.getDisplayType()) {
                    case DBConstant.SHOW_TYPE_AUDIO:
                    {
                        type = isMine
                                ? RenderType.MESSAGE_TYPE_MINE_AUDIO
                                : RenderType.MESSAGE_TYPE_OTHER_AUDIO;
                        break;
                    }
                    case DBConstant.SHOW_TYPE_IMAGE:
                    {
                        ImageMessageEntity imageMessage = (ImageMessageEntity) info;
                        if (CommonUtil.gifCheck(imageMessage.getUrl())) {
                            type = isMine
                                    ? RenderType.MESSAGE_TYPE_MINE_GIF
                                    : RenderType.MESSAGE_TYPE_OTHER_GIF;
                        } else {
                            type = isMine
                                    ? RenderType.MESSAGE_TYPE_MINE_IMAGE
                                    : RenderType.MESSAGE_TYPE_OTHER_IMAGE;
                        }

                        break;
                    }
                    case DBConstant.SHOW_TYPE_PLAIN_TEXT:
                    {
                        if (info.isGIfEmo()) {
                            type = isMine
                                    ? RenderType.MESSAGE_TYPE_MINE_GIF
                                    : RenderType.MESSAGE_TYPE_OTHER_GIF;
                        } else {
                            type = isMine
                                    ? RenderType.MESSAGE_TYPE_MINE_TEXT
                                    : RenderType.MESSAGE_TYPE_OTHER_TEXT;
                        }

                        break;
                    }
                    case DBConstant.SHOW_TYPE_RICH_TEXT:
                    default:
                        break;
                }
            }
            return type.ordinal();
        } catch (Exception e) {
            logger.e(e.getMessage());
            return RenderType.MESSAGE_TYPE_INVALID.ordinal();
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount() || position < 0) {
            return null;
        }
        return msgObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setImService(IMService imService, UserEntity loginUser) {
        this.imService = imService;
        this.loginUser = loginUser;
    }

    public void addItem(final MessageEntity msg) {
        if (msg.getDisplayType() == DBConstant.MSG_TYPE_SINGLE_TEXT) {
            msg.setGIfEmo(isMsgGif(msg));
        }
        int nextTime = msg.getCreated();
        if (getCount() > 0) {
            Object object = msgObjectList.get(getCount() - 1);
            if (object instanceof MessageEntity) {
                int preTime = ((MessageEntity) object).getCreated();
                boolean needTime = DateUtil.needDisplayTime(preTime, nextTime);
                if (needTime) {
                    Integer in = nextTime;
                    msgObjectList.add(in);
                }
            }
        } else {
            Integer in = msg.getCreated();
            msgObjectList.add(in);
        }

        if (msg.getDisplayType() == DBConstant.SHOW_TYPE_RICH_TEXT) {
            RichTextMessageEntity mixMessage = (RichTextMessageEntity) msg;
            msgObjectList.addAll(mixMessage.getMsgList());
        } else {
            msgObjectList.add(msg);
        }

        if (msg instanceof ImageMessageEntity) {
            ImageMessageEntity.addToImageMessageList((ImageMessageEntity) msg);
        }
        notifyDataSetChanged();
    }

    private boolean isMsgGif(MessageEntity msg) {
        String content = msg.getContent();
        // @YM 临时处理  牙牙表情与消息混合出现的消息丢失
        if (TextUtils.isEmpty(content) || !(content.startsWith("[") && content.endsWith("]"))) {
            return false;
        }
        return Emoparser.getInstance(this.ctx).isMessageGif(msg.getContent());
    }

    public MessageEntity getTopMsgEntity() {
        if (msgObjectList.isEmpty()) {
            return null;
        }
        for (Object result : msgObjectList) {
            if (result instanceof MessageEntity) {
                return (MessageEntity) result;
            }
        }
        return null;
    }

    public static class MessageTimeComparator implements Comparator<MessageEntity> {
        @Override
        public int compare(MessageEntity lhs, MessageEntity rhs) {
            if (lhs.getCreated() == rhs.getCreated()) {
                return lhs.getMsgId() - rhs.getMsgId();
            }
            return lhs.getCreated() - rhs.getCreated();
        }
    }

    public void loadHistoryList(final List<MessageEntity> historyList) {
        if (null == historyList || historyList.isEmpty()) {
            return;
        }
        historyList.sort(new MessageTimeComparator());
        ArrayList<Object> chatList = new ArrayList<>();
        int preTime = 0;
        int nextTime;
        for (MessageEntity msg : historyList) {
            if (msg.getDisplayType() == DBConstant.MSG_TYPE_SINGLE_TEXT) {
                msg.setGIfEmo(isMsgGif(msg));
            }
            nextTime = msg.getCreated();
            boolean needTimeBubble = DateUtil.needDisplayTime(preTime, nextTime);
            if (needTimeBubble) {
                Integer in = nextTime;
                chatList.add(in);
            }
            preTime = nextTime;
            if (msg.getDisplayType() == DBConstant.SHOW_TYPE_RICH_TEXT) {
                RichTextMessageEntity mixMessage = (RichTextMessageEntity) msg;
                chatList.addAll(mixMessage.getMsgList());
            } else {
                chatList.add(msg);
            }
        }
        msgObjectList.addAll(0, chatList);
        getImageList();
        notifyDataSetChanged();
    }

    private void getImageList() {
        for (int i = msgObjectList.size() - 1; i >= 0; --i) {
            Object item = msgObjectList.get(i);
            if (item instanceof ImageMessageEntity) {
                ImageMessageEntity.addToImageMessageList((ImageMessageEntity) item);
            }
        }
    }

    /**
     * 临时处理，一定要干掉
     */
    public void hidePopup() {
        if (currentPop != null) {
            currentPop.hidePopup();
        }
    }

    public void clearItem() {
        msgObjectList.clear();
    }

    public void updateItemState(int position, final MessageEntity messageEntity) {
        imService.getDbInterface().insertOrUpdateMessage(messageEntity);
        notifyDataSetChanged();
    }

    /**
     * 对于混合消息的特殊处理
     */
    public void updateItemState(final MessageEntity messageEntity) {
        long dbId = messageEntity.getId();
        int msgId = messageEntity.getMsgId();
        int len = msgObjectList.size();
        for (int index = len - 1; index > 0; index--) {
            Object object = msgObjectList.get(index);
            if (object instanceof MessageEntity) {
                MessageEntity entity = (MessageEntity) object;
                if (object instanceof ImageMessageEntity) {
                    ImageMessageEntity.addToImageMessageList((ImageMessageEntity) object);
                }
                if (entity.getId() == dbId && entity.getMsgId() == msgId) {
                    msgObjectList.set(index, messageEntity);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    private View renderTimeBubble(int position, View convertView, ViewGroup parent) {
        TimeRenderView timeRenderView;
        Integer time = (Integer) msgObjectList.get(position);
        if (null == convertView) {
            timeRenderView = TimeRenderView.inflater(ctx, parent);
        } else {
            timeRenderView = (TimeRenderView) convertView;
        }
        timeRenderView.setTime(time);
        return timeRenderView;
    }

    private View renderImageMsg(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        ImageRenderView imageRenderView;
        final ImageMessageEntity imageMessage = (ImageMessageEntity) msgObjectList.get(position);
        UserEntity senderUserEntity = imService.getIMContactManager().findContact(imageMessage.getFromId());

        if (null == convertView) {
            imageRenderView = ImageRenderView.inflater(ctx, parent, isMine);
        } else {
            imageRenderView = (ImageRenderView) convertView;
        }

        final ImageView messageImage = imageRenderView.getMessageImage();
        final int msgId = imageMessage.getMsgId();
        imageRenderView.setBtnImageListener(new ImageRenderView.BtnImageListener() {
            @Override
            public void onMsgFailure() {
                /**
                 * 多端同步也不会拉到本地失败的数据
                 * 只有isMine才有的状态，消息发送失败
                 * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
                 * 2. 图片上传成功，但是发送失败。 点击重新发送??
                 */
                if (FileUtil.isSdCardAvailuable()) {
                    imageMessage.setStatus(MessageConstant.MSG_SENDING);
                    if (imService != null) {
                        imService.getIMMessageManager().resendMessage(imageMessage);
                    }
                    updateItemState(msgId, imageMessage);
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.sdcard_unavaluable), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onMsgSuccess() {
                Intent intent = new Intent(ctx, PreviewMessageImagesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(IntentConstant.CUR_MESSAGE, imageMessage);
                intent.putExtras(bundle);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.image_right_enter, R.anim.stay_y);
            }
        });

        imageRenderView.setImageLoadListener(new ImageRenderView.ImageLoadListener() {
            @Override
            public void onLoadComplete(String localPath) {
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                updateItemState(imageMessage);
            }

            @Override
            public void onLoadFailed() {
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                updateItemState(imageMessage);
            }
        });

        final View messageLayout = imageRenderView.getMessageLayout();
        messageImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
                boolean bResend = (imageMessage.getStatus() == MessageConstant.MSG_FAILURE)
                        || (imageMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);
                popup.show(messageLayout, DBConstant.SHOW_TYPE_IMAGE, bResend, isMine);
                return true;
            }
        });

        imageRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
                popup.show(messageLayout, DBConstant.SHOW_TYPE_IMAGE, true, isMine);
            }
        });

        imageRenderView.render(imageMessage, senderUserEntity, ctx);

        return imageRenderView;
    }

    private View renderGIFImageMsg(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        GifImageRenderView imageRenderView;
        final ImageMessageEntity imageMessage = (ImageMessageEntity) msgObjectList.get(position);
        UserEntity senderUserEntity = imService.getIMContactManager().findContact(imageMessage.getFromId());
        if (null == convertView) {
            imageRenderView = GifImageRenderView.inflater(ctx, parent, isMine);
        } else {
            imageRenderView = (GifImageRenderView) convertView;
        }
        GifView imageView = imageRenderView.getMessageContent();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String url = imageMessage.getUrl();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, url);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.image_right_enter, R.anim.stay_y);
            }
        });
        imageRenderView.render(imageMessage, senderUserEntity, ctx);
        return imageRenderView;
    }

    private View renderAudioMsg(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        AudioRenderView audioRenderView;
        final AudioMessageEntity audioMessage = (AudioMessageEntity) msgObjectList.get(position);
        UserEntity senderUserEntity = imService.getIMContactManager().findContact(audioMessage.getFromId());
        if (null == convertView) {
            audioRenderView = AudioRenderView.inflater(ctx, parent, isMine);
        } else {
            audioRenderView = (AudioRenderView) convertView;
        }
        final String audioPath = audioMessage.getAudioPath();
        final View messageLayout = audioRenderView.getMessageLayout();
        if (!TextUtils.isEmpty(audioPath)) {
            messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(audioMessage, position));
                    boolean isResend = audioMessage.getStatus() == MessageConstant.MSG_FAILURE;
                    popup.show(messageLayout, DBConstant.SHOW_TYPE_AUDIO, isResend, isMine);
                    return true;
                }
            });
        }

        audioRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(audioMessage, position));
                popup.show(messageLayout, DBConstant.SHOW_TYPE_AUDIO, true, isMine);
            }
        });

        audioRenderView.setBtnImageListener(new AudioRenderView.BtnImageListener() {
            @Override
            public void onClickUnread() {
                audioMessage.setReadStatus(MessageConstant.AUDIO_READ);
                imService.getDbInterface().insertOrUpdateMessage(audioMessage);
            }

            @Override
            public void onClickReaded() {
            }
        });
        audioRenderView.render(audioMessage, senderUserEntity, ctx);
        return audioRenderView;
    }

    private View renderTextMsg(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
        TextRenderView textRenderView;
        final TextMessageEntity textMessage = (TextMessageEntity) msgObjectList.get(position);
        UserEntity userEntity = imService.getIMContactManager().findContact(textMessage.getFromId());

        if (null == convertView) {
            textRenderView = TextRenderView.inflater(ctx, viewGroup, isMine);
        } else {
            textRenderView = (TextRenderView) convertView;
        }

        final TextView textView = textRenderView.getMessageContent();

        textRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
                popup.show(textView, DBConstant.SHOW_TYPE_PLAIN_TEXT, true, isMine);
            }
        });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
                boolean bResend = textMessage.getStatus() == MessageConstant.MSG_FAILURE;
                popup.show(textView, DBConstant.SHOW_TYPE_PLAIN_TEXT, bResend, isMine);
                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = textMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
            }

            @Override
            public void onDoubleClick(View view) {
                Intent intent = new Intent(ctx, PreviewTextActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
            }
        });
        textRenderView.render(textMessage, userEntity, ctx);
        return textRenderView;
    }

    private View renderGIFMsg(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
        EmojiRenderView gifRenderView;
        final TextMessageEntity textMessage = (TextMessageEntity) msgObjectList.get(position);
        UserEntity userEntity = imService.getIMContactManager().findContact(textMessage.getFromId());
        if (null == convertView) {
            gifRenderView = EmojiRenderView.inflater(ctx, viewGroup, isMine); //new TextRenderView(ctx,viewGroup,isMine);
        } else {
            gifRenderView = (EmojiRenderView) convertView;
        }

        final ImageView imageView = gifRenderView.getMessageContent();
        // 失败事件添加
        gifRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
                popup.show(imageView, DBConstant.SHOW_TYPE_GIF, true, isMine);
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
                boolean bResend = textMessage.getStatus() == MessageConstant.MSG_FAILURE;
                popup.show(imageView, DBConstant.SHOW_TYPE_GIF, bResend, isMine);

                return true;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String content = textMessage.getContent();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.image_right_enter, R.anim.stay_y);
            }
        });

        gifRenderView.render(textMessage, userEntity, ctx);
        return gifRenderView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final int typeIndex = getItemViewType(position);
            RenderType renderType = RenderType.values()[typeIndex];
            // 改用map的形式
            switch (renderType) {
                case MESSAGE_TYPE_INVALID:
                    logger.e("[fatal erro] render type:MESSAGE_TYPE_INVALID");
                    break;

                case MESSAGE_TYPE_TIME_TITLE:
                    convertView = renderTimeBubble(position, convertView, parent);
                    break;

                case MESSAGE_TYPE_MINE_AUDIO:
                    convertView = renderAudioMsg(position, convertView, parent, true);
                    break;

                case MESSAGE_TYPE_OTHER_AUDIO:
                    convertView = renderAudioMsg(position, convertView, parent, false);
                    break;

                case MESSAGE_TYPE_MINE_GIF_IMAGE:
                    convertView = renderGIFImageMsg(position, convertView, parent, true);
                    break;

                case MESSAGE_TYPE_OTHER_GIF_IMAGE:
                    convertView = renderGIFImageMsg(position, convertView, parent, false);
                    break;

                case MESSAGE_TYPE_MINE_IMAGE:
                    convertView = renderImageMsg(position, convertView, parent, true);
                    break;

                case MESSAGE_TYPE_OTHER_IMAGE:
                    convertView = renderImageMsg(position, convertView, parent, false);

                case MESSAGE_TYPE_MINE_TEXT:
                    convertView = renderTextMsg(position, convertView, parent, true);
                    break;

                case MESSAGE_TYPE_OTHER_TEXT:
                    convertView = renderTextMsg(position, convertView, parent, false);
                    break;

                case MESSAGE_TYPE_MINE_GIF:
                    convertView = renderGIFMsg(position, convertView, parent, true);
                    break;

                case MESSAGE_TYPE_OTHER_GIF:
                    convertView = renderGIFMsg(position, convertView, parent, false);
                    break;
            }
            return convertView;
        } catch (Exception e) {
            logger.e("chat#%s", e);
            return null;
        }
    }

    /**
     * 点击事件的定义
     */
    private MessageOperatePopup getPopMenu(ViewGroup parent, MessageOperatePopup.OnItemClickListener listener) {
        MessageOperatePopup popupView = MessageOperatePopup.instance(ctx, parent);
        currentPop = popupView;
        popupView.setOnItemClickListener(listener);
        return popupView;
    }

    private class OperateItemClickListener
            implements
            MessageOperatePopup.OnItemClickListener {

        private final MessageEntity mMsgInfo;
        private final int mType;
        private final int mPosition;

        public OperateItemClickListener(MessageEntity msgInfo, int position) {
            mMsgInfo = msgInfo;
            mType = msgInfo.getDisplayType();
            mPosition = position;
        }

        @SuppressWarnings("deprecation")
        @SuppressLint("NewApi")
        @Override
        public void onCopyClick() {
            try {
                ClipboardManager manager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);

                logger.d("menu#onCopyClick content:%s", mMsgInfo.getContent());
                ClipData data = ClipData.newPlainText("data", mMsgInfo.getContent());
                manager.setPrimaryClip(data);
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }

        @Override
        public void onResendClick() {
            try {
                if (mType == DBConstant.SHOW_TYPE_AUDIO
                        || mType == DBConstant.SHOW_TYPE_PLAIN_TEXT) {

                    if (mMsgInfo.getDisplayType() == DBConstant.SHOW_TYPE_AUDIO) {
                        if (mMsgInfo.getSendContent().length < 4) {
                            return;
                        }
                    }
                } else if (mType == DBConstant.SHOW_TYPE_IMAGE) {
                    logger.d("pic#resend");
                    // 之前的状态是什么 上传没有成功继续上传
                    // 上传成功，发送消息
                    ImageMessageEntity imageMessage = (ImageMessageEntity) mMsgInfo;
                    if (TextUtils.isEmpty(imageMessage.getPath())) {
                        Toast.makeText(ctx, ctx.getString(R.string.image_path_unavaluable), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
                msgObjectList.remove(mPosition);
                addItem(mMsgInfo);
                if (imService != null) {
                    imService.getIMMessageManager().resendMessage(mMsgInfo);
                }

            } catch (Exception e) {
                logger.e("chat#exception:" + e);
            }
        }

        @Override
        public void onSpeakerClick() {
            AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler.getInstance();
            if (audioPlayerHandler.getAudioMode(ctx) == AudioManager.MODE_NORMAL) {
                audioPlayerHandler.setAudioMode(AudioManager.MODE_IN_CALL, ctx);
                SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_call), Toast.LENGTH_SHORT);
            } else {
                audioPlayerHandler.setAudioMode(AudioManager.MODE_NORMAL, ctx);
                SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_speeker), Toast.LENGTH_SHORT);
            }
        }
    }
}
