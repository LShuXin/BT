package com.lsx.bigtalk.service.manager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.service.entity.UnreadMessageEntity;
import com.lsx.bigtalk.service.event.UnreadEvent;
import com.lsx.bigtalk.pb.helper.EntityChangeEngine;
import com.lsx.bigtalk.pb.helper.Java2ProtoBuf;
import com.lsx.bigtalk.pb.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.IMMessage;
import com.lsx.bigtalk.logs.Logger;

import de.greenrobot.event.EventBus;


public class IMUnreadMsgManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMUnreadMsgManager.class);
    private final IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private final IMLoginManager loginManager = IMLoginManager.getInstance();
    private final ConcurrentHashMap<String, UnreadMessageEntity> unreadMsgMap = new ConcurrentHashMap<>();
    private boolean isUnreadMsgReady = false;
    @SuppressLint("StaticFieldLeak")
    private static IMUnreadMsgManager instance;
    public static synchronized IMUnreadMsgManager getInstance() {
        if (null == instance) {
            instance = new IMUnreadMsgManager();
        }
        return instance;
    }

    @Override
    public void doOnStart() {
        
    }

    @Override
    public void reset() {
        isUnreadMsgReady = false;
        unreadMsgMap.clear();
    }
    
    public void onNormalLoginOk() {
        onRemoteLoginOk();
    }

    public void onRemoteLoginOk() {
        unreadMsgMap.clear();
        fetchUnreadMsgList();
    }
    
    public synchronized void triggerEvent(UnreadEvent event) {
        if (UnreadEvent.Event.UNREAD_MSG_LISTED == event.event) {
            isUnreadMsgReady = true;
        }

        EventBus.getDefault().post(event);
    }
    
    private void fetchUnreadMsgList() {
        logger.i("IMUnreadMsgManager#fetchUnreadMsgList");
        int loginId = IMLoginManager.getInstance().getLoginId();
        IMMessage.IMUnreadMsgCntReq unreadMsgCntReq = IMMessage.IMUnreadMsgCntReq
                .newBuilder()
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_UNREAD_CNT_REQUEST_VALUE;
        imSocketManager.sendRequest(unreadMsgCntReq, sid, cid);
    }

    public void handleFetchUnreadMsgListResp(IMMessage.IMUnreadMsgCntRsp unreadMsgCntRsp) {
        logger.i("IMUnreadMsgManager#handleFetchUnreadMsgListResp");
        int totalUnreadCount = unreadMsgCntRsp.getTotalCnt();
        List<IMBaseDefine.UnreadInfo> unreadInfoList = unreadMsgCntRsp.getUnreadinfoListList();
        logger.i("IMUnreadMsgManager#unreadMsgCnt:%d, unreadMsgInfoCnt:%d", unreadInfoList.size(), totalUnreadCount);

        for (IMBaseDefine.UnreadInfo unreadInfo : unreadInfoList) {
            UnreadMessageEntity unreadEntity = ProtoBuf2JavaBean.getUnreadEntity(unreadInfo);
            processShieldField(unreadEntity);
            unreadMsgMap.put(unreadEntity.getSessionKey(), unreadEntity);
        }
        triggerEvent(new UnreadEvent(UnreadEvent.Event.UNREAD_MSG_LISTED));
    }
    
    private void processShieldField(UnreadMessageEntity unreadEntity) {
        if (AppConstant.DBConstant.SESSION_TYPE_GROUP == unreadEntity.getSessionType()) {
            GroupEntity groupEntity = IMGroupManager.getInstance().findGroup(unreadEntity.getPeerId());
            if (groupEntity != null && groupEntity.getStatus() == AppConstant.DBConstant.GROUP_STATUS_SHIELD) {
                unreadEntity.setIsShield(true);
            }
        }
    }
    
    public void setIsShield(String sessionKey, boolean isShield) {
        UnreadMessageEntity unreadEntity = unreadMsgMap.get(sessionKey);
        if (null != unreadEntity) {
            unreadEntity.setIsShield(isShield);
        }
    }

    public void add(MessageEntity msgEntity) {
        if (null == msgEntity) {
            return;
        }
        boolean isFirst = false;
        UnreadMessageEntity unreadMessageEntity;
        int loginId = IMLoginManager.getInstance().getLoginId();
        String sessionKey = msgEntity.getSessionKey();
        boolean isSend = msgEntity.isSend(loginId);
        if (isSend) {
            IMNotificationManager.getInstance().cancelSessionNotifications(sessionKey);
            return;
        }

        if (unreadMsgMap.containsKey(sessionKey)) {
            unreadMessageEntity = unreadMsgMap.get(sessionKey);
            if (null == unreadMessageEntity || unreadMessageEntity.getLatestMsgId() == msgEntity.getMsgId()) {
                return;
            }
            unreadMessageEntity.setUnReadCnt(unreadMessageEntity.getUnReadCnt() + 1);
        } else {
            isFirst = true;
            unreadMessageEntity = new UnreadMessageEntity();
            unreadMessageEntity.setUnReadCnt(1);
            unreadMessageEntity.setPeerId(msgEntity.getPeerId(false));
            unreadMessageEntity.setSessionType(msgEntity.getSessionType());
            unreadMessageEntity.buildSessionKey();
        }

        unreadMessageEntity.setLatestMsgData(msgEntity.getMessageDisplay());
        unreadMessageEntity.setLatestMsgId(msgEntity.getMsgId());
        processShieldField(unreadMessageEntity);

        unreadMsgMap.put(unreadMessageEntity.getSessionKey(), unreadMessageEntity);

        if (!unreadMessageEntity.getIsShield() || isFirst) {
            UnreadEvent unreadEvent = new UnreadEvent();
            unreadEvent.event = UnreadEvent.Event.UNREAD_MSG_RECEIVED;
            unreadEvent.entity = unreadMessageEntity;
            triggerEvent(unreadEvent);
        }
    }

    public void ackReadMsg(MessageEntity messageEntity) {
        logger.d("IMUnreadMsgManager#ackReadMsg -> msg:%s", messageEntity);
        int loginId = loginManager.getLoginId();
        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(messageEntity.getSessionType());
        IMMessage.IMMsgDataReadAck readAck = IMMessage.IMMsgDataReadAck.newBuilder()
                .setMsgId(messageEntity.getMsgId())
                .setSessionId(messageEntity.getPeerId(false))
                .setSessionType(sessionType)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_READ_ACK_VALUE;
        imSocketManager.sendRequest(readAck, sid, cid);
    }

    public void ackReadMsg(UnreadMessageEntity unreadMessageEntity) {
        logger.d("IMUnreadMsgManager#ackReadMsg -> msg:%s", unreadMessageEntity);
        int loginId = loginManager.getLoginId();
        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(unreadMessageEntity.getSessionType());
        IMMessage.IMMsgDataReadAck readAck = IMMessage.IMMsgDataReadAck.newBuilder()
                .setMsgId(unreadMessageEntity.getLatestMsgId())
                .setSessionId(unreadMessageEntity.getPeerId())
                .setSessionType(sessionType)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_READ_ACK_VALUE;
        imSocketManager.sendRequest(readAck, sid, cid);
    }


    /**
     * notification from server side has been read
     */
    public void onNotifyRead(IMMessage.IMMsgDataReadNotify readNotify) {
        logger.d("IMUnreadMsgManager#onNotifyRead");
        int triggerId = readNotify.getUserId();
        int loginId = IMLoginManager.getInstance().getLoginId();
        if (triggerId != loginId) {
            logger.i("IMUnreadMsgManager#onNotifyRead#triggerId: %s, loginId: %s not Equal", triggerId, loginId);
            return;
        }
        //现在的逻辑是msgId之后的 全部都是已读的
        // 不做复杂判断了，简单处理
        int msgId = readNotify.getMsgId();
        int peerId = readNotify.getSessionId();
        int sessionType = ProtoBuf2JavaBean.getJavaSessionType(readNotify.getSessionType());
        String sessionKey = EntityChangeEngine.getSessionKey(peerId, sessionType);

        // 通知栏也要去除掉
        NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyMgr == null) {
            return;
        }
        int notificationId = IMNotificationManager.getInstance().getSessionNotificationId(sessionKey);
        notifyMgr.cancel(notificationId);

        UnreadMessageEntity unreadSession = findUnread(sessionKey);
        if (unreadSession != null && unreadSession.getLatestMsgId() <= msgId) {
            // 清空会话session
            logger.d("IMUnreadMsgManager#onNotifyRead# unreadSession onLoginOut");
            readUnreadSession(sessionKey);
        }
    }

    public void readUnreadSession(String sessionKey) {
        logger.d("IMUnreadMsgManager#readUnreadSession#sessionKey: %s", sessionKey);
        if (unreadMsgMap.containsKey(sessionKey)) {
            UnreadMessageEntity entity = unreadMsgMap.remove(sessionKey);
            ackReadMsg(entity);
            triggerEvent(new UnreadEvent(UnreadEvent.Event.SESSION_UNREAD_MSG_READ));
        }
    }

    public UnreadMessageEntity findUnread(String sessionKey) {
        logger.d("IMUnreadMsgManager#findUnread#sessionKey: %s", sessionKey);
        if (TextUtils.isEmpty(sessionKey) || unreadMsgMap.isEmpty()) {
            logger.i("IMUnreadMsgManager#findUnread#no unread info");
            return null;
        }
        if (unreadMsgMap.containsKey(sessionKey)) {
            return unreadMsgMap.get(sessionKey);
        }
        return null;
    }
    
    public ConcurrentHashMap<String, UnreadMessageEntity> getUnreadMsgMap() {
        return unreadMsgMap;
    }

    public int getTotalUnreadCount() {
        int count = 0;
        for (UnreadMessageEntity entity : unreadMsgMap.values()) {
            if (!entity.getIsShield()) {
                count += entity.getUnReadCnt();
            }
        }
        return count;
    }

    public boolean getIsUnreadMsgReady() {
        return isUnreadMsgReady;
    }
}
