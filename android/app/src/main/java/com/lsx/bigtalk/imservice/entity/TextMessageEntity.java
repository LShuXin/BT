package com.lsx.bigtalk.imservice.entity;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.entity.PeerEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.config.MessageConstant;
import com.lsx.bigtalk.imservice.support.SequenceNumberMaker;


public class TextMessageEntity extends MessageEntity implements Serializable {

    public TextMessageEntity() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    private TextMessageEntity(MessageEntity entity) {
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    public static TextMessageEntity parseFromNet(MessageEntity entity) {
        TextMessageEntity textMessage = new TextMessageEntity(entity);
        textMessage.setStatus(MessageConstant.MSG_SUCCESS);
        textMessage.setDisplayType(DBConstant.SHOW_TYPE_PLAIN_TEXT);
        return textMessage;
    }

    public static TextMessageEntity parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != DBConstant.SHOW_TYPE_PLAIN_TEXT) {
            throw new RuntimeException("parseFromDB, error show type");
        }
        return new TextMessageEntity(entity);
    }

    public static TextMessageEntity buildForSend(String content, UserEntity fromUser, PeerEntity peerEntity) {
        TextMessageEntity textMessage = new TextMessageEntity();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPeerId());
        textMessage.setToId(peerEntity.getPeerId());
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime);
        textMessage.setDisplayType(DBConstant.SHOW_TYPE_PLAIN_TEXT);
        textMessage.setGIfEmo(true);
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP
                ? DBConstant.MSG_TYPE_GROUP_TEXT
                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        textMessage.setMsgType(msgType);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(content);
        textMessage.buildSessionKey(true);
        return textMessage;
    }


    /**
     * Not-null value.
     * DB的时候需要
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getSendContent() {
        /** 加密*/
        String sendContent = new String(com.lsx.bigtalk.Security.getInstance().EncryptMsg(content));
        return sendContent.getBytes(StandardCharsets.UTF_8);
    }
}
