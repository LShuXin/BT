package com.lsx.bigtalk.service.entity;

import androidx.annotation.NonNull;

import com.lsx.bigtalk.pb.helper.EntityChangeEngine;


public class UnreadMessageEntity {
    private String sessionKey;
    private int peerId;
    private int sessionType;
    private int unReadCnt;
    private int latestMsgId;
    private String latestMsgData;
    private boolean isShield = false;

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

    public void setUnReadCnt(int unReadCnt) {
        this.unReadCnt = unReadCnt;
    }

    public int getUnReadCnt() {
        return unReadCnt;
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

    public void setIsShield(boolean isShield) {
        this.isShield = isShield;
    }

    public boolean getIsShield() {
        return isShield;
    }

    @NonNull
    @Override
    public String toString() {
        return "UnreadEntity{" +
                "sessionKey='" + sessionKey + '\'' +
                ", peerId=" + peerId +
                ", sessionType=" + sessionType +
                ", unReadCnt=" + unReadCnt +
                ", latestMsgId=" + latestMsgId +
                ", latestMsgData='" + latestMsgData + '\'' +
                ", isShield=" + isShield +
                '}';
    }

    public String buildSessionKey() {
        if (sessionType <= 0 || peerId <= 0) {
            throw new IllegalArgumentException(
                    "SessionEntity buildSessionKey error,cause by some params <=0");
        }
        sessionKey = EntityChangeEngine.getSessionKey(peerId, sessionType);
        return sessionKey;
    }
}
