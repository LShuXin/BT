package com.lsx.bigtalk.service.event;

import com.lsx.bigtalk.service.entity.UnreadMessageEntity;


public class UnreadEvent {
    public UnreadMessageEntity entity;
    public Event event;

    public UnreadEvent() {}
    public UnreadEvent(Event event){
        this.event = event;
    }

    public enum Event {
        UNREAD_MSG_LISTED,
        UNREAD_MSG_RECEIVED,
        SESSION_UNREAD_MSG_READ
    }
}
