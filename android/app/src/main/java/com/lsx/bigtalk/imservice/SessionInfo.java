package com.lsx.bigtalk.imservice;

import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.DB.entity.SessionEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.imservice.entity.UnreadMessageEntity;
import com.lsx.bigtalk.imservice.manager.IMContactManager;

import java.util.ArrayList;
import java.util.List;


public class SessionInfo {
    private String sessionKey;
    private int peerId;
    private int sessionType;
    private int latestMsgType;
    private int latestMsgId;
    private String latestMsgData;
    private int updateTime;
    private int unReadCnt;
    private String name;
    private List<String> avatar;
    private boolean isSpin = false;
    private boolean isShield = false;

    public SessionInfo() {

    }

    public SessionInfo(SessionEntity sessionEntity, UserEntity userEntity, UnreadMessageEntity unreadEntity) {
        sessionKey = sessionEntity.getSessionKey();
        peerId = sessionEntity.getPeerId();
        sessionType = DBConstant.SESSION_TYPE_SINGLE;
        latestMsgType = sessionEntity.getLatestMsgType();
        latestMsgId = sessionEntity.getLatestMsgId();
        latestMsgData = sessionEntity.getLatestMsgData();
        updateTime = sessionEntity.getUpdated();

        if (unreadEntity != null) {
            unReadCnt = unreadEntity.getUnReadCnt();
        }

        if (userEntity != null) {
            name = userEntity.getMainName();
            ArrayList<String> avatarList = new ArrayList<>();
            avatarList.add(userEntity.getAvatar());
            avatar = avatarList;
        }
    }

    public SessionInfo(SessionEntity sessionEntity, GroupEntity groupEntity, UnreadMessageEntity unreadEntity) {
        sessionKey = sessionEntity.getSessionKey();
        peerId = sessionEntity.getPeerId();
        sessionType = DBConstant.SESSION_TYPE_GROUP;
        latestMsgType = sessionEntity.getLatestMsgType();
        latestMsgId = sessionEntity.getLatestMsgId();
        latestMsgData = sessionEntity.getLatestMsgData();
        updateTime = sessionEntity.getUpdated();

        if (unreadEntity != null) {
            unReadCnt = unreadEntity.getUnReadCnt();
        }

        if (groupEntity != null) {
            name = groupEntity.getMainName();

            if (groupEntity.getStatus() == DBConstant.GROUP_STATUS_SHIELD) {
                isShield = true;
            }

            ArrayList<String> avatarList = new ArrayList<>();
            ArrayList<Integer> groundMemberIds = new ArrayList<>(groupEntity.getlistGroupMemberIds());
            for (Integer userId : groundMemberIds) {
                UserEntity entity = IMContactManager.getInstance().findContact(userId);
                if (entity != null) {
                    avatarList.add(entity.getAvatar());
                }
                if (avatarList.size() >= 4) {
                    break;
                }
            }
            avatar = avatarList;
        }
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setLatestMsgType(int latestMsgType) {
        this.latestMsgType = latestMsgType;
    }

    public int getLatestMsgType() {
        return latestMsgType;
    }

    public void setLatestMsgId(int latestMsgId) {
        this.latestMsgId = latestMsgId;
    }

    public int getLatestMsgId() {
        return latestMsgId;
    }

    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public String getLatestMsgData() {
        return latestMsgData;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUnReadCnt(int unReadCnt) {
        this.unReadCnt = unReadCnt;
    }

    public int getUnReadCnt() {
        return unReadCnt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAvatar(List<String> avatar) {
        this.avatar = avatar;
    }

    public List<String> getAvatar() {
        return avatar;
    }

    public void setIsSpin(boolean isSpin) {
        this.isSpin = isSpin;
    }

    public boolean getIsSpin() {
        return isSpin;
    }

    public void setIsShield(boolean isShield) {
        this.isShield = isShield;
    }

    public boolean getIsShield() {
        return isShield;
    }
}
