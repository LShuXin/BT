
package com.lsx.bigtalk.ui.widget.message;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.ui.helper.AudioPlayerHandler;

/**
 * A popup window that can be used to display an arbitrary view
 * OnItemClickListener
 */
public class MessageOperatePopup implements View.OnClickListener, View.OnTouchListener {

    private final PopupWindow mPopup;
    private static MessageOperatePopup messageOperatePopup;
    private OnItemClickListener mListener;

    private int mWidth;
    private final int mHeight;

    private final int mParentTop;
    private final TextView copyBtn;
    private final TextView resendBtn;
    private final TextView speakerBtn;
    private boolean bcopyShow, bresendShow, bspeakerShow;

    private Context context = null;

    public static MessageOperatePopup instance(Context ctx,View parent){
        if(null == messageOperatePopup ){
            synchronized (MessageOperatePopup.class){
                messageOperatePopup = new  MessageOperatePopup(ctx,parent);
            }
        }
        return messageOperatePopup;
    }

    public void hidePopup() {
        if (messageOperatePopup != null) {
            messageOperatePopup.dismiss();
        }
    }


    @SuppressWarnings("deprecation")
    private MessageOperatePopup(Context ctx, View parent) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.message_operation_popup_view,
                null);
        this.context = ctx;

        copyBtn = view.findViewById(R.id.copy_btn);
        copyBtn.setOnClickListener(this);
        copyBtn.setOnTouchListener(this);
        copyBtn.setPadding(0, 13, 0, 8);

        resendBtn = view.findViewById(R.id.resend_btn);
        resendBtn.setOnClickListener(this);
        resendBtn.setOnTouchListener(this);
        resendBtn.setPadding(0, 13, 0, 8);

        speakerBtn = view.findViewById(R.id.speaker_btn);
        speakerBtn.setOnClickListener(this);
        speakerBtn.setOnTouchListener(this);
        speakerBtn.setPadding(0, 13, 0, 8);

        mWidth = (int) context.getResources().getDimension(
                R.dimen.message_item_popup_width_single_short);
        mHeight = (int) context.getResources().getDimension(
                R.dimen.message_item_popup_height);

        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        mParentTop = location[1];
        mPopup = new PopupWindow(view, mWidth, mHeight);
        // mPopup.setFocusable(true);
        // 设置允许在外点击消失
        mPopup.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mPopup.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void show(View item, int type, boolean bResend, boolean bSelf) {
        if (mPopup == null || mPopup.isShowing()) {
            return;
        }

        boolean showTop = true;

        int[] location = new int[2];
        item.getLocationOnScreen(location);
        // 默认在item上面弹出
        if (location[1] - mParentTop/* - mHeight */<= 0) {
            // showTop = false;
        }
        else {
            // 如果不是在最顶部，显示的距离要上移10
            location[1] = location[1] - 10;
        }

        //下面全用回调 todo

        // 语音类型
        if (type == AppConstant.DBConstant.SHOW_TYPE_AUDIO) {
            speakerBtn.setVisibility(View.VISIBLE);
            if (AudioPlayerHandler.getInstance().getAudioMode(context) == AudioManager.MODE_NORMAL) {
                speakerBtn.setText(R.string.call_mode);
            } else {
                speakerBtn.setText(R.string.speaker_mode);
            }
            bspeakerShow = true;
        } else {
            speakerBtn.setVisibility(View.GONE);
            bspeakerShow = false;
        }

        // 自己消息重发
        // 自己的消息
        // 非自己的消息
            // 图片语音
            // 文本
        if (bResend && bSelf) {
            resendBtn.setVisibility(View.VISIBLE);
            bresendShow = true;
            if (type == AppConstant.DBConstant.SHOW_TYPE_PLAIN_TEXT) {
                copyBtn.setVisibility(View.VISIBLE);
                bcopyShow = true;
            } else {
                copyBtn.setVisibility(View.GONE);
                bcopyShow = false;
            }
        } else if (!bResend && bSelf) {
            resendBtn.setVisibility(View.GONE);
            bresendShow = false;
            if (type != AppConstant.DBConstant.SHOW_TYPE_IMAGE && type != AppConstant.DBConstant.SHOW_TYPE_AUDIO && type != AppConstant.DBConstant.SHOW_TYPE_GIF)  {
                copyBtn.setVisibility(View.VISIBLE);
                bcopyShow = true;
            } else {
                copyBtn.setVisibility(View.GONE);
                bcopyShow = false;
            }
        } else {
            if (type != AppConstant.DBConstant.SHOW_TYPE_IMAGE && type != AppConstant.DBConstant.SHOW_TYPE_AUDIO && type != AppConstant.DBConstant.SHOW_TYPE_GIF) {
                copyBtn.setVisibility(View.VISIBLE);
                bcopyShow = true;
            }else {
                copyBtn.setVisibility(View.GONE);
                bcopyShow = false;
            }
        }
        Resources resource = context.getResources();
        if (bcopyShow && bresendShow) {
            // int nWidth = (int) resource
            // .getDimension(R.dimen.message_item_popup_width_double_short);
            mWidth = (int) resource
                    .getDimension(R.dimen.message_item_popup_width_double_short);
            mPopup.setWidth(mWidth);
            Drawable bgLeft = resource
                    .getDrawable(R.drawable.bg_bubble_mine_nomal);
            copyBtn.setBackgroundDrawable(bgLeft);
            copyBtn.setPadding(0, 13, 0, 8);
            Drawable bgRight = resource
                    .getDrawable(R.drawable.bg_bubble_others_nomal);
            resendBtn.setBackgroundDrawable(bgRight);
            resendBtn.setPadding(0, 13, 0, 8);
        } else if (bcopyShow || bresendShow) {
            if (bspeakerShow) {
                // int nWidth = (int) resource
                // .getDimension(R.dimen.message_item_popup_width_double_long);
                mWidth = (int) resource
                        .getDimension(R.dimen.message_item_popup_width_double_long);
                mPopup.setWidth(mWidth);
                Drawable bgLeft = resource
                        .getDrawable(R.drawable.bg_bubble_mine_nomal);
                speakerBtn.setBackgroundDrawable(bgLeft);
                Drawable bgRight = resource
                        .getDrawable(R.drawable.bg_bubble_others_nomal);
                speakerBtn.setPadding(0, 13, 0, 8);
                resendBtn.setBackgroundDrawable(bgRight);
                resendBtn.setPadding(0, 13, 0, 8);
            } else {
                // int nWidth = (int) resource
                // .getDimension(R.dimen.message_item_popup_width_single_short);
                mWidth = (int) resource
                        .getDimension(R.dimen.message_item_popup_width_single_short);
                mPopup.setWidth(mWidth);
                Drawable bgNormal = resource
                        .getDrawable(R.drawable.bg_message_popup_normal);
                copyBtn.setBackgroundDrawable(bgNormal);
                resendBtn.setBackgroundDrawable(bgNormal);
                copyBtn.setPadding(0, 13, 0, 8);
                resendBtn.setPadding(0, 13, 0, 8);
            }
        } else if (bspeakerShow) {
            // int nWidth = (int) resource
            // .getDimension(R.dimen.message_item_popup_width_single_long);
            mWidth = (int) resource
                    .getDimension(R.dimen.message_item_popup_width_single_long);
            mPopup.setWidth(mWidth);
            Drawable bgNormal = resource
                    .getDrawable(R.drawable.bg_message_popup_normal);
            speakerBtn.setBackgroundDrawable(bgNormal);
            speakerBtn.setPadding(0, 13, 0, 8);
        } else {
            return;
        }
        if (showTop) {
            if (location[1] - mParentTop/* - mHeight */> 0) {
                mPopup.showAtLocation(item, Gravity.NO_GRAVITY, location[0]
                        + (item.getWidth() / 2 - mWidth / 2), location[1]
                        - mHeight);
            } else {
                mPopup.showAtLocation(item, Gravity.NO_GRAVITY, location[0]
                        + (item.getWidth() / 2 - mWidth / 2), mHeight / 2);
            }
        } else {
            // TODO: 在下面弹出的时候需要翻转背景
            mPopup.showAtLocation(item, Gravity.NO_GRAVITY,
                    location[0] + (item.getWidth() / 2 - mWidth / 2),
                    location[1] + item.getHeight());
        }
    }

    public void dismiss() {
        if (mPopup == null || !mPopup.isShowing()) {
            return;
        }

        mPopup.dismiss();
    }

    public interface OnItemClickListener {
        void onCopyClick();

        void onResendClick();

        void onSpeakerClick();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        if (R.id.copy_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onCopyClick();
            }
        } else if (R.id.resend_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onResendClick();
            }
        } else if (R.id.speaker_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onSpeakerClick();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        Resources resource = context.getResources();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (R.id.copy_btn == v.getId()) {
                Drawable drawable = null;
                if (bcopyShow && bresendShow) {
                    drawable = resource
                            .getDrawable(R.drawable.bg_bubble_mine_nomal);
                } else if (bcopyShow || bresendShow) {
                    drawable = resource
                            .getDrawable(R.drawable.bg_message_popup_normal);
                }
                if (drawable != null) {
                    copyBtn.setBackgroundDrawable(drawable);
                    copyBtn.setPadding(0, 13, 0, 8);
                }
            } else if (R.id.resend_btn == v.getId()) {
                Drawable drawable = null;
                if (bcopyShow && bresendShow) {
                    drawable = resource
                            .getDrawable(R.drawable.bg_bubble_others_nomal);
                } else if (bcopyShow || bresendShow) {
                    if (bspeakerShow) {
                        drawable = resource
                                .getDrawable(R.drawable.bg_bubble_others_nomal);
                    } else {
                        drawable = resource
                                .getDrawable(R.drawable.bg_message_popup_normal);
                    }
                }
                if (drawable != null) {
                    resendBtn.setBackgroundDrawable(drawable);
                    resendBtn.setPadding(0, 13, 0, 8);
                }
            } else if (R.id.speaker_btn == v.getId()) {
                Drawable drawable = null;
                if (bresendShow) {
                    drawable = resource
                            .getDrawable(R.drawable.bg_bubble_mine_nomal);
                } else if (bspeakerShow) {
                    drawable = resource
                            .getDrawable(R.drawable.bg_message_popup_normal);
                }
                if (drawable != null) {
                    speakerBtn.setBackgroundDrawable(drawable);
                    speakerBtn.setPadding(0, 13, 0, 8);
                }
            }
        } else {
            if (R.id.copy_btn == v.getId()) {
                Drawable drawableResend = null;
                Drawable drawableCopy = null;
                if (bcopyShow && bresendShow) {
                    drawableCopy = resource
                            .getDrawable(R.drawable.bg_bubble_mine_pressed);
                    drawableResend = resource
                            .getDrawable(R.drawable.bg_bubble_others_nomal);
                } else if (bcopyShow || bresendShow) {
                    drawableCopy = resource
                            .getDrawable(R.drawable.bg_message_popup_pressed);
                }
                if (drawableCopy != null) {
                    copyBtn.setBackgroundDrawable(drawableCopy);
                    copyBtn.setPadding(0, 13, 0, 8);
                }
                if (drawableResend != null) {
                    resendBtn.setBackgroundDrawable(drawableResend);
                    resendBtn.setPadding(0, 13, 0, 8);
                }
            } else if (R.id.resend_btn == v.getId()) {
                Drawable drawableCopy = null;
                Drawable drawableResend = null;
                Drawable drawableSpeaker = null;
                if (bcopyShow && bresendShow) {
                    drawableCopy = resource
                            .getDrawable(R.drawable.bg_bubble_mine_nomal);
                    drawableResend = resource
                            .getDrawable(R.drawable.bg_bubble_others_pressed);
                } else if (bcopyShow || bresendShow) {
                    if (bspeakerShow) {
                        drawableSpeaker = resource
                                .getDrawable(R.drawable.bg_bubble_mine_nomal);
                        drawableResend = resource
                                .getDrawable(R.drawable.bg_bubble_others_pressed);
                    } else {
                        drawableResend = resource
                                .getDrawable(R.drawable.bg_message_popup_pressed);
                    }
                }
                if (drawableResend != null) {
                    resendBtn.setBackgroundDrawable(drawableResend);
                    resendBtn.setPadding(0, 13, 0, 8);
                }
                if (drawableCopy != null) {
                    copyBtn.setBackgroundDrawable(drawableCopy);
                    copyBtn.setPadding(0, 13, 0, 8);
                }
                if (drawableSpeaker != null) {
                    speakerBtn.setBackgroundDrawable(drawableSpeaker);
                    speakerBtn.setPadding(0, 13, 0, 8);
                }
            } else if (R.id.speaker_btn == v.getId()) {
                // Drawable drawableCopy = null;
                Drawable drawableResend = null;
                Drawable drawableSpeaker = null;
                if (bresendShow && bspeakerShow) {
                    drawableSpeaker = resource
                            .getDrawable(R.drawable.bg_bubble_mine_pressed);
                    drawableResend = resource
                            .getDrawable(R.drawable.bg_bubble_others_nomal);
                } else if (bspeakerShow) {
                    drawableSpeaker = resource
                            .getDrawable(R.drawable.bg_message_popup_pressed);
                }
                if (drawableResend != null) {
                    resendBtn.setBackgroundDrawable(drawableResend);
                    resendBtn.setPadding(0, 13, 0, 8);
                }
                if (drawableSpeaker != null) {
                    speakerBtn.setBackgroundDrawable(drawableSpeaker);
                    speakerBtn.setPadding(0, 13, 0, 8);
                }
            }
        }
        return false;
    }
}
