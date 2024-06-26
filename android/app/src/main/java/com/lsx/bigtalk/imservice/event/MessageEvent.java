package com.lsx.bigtalk.imservice.event;

import com.lsx.bigtalk.DB.entity.MessageEntity;

import java.util.ArrayList;


public class MessageEvent {
    private ArrayList<MessageEntity> msgList;
    private Event event;

    public MessageEvent() {

    }

    public MessageEvent(Event event) {
        this.event = event;
    }

    public MessageEvent(Event event, MessageEntity entity) {
        this.event = event;
        msgList = new ArrayList<>(1);
        msgList.add(entity);
    }

    public enum Event {
        NONE,

        HISTORY_MSG_OBTAINED,
        SENDING_MESSAGE,
        SEND_MESSAGE_SUCCESS,
        SEND_MESSAGE_TIMEOUT,
        SEND_MESSAGE_FAILED,

        IMAGE_UPLOAD_FAILED,
        IMAGE_UPLOAD_FAILURE,
        HANDLER_IMAGE_UPLOAD_SUCCESS,
        IMAGE_UPLOAD_SUCCESS
    }

    public void setMessageEntity(MessageEntity messageEntity) {
        if (msgList == null) {
            msgList = new ArrayList<>();
        }
        msgList.clear();
        msgList.add(messageEntity);
    }

    public MessageEntity getMessageEntity() {
        if (msgList == null || msgList.isEmpty()){
            return null;
        }
        return msgList.get(0);
    }

    public void setMsgList(ArrayList<MessageEntity> msgList) {
        this.msgList = msgList;
    }

    public ArrayList<MessageEntity> getMsgList() {
        return msgList;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
