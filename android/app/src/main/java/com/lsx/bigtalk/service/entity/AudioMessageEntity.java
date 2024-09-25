package com.lsx.bigtalk.service.entity;

import java.io.Serializable;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.storage.db.entity.PeerEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.service.support.SequenceNumberMaker;
import com.lsx.bigtalk.utils.CommonUtil;
import com.lsx.bigtalk.utils.FileUtil;


public class AudioMessageEntity extends MessageEntity implements Serializable {
    private String audioPath = "";
    private int audioLength = 0;
    private int readStatus = AppConstant.MessageConstant.AUDIO_UNREAD;

    public AudioMessageEntity() {
        msgId = SequenceNumberMaker.getInstance().makeLocalUniqueMsgId();
    }

    private AudioMessageEntity(MessageEntity entity) {
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        content = entity.getContent();
        msgType = entity.getMsgType();
        sessionKey = entity.getSessionKey();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }
    
    public static AudioMessageEntity parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != AppConstant.DBConstant.SHOW_TYPE_AUDIO) {
            throw new RuntimeException("#AudioMessage# parseFromDB,not SHOW_TYPE_AUDIO");
        }
        AudioMessageEntity audioMessage = new AudioMessageEntity(entity);
        // 注意坑 啊
        String originContent = entity.getContent();

        JSONObject extraContent = null;
        try {
            extraContent = new JSONObject(originContent);
            audioMessage.setAudioPath(extraContent.getString("audioPath"));
            audioMessage.setAudioLength(extraContent.getInt("audioLength"));
            audioMessage.setReadStatus(extraContent.getInt("readStatus"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return audioMessage;
    }

    public static AudioMessageEntity buildForSend(float audioLen, String audioSavePath, UserEntity fromUser, PeerEntity peerEntity) {
        int tLen = (int) (audioLen + 0.5);
        tLen = Math.max(tLen, 1);
        if (tLen < audioLen) {
            ++tLen;
        }

        int nowTime = (int) (System.currentTimeMillis() / 1000);
        AudioMessageEntity audioMessage = new AudioMessageEntity();
        audioMessage.setFromId(fromUser.getPeerId());
        audioMessage.setToId(peerEntity.getPeerId());
        audioMessage.setCreated(nowTime);
        audioMessage.setUpdated(nowTime);
        int peerType = peerEntity.getType();
        int msgType = peerType == AppConstant.DBConstant.SESSION_TYPE_GROUP ? AppConstant.DBConstant.MSG_TYPE_GROUP_AUDIO :
                AppConstant.DBConstant.MSG_TYPE_SINGLE_AUDIO;
        audioMessage.setMsgType(msgType);

        audioMessage.setAudioPath(audioSavePath);
        audioMessage.setAudioLength(tLen);
        audioMessage.setReadStatus(AppConstant.MessageConstant.AUDIO_READ);
        audioMessage.setDisplayType(AppConstant.DBConstant.SHOW_TYPE_AUDIO);
        audioMessage.setStatus(AppConstant.MessageConstant.MSG_SENDING);
        audioMessage.buildSessionKey(true);
        return audioMessage;
    }

    /**
     * for db storage usage
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("audioPath", audioPath);
            extraContent.put("audioLength", audioLength);
            extraContent.put("readStatus", readStatus);
            return extraContent.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public byte[] getSendContent() {
        byte[] result = new byte[4];
        result = CommonUtil.intToBytes(audioLength);
        if (TextUtils.isEmpty(audioPath)) {
            return result;
        }

        byte[] bytes = FileUtil.getFileContent(audioPath);
        if (bytes == null) {
            return null;
        }
        int contentLength = bytes.length;
        byte[] byteAudioContent = new byte[4 + contentLength];
        System.arraycopy(result, 0, byteAudioContent, 0, 4);
        System.arraycopy(bytes, 0, byteAudioContent, 4, contentLength);
        return byteAudioContent;
    }


    /***-------------------------------set/get----------------------------------*/
    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioLength(int audioLength) {
        this.audioLength = audioLength;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getReadStatus() {
        return readStatus;
    }
}
