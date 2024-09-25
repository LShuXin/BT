package com.lsx.bigtalk.service.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.MessageEntity;



public class RichTextMessageEntity extends MessageEntity {

    public List<MessageEntity> msgList;

    public RichTextMessageEntity(List<MessageEntity> entityList) {
        if (entityList == null || entityList.size() <= 1) {
            throw new RuntimeException("MixMessage# type is error!");
        }

        MessageEntity justOne = entityList.get(0);
        id = justOne.getId();
        msgId = justOne.getMsgId();
        fromId = justOne.getFromId();
        toId = justOne.getToId();
        sessionKey = justOne.getSessionKey();
        msgType = justOne.getMsgType();
        status = justOne.getStatus();
        created = justOne.getCreated();
        updated = justOne.getUpdated();
        msgList = entityList;
        displayType = AppConstant.DBConstant.SHOW_TYPE_RICH_TEXT;

        /**分配主键Id
         * 图文混排的之间全部从-1开始
         * 在messageAdapter中 结合msgId进行更新
         *
         * btDb 结合id sessionKey msgid来替换具体的消息
         * {insertOrUpdateMix}
         * */
        long index = -1;
        for (MessageEntity msg : entityList) {
            msg.setId(index);
            index--;
        }
    }

    @Override
    public String getContent() {
        return getSerializableContent(msgList);
    }

    @Override
    public void setSessionKey(String sessionKey) {
        super.setSessionKey(sessionKey);
        for (MessageEntity msg : msgList) {
            msg.setSessionKey(sessionKey);
        }
    }

    @Override
    public void setToId(int toId) {
        super.setToId(toId);
        for (MessageEntity msg : msgList) {
            msg.setToId(toId);
        }
    }

    public RichTextMessageEntity(MessageEntity dbEntity) {
        id = dbEntity.getId();
        msgId = dbEntity.getMsgId();
        fromId = dbEntity.getFromId();
        toId = dbEntity.getToId();
        msgType = dbEntity.getMsgType();
        status = dbEntity.getStatus();
        created = dbEntity.getCreated();
        updated = dbEntity.getUpdated();
        content = dbEntity.getContent();
        displayType = dbEntity.getDisplayType();
        sessionKey = dbEntity.getSessionKey();
    }

    private String getSerializableContent(List<MessageEntity> entityList) {
        Gson gson = new Gson();
        return gson.toJson(entityList);
    }

    public static RichTextMessageEntity parseFromDB(MessageEntity entity) throws JSONException {
        if (entity.getDisplayType() != AppConstant.DBConstant.SHOW_TYPE_RICH_TEXT) {
            throw new RuntimeException("#MixMessage# parseFromDB,not SHOW_TYPE_RICH_TEXT");
        }
        Gson gson = new GsonBuilder().create();
        RichTextMessageEntity mixMessage = new RichTextMessageEntity(entity);
        List<MessageEntity> msgList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(entity.getContent());

        for (int i = 0, length = jsonArray.length(); i < length; i++) {
            JSONObject jsonOb = (JSONObject) jsonArray.opt(i);
            int displayType = jsonOb.getInt("displayType");
            String jsonMessage = jsonOb.toString();
            switch (displayType) {
                case AppConstant.DBConstant.SHOW_TYPE_PLAIN_TEXT: {
                    TextMessageEntity textMessage = gson.fromJson(jsonMessage, TextMessageEntity.class);
                    textMessage.setSessionKey(entity.getSessionKey());
                    msgList.add(textMessage);
                }
                break;

                case AppConstant.DBConstant.SHOW_TYPE_IMAGE:
                    ImageMessageEntity imageMessage = gson.fromJson(jsonMessage, ImageMessageEntity.class);
                    imageMessage.setSessionKey(entity.getSessionKey());
                    msgList.add(imageMessage);
                    break;
            }
        }
        mixMessage.setMsgList(msgList);
        return mixMessage;
    }

    public void setMsgList(List<MessageEntity> msgList) {
        this.msgList = msgList;
    }

    public List<MessageEntity> getMsgList() {
        return msgList;
    }
}
