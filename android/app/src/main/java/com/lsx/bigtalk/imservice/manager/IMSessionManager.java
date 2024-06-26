package com.lsx.bigtalk.imservice.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.lsx.bigtalk.DB.DBInterface;
import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.entity.PeerEntity;
import com.lsx.bigtalk.DB.entity.SessionEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.DB.sp.ConfigurationSp;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.imservice.SessionInfo;
import com.lsx.bigtalk.imservice.entity.UnreadMessageEntity;
import com.lsx.bigtalk.imservice.event.SessionEvent;
import com.lsx.bigtalk.protobuf.IMBaseDefine;
import com.lsx.bigtalk.protobuf.IMBuddy;
import com.lsx.bigtalk.protobuf.helper.EntityChangeEngine;
import com.lsx.bigtalk.protobuf.helper.Java2ProtoBuf;
import com.lsx.bigtalk.protobuf.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.utils.Logger;

import de.greenrobot.event.EventBus;


public class IMSessionManager extends IMManager {
    @SuppressLint("StaticFieldLeak")
    private static IMSessionManager instance;
    private final Logger logger = Logger.getLogger(IMSessionManager.class);
    private final IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private final IMLoginManager imLoginManager = IMLoginManager.getInstance();
    private final DBInterface dbInterface = DBInterface.instance();
    private final IMGroupManager groupManager = IMGroupManager.getInstance();
    // key = sessionKey -->  sessionType_peerId
    private final Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();
    private boolean sessionListReady = false;

    public static synchronized IMSessionManager getInstance() {
        if (null == instance) {
            instance = new IMSessionManager();
        }
        return instance;
    }

    private static void sort(List<SessionInfo> data) {
        data.sort(new Comparator<SessionInfo>() {
            public int compare(SessionInfo o1, SessionInfo o2) {
                Integer a = o1.getUpdateTime();
                Integer b = o2.getUpdateTime();

                boolean isTopA = o1.getIsSpin();
                boolean isTopB = o2.getIsSpin();

                if (isTopA == isTopB) {
                    // 降序
                    return b.compareTo(a);
                } else {
                    if (isTopA) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        });
    }

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        sessionListReady = false;
        sessionMap.clear();
    }

    public void triggerEvent(SessionEvent event) {
        if (event == SessionEvent.SESSION_LIST_SUCCESS) {
            sessionListReady = true;
        }
        EventBus.getDefault().post(event);
    }

    public void onNormalLoginOk() {
        logger.d("IMSessionManager#onNormalLoginOk");
        onLocalLoginOk();
        onLocalNetOk();
    }

    public void onLocalLoginOk() {
        logger.i("IMSessionManager#onLocalLoginOk load session from db");
        List<SessionEntity> sessionInfoList = dbInterface.loadAllSession();
        for (SessionEntity sessionInfo : sessionInfoList) {
            sessionMap.put(sessionInfo.getSessionKey(), sessionInfo);
        }

        triggerEvent(SessionEvent.SESSION_LIST_SUCCESS);
    }

    public void onLocalNetOk() {
        int latestUpdateTime = dbInterface.getSessionLastTime();
        logger.d("IMSessionManager#onLocalNetOk fetch session from server, latestUpdateTime:%d", latestUpdateTime);
        fetchRecentSessions(latestUpdateTime);
    }

    private void fetchRecentSessions(int latestUpdateTime) {
        logger.i("IMSessionManager#fetchRecentSessions latestUpdateTime:%d", latestUpdateTime);
        int loginId = IMLoginManager.getInstance().getLoginId();
        IMBuddy.IMRecentContactSessionReq recentContactSessionReq = IMBuddy.IMRecentContactSessionReq
                .newBuilder()
                .setLatestUpdateTime(latestUpdateTime)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_RECENT_CONTACT_SESSION_REQUEST_VALUE;
        imSocketManager.sendRequest(recentContactSessionReq, sid, cid);
    }

    public void onRepRecentContacts(IMBuddy.IMRecentContactSessionRsp recentContactSessionRsp) {
        logger.i("session#onRepRecentContacts");
        int userId = recentContactSessionRsp.getUserId();
        List<IMBaseDefine.ContactSessionInfo> contactSessionInfoList = recentContactSessionRsp.getContactSessionListList();
        logger.i("contact#user:%d  cnt:%d", userId, contactSessionInfoList.size());
        /**更新最近联系人列表*/

        ArrayList<SessionEntity> needDb = new ArrayList<>();
        for (IMBaseDefine.ContactSessionInfo sessionInfo : contactSessionInfoList) {
            // 返回的没有主键Id
            SessionEntity sessionEntity = ProtoBuf2JavaBean.getSessionEntity(sessionInfo);
            //并没有按照时间来排序
            sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
            needDb.add(sessionEntity);
        }
        logger.d("session#onRepRecentContacts is ready,now broadcast");

        //将最新的session信息保存在DB中
        dbInterface.batchInsertOrUpdateSession(needDb);
        if (!needDb.isEmpty()) {
            triggerEvent(SessionEvent.SESSION_UPDATE);
        }
    }

    /**
     * 请求删除会话
     */
    public void reqRemoveSession(SessionInfo SessionInfo) {
        logger.i("session#reqRemoveSession");

        int loginId = imLoginManager.getLoginId();
        String sessionKey = SessionInfo.getSessionKey();
        /**直接本地先删除,清楚未读消息*/
        if (sessionMap.containsKey(sessionKey)) {
            sessionMap.remove(sessionKey);
            IMUnreadMsgManager.getInstance().readUnreadSession(sessionKey);
            dbInterface.deleteSession(sessionKey);
            ConfigurationSp.instance(ctx, loginId).setSessionTop(sessionKey, false);
            triggerEvent(SessionEvent.SESSION_UPDATE);
        }

        IMBuddy.IMRemoveSessionReq removeSessionReq = IMBuddy.IMRemoveSessionReq
                .newBuilder()
                .setUserId(loginId)
                .setSessionId(SessionInfo.getPeerId())
                .setSessionType(Java2ProtoBuf.getProtoSessionType(SessionInfo.getSessionType()))
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_REQ_VALUE;
        imSocketManager.sendRequest(removeSessionReq, sid, cid);
    }

    /**
     * 删除会话返回
     */
    public void onRepRemoveSession(IMBuddy.IMRemoveSessionRsp removeSessionRsp) {
        logger.i("session#onRepRemoveSession");
        int resultCode = removeSessionRsp.getResultCode();
        if (0 != resultCode) {
            logger.e("session#removeSession failed");
        }
    }

    /**
     * 新建群组时候的更新
     */
    public void updateSession(GroupEntity entity) {
        logger.d("recent#updateSession GroupEntity:%s", entity);
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setLatestMsgType(DBConstant.MSG_TYPE_GROUP_TEXT);
        sessionEntity.setUpdated(entity.getUpdated());
        sessionEntity.setCreated(entity.getCreated());
        sessionEntity.setLatestMsgData("[你创建的新群喔]");
        sessionEntity.setTalkId(entity.getCreatorId());
        sessionEntity.setLatestMsgId(0);
        sessionEntity.setPeerId(entity.getPeerId());
        sessionEntity.setPeerType(DBConstant.SESSION_TYPE_GROUP);
        sessionEntity.buildSessionKey();

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);
        triggerEvent(SessionEvent.SESSION_UPDATE);
    }

    /**
     * 1.自己发送消息
     * 2.收到消息
     *
     * @param msg
     */
    public void updateSession(MessageEntity msg) {
        logger.d("recent#updateSession msg:%s", msg);
        if (msg == null) {
            logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }
        int loginId = imLoginManager.getLoginId();
        boolean isSend = msg.isSend(loginId);
        // 因为多端同步的问题
        int peerId = msg.getPeerId(isSend);

        SessionEntity sessionEntity = sessionMap.get(msg.getSessionKey());
        if (sessionEntity == null) {
            logger.d("session#updateSession#not found msgSessionEntity");
            sessionEntity = EntityChangeEngine.getSessionEntity(msg);
            sessionEntity.setPeerId(peerId);
            sessionEntity.buildSessionKey();
            // 判断群组的信息是否存在
            if (sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP) {
                GroupEntity groupEntity = groupManager.findGroup(peerId);
                if (groupEntity == null) {
                    groupManager.fetchGroupDetailInfo(peerId);
                }
            }
        } else {
            logger.d("session#updateSession#msgSessionEntity already in Map");
            sessionEntity.setUpdated(msg.getUpdated());
            sessionEntity.setLatestMsgData(msg.getMessageDisplay());
            sessionEntity.setTalkId(msg.getFromId());
            //todo check if msgid is null/0
            sessionEntity.setLatestMsgId(msg.getMsgId());
            sessionEntity.setLatestMsgType(msg.getMsgType());
        }

        /**DB 先更新*/
        ArrayList<SessionEntity> needDb = new ArrayList<>(1);
        needDb.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDb);

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        triggerEvent(SessionEvent.SESSION_UPDATE);
    }

    public List<SessionEntity> getRecentSessionList() {
        List<SessionEntity> SessionInfoList = new ArrayList<>(sessionMap.values());
        return SessionInfoList;
    }

    // 获取最近联系人列表，SessionInfo 是sessionEntity unreadEntity user/group 等等实体的封装
    // todo every time it has to sort, kind of inefficient, change it
    public List<SessionInfo> getRecentListInfo() {
        /**整理topList*/
        List<SessionInfo> recentSessionList = new ArrayList<>();
        int loginId = IMLoginManager.getInstance().getLoginId();

        List<SessionEntity> sessionList = getRecentSessionList();
        Map<Integer, UserEntity> userMap = IMContactManager.getInstance().getUserMap();
        Map<String, UnreadMessageEntity> unreadMsgMap = IMUnreadMsgManager.getInstance().getUnreadMsgMap();
        Map<Integer, GroupEntity> groupEntityMap = IMGroupManager.getInstance().getGroupMap();
        HashSet<String> topList = ConfigurationSp.instance(ctx, loginId).getSessionTopList();


        for (SessionEntity recentSession : sessionList) {
            int sessionType = recentSession.getPeerType();
            int peerId = recentSession.getPeerId();
            String sessionKey = recentSession.getSessionKey();

            UnreadMessageEntity unreadEntity = unreadMsgMap.get(sessionKey);
            if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
                GroupEntity groupEntity = groupEntityMap.get(peerId);
                SessionInfo SessionInfo = new SessionInfo(recentSession, groupEntity, unreadEntity);
                if (topList != null && topList.contains(sessionKey)) {
                    SessionInfo.setIsSpin(true);
                }

                //谁说的这条信息，只有群组需要，例如 【XXX:您好】
                int lastFromId = recentSession.getTalkId();
                UserEntity talkUser = userMap.get(lastFromId);
                // 用户已经不存在了
                if (talkUser != null) {
                    String oriContent = SessionInfo.getLatestMsgData();
                    String finalContent = talkUser.getMainName() + ": " + oriContent;
                    SessionInfo.setLatestMsgData(finalContent);
                }
                recentSessionList.add(SessionInfo);
            } else if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
                UserEntity userEntity = userMap.get(peerId);
                SessionInfo SessionInfo = new SessionInfo(recentSession, userEntity, unreadEntity);
                if (topList != null && topList.contains(sessionKey)) {
                    SessionInfo.setIsSpin(true);
                }
                recentSessionList.add(SessionInfo);
            }
        }
        sort(recentSessionList);
        return recentSessionList;
    }


    public SessionEntity findSession(String sessionKey) {
        if (sessionMap.size() <= 0 || TextUtils.isEmpty(sessionKey)) {
            return null;
        }
        if (sessionMap.containsKey(sessionKey)) {
            return sessionMap.get(sessionKey);
        }
        return null;
    }

    public PeerEntity findPeerEntity(String sessionKey) {
        if (TextUtils.isEmpty(sessionKey)) {
            return null;
        }
        // 拆分
        PeerEntity peerEntity;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);
        switch (peerType) {
            case DBConstant.SESSION_TYPE_SINGLE: {
                peerEntity = IMContactManager.getInstance().findContact(peerId);
            }
            break;
            case DBConstant.SESSION_TYPE_GROUP: {
                peerEntity = IMGroupManager.getInstance().findGroup(peerId);
            }
            break;
            default:
                throw new IllegalArgumentException("findPeerEntity#peerType is illegal,cause by " + peerType);
        }
        return peerEntity;
    }

    /**
     * ------------------------实体的get set-----------------------------
     */
    public boolean isSessionListReady() {
        return sessionListReady;
    }

    public void setSessionListReady(boolean sessionListReady) {
        this.sessionListReady = sessionListReady;
    }
}
