package com.lsx.bigtalk.imservice.event;

import java.util.List;

import com.lsx.bigtalk.DB.entity.GroupEntity;


public class GroupEvent {
    private GroupEntity groupEntity;
    private Event event;
    private int changeType;
    private List<Integer> changeList;

    public GroupEvent(Event event){
        this.event = event;
    }

    public GroupEvent(Event event, GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
        this.event = event;
    }

    public enum Event {
        NONE,

        GROUP_INFO_OK,
        GROUP_INFO_UPDATED,

        CHANGE_GROUP_MEMBER_SUCCESS,
        CHANGE_GROUP_MEMBER_FAIL,
        CHANGE_GROUP_MEMBER_TIMEOUT,

        CREATE_GROUP_OK,
        CREATE_GROUP_FAIL,
        CREATE_GROUP_TIMEOUT,

        SHIELD_GROUP_OK,
        SHIELD_GROUP_TIMEOUT,
        SHIELD_GROUP_FAIL
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeList(List<Integer> changeList) {
        this.changeList = changeList;
    }

    public List<Integer> getChangeList() {
        return changeList;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
