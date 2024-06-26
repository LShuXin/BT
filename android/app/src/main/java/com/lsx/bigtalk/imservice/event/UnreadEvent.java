package com.lsx.bigtalk.imservice.event;

import com.lsx.bigtalk.imservice.entity.UnreadMessageEntity;


public class UnreadEvent {
    public UnreadMessageEntity entity;
    public Event event;

    public UnreadEvent() {}
    public UnreadEvent(Event event){
        this.event = event;
    }

    public enum Event {
        UNREAD_MSG_LIST_OK,
        UNREAD_MSG_RECEIVED,
        SESSION_READ_UNREAD_MSG
    }
}
