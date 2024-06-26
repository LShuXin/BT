package com.lsx.bigtalk.imservice.manager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.lsx.bigtalk.DB.entity.PeerEntity;
import com.lsx.bigtalk.DB.entity.SessionEntity;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.DB.DBInterface;
import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.config.MessageConstant;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.callback.Packetlistener;
import com.lsx.bigtalk.imservice.entity.AudioMessageEntity;
import com.lsx.bigtalk.imservice.entity.ImageMessageEntity;
import com.lsx.bigtalk.imservice.entity.TextMessageEntity;
import com.lsx.bigtalk.imservice.event.MessageEvent;
import com.lsx.bigtalk.imservice.event.PriorityEvent;
import com.lsx.bigtalk.imservice.event.HistoryMsgRefreshEvent;
import com.lsx.bigtalk.imservice.service.LoadImageService;
import com.lsx.bigtalk.protobuf.helper.EntityChangeEngine;
import com.lsx.bigtalk.protobuf.helper.Java2ProtoBuf;
import com.lsx.bigtalk.protobuf.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.protobuf.IMBaseDefine;
import com.lsx.bigtalk.protobuf.IMMessage;
import com.lsx.bigtalk.imservice.support.SequenceNumberMaker;
import com.lsx.bigtalk.utils.Logger;

import de.greenrobot.event.EventBus;


public class IMMessageManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMMessageManager.class);
    private final IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private final IMSessionManager imSessionManager = IMSessionManager.getInstance();
    private final DBInterface dbInterface = DBInterface.instance();
    @SuppressLint("StaticFieldLeak")
    private static IMMessageManager instance;
    public static IMMessageManager getInstance() {
        if (null == instance) {
            instance = new IMMessageManager();
        }
        return instance;
    }

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        EventBus.getDefault().unregister(instance);
    }
    
    private long getTimeoutTolerance(MessageEntity msg) {
        if (msg.getDisplayType() == DBConstant.SHOW_TYPE_IMAGE) {
            return 4 * 60 * 1000;
        }
        return 6 * 1000;
    }
    
    public void ackReceiveMsg(MessageEntity msg) {
        logger.d("IMMessageManager#ackReceiveMsg -> msg:%s", msg);
        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(msg.getSessionType());
        IMMessage.IMMsgDataAck imMsgDataAck = IMMessage.IMMsgDataAck.newBuilder()
                .setMsgId(msg.getMsgId())
                .setSessionId(msg.getToId())
                .setUserId(msg.getFromId())
                .setSessionType(sessionType)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_ACK_VALUE;
        imSocketManager.sendRequest(imMsgDataAck, sid, cid);
    }
    
    public void onLoginSuccess() {
        if (!EventBus.getDefault().isRegistered(instance)) {
            EventBus.getDefault().register(instance);
        }
    }
    
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }
    
    public void onEvent(MessageEvent event) {
        MessageEvent.Event type = event.getEvent();
        switch (type) {
            case IMAGE_UPLOAD_FAILURE:
            {
                logger.d("IMMessageManager#onEvent#IMAGE_UPLOAD_FAILURE");
                ImageMessageEntity imageMessage = (ImageMessageEntity)event.getMessageEntity();
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                imageMessage.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(imageMessage);
                event.setEvent(MessageEvent.Event.IMAGE_UPLOAD_FAILED);
                event.setMessageEntity(imageMessage);
                triggerEvent(event);
                break;
            }
            case IMAGE_UPLOAD_SUCCESS:
            {
                logger.d("IMMessageManager#onEvent#IMAGE_UPLOAD_SUCCESS");
                onImageLoadSuccess(event);
                break;
            }
        }
    }

    /**
     * 事件的处理会在一个后台线程中执行，对应的函数名是onEventBackgroundThread，
     * 虽然名字是BackgroundThread，事件处理是在后台线程，
     * 但事件处理时间还是不应该太长
     * 因为如果发送事件的线程是后台线程，会直接执行事件，
     * 如果当前线程是UI线程，事件会被加到一个队列中，由一个线程依次处理这些事件，
     * 如果某个事件处理时间太长，会阻塞后面的事件的派发或处理
     * */
    public void onEventBackgroundThread(HistoryMsgRefreshEvent historyMsgEvent) {
        refreshLocalMsg(historyMsgEvent);
    }


    /**----------------------底层的接口-------------------------------------*/
    /**
     * 发送消息，最终的状态情况
     * MessageManager下面的拆分
     * 应该是自己发的信息，所以msgId为0
     * 这个地方用DB id作为主键
     */
    public void sendMessage(MessageEntity msgEntity) {
        logger.d("IMMessageManager#sendMessage, msg:%s", msgEntity);
        // 发送情况下 msg_id 都是0
        // 服务端是从1开始计数的
        if (!SequenceNumberMaker.getInstance().isFailure(msgEntity.getMsgId())) {
            throw new RuntimeException("#sendMessage# msgId is wrong,cause by 0!");
        }

        IMBaseDefine.MsgType msgType = Java2ProtoBuf.getProtoMsgType(msgEntity.getMsgType());
        byte[] sendContent = msgEntity.getSendContent();


        IMMessage.IMMsgData msgData = IMMessage.IMMsgData.newBuilder()
                .setFromUserId(msgEntity.getFromId())
                .setToSessionId(msgEntity.getToId())
                .setMsgId(0)
                .setCreateTime(msgEntity.getCreated())
                .setMsgType(msgType)
                .setMsgData(ByteString.copyFrom(sendContent))  // 这个点要特别注意 todo ByteString.copyFrom
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE;

        final MessageEntity messageEntity = msgEntity;
        imSocketManager.sendRequest(msgData, sid, cid, new Packetlistener(getTimeoutTolerance(messageEntity)) {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMMessage.IMMsgDataAck imMsgDataAck = IMMessage.IMMsgDataAck.parseFrom((CodedInputStream)response);
                    logger.i("IMMessageManager#sendMessage#onSuccess");

                    if (imMsgDataAck.getMsgId() <= 0) {
                        throw  new RuntimeException("Msg ack error,cause by msgId <=0");
                    }
                    messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
                    messageEntity.setMsgId(imMsgDataAck.getMsgId());
                    dbInterface.insertOrUpdateMessage(messageEntity);
                    imSessionManager.updateSession(messageEntity);
                    triggerEvent(new MessageEvent(MessageEvent.Event.SEND_MESSAGE_SUCCESS, messageEntity));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFaild() {
                logger.i("IMMessageManager#sendMessage#onFaild");
                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(messageEntity);
                triggerEvent(new MessageEvent(MessageEvent.Event.SEND_MESSAGE_FAILED, messageEntity));
            }

            @Override
            public void onTimeout() {
                logger.i("IMMessageManager#sendMessage#onTimeout");
                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(messageEntity);
                triggerEvent(new MessageEvent(MessageEvent.Event.SEND_MESSAGE_TIMEOUT, messageEntity));
            }
        });
    }

    public void onReceiveMessage(IMMessage.IMMsgData imMsgData) {
        logger.i("IMMessageManager#onRecvMessage");
        if (imMsgData == null) {
            logger.i("IMMessageManager#onRecvMessage#null");
            return;
        }

        MessageEntity receivedMessage = ProtoBuf2JavaBean.getMessageEntity(imMsgData);
        int loginId = IMLoginManager.getInstance().getLoginId();
        boolean isSend = receivedMessage.isSend(loginId);
        receivedMessage.buildSessionKey(isSend);
        receivedMessage.setStatus(MessageConstant.MSG_SUCCESS);

        dbInterface.insertOrUpdateMessage(receivedMessage);
        imSessionManager.updateSession(receivedMessage);

        PriorityEvent notifyEvent = new PriorityEvent();
        notifyEvent.event = PriorityEvent.Event.MSG_RECEIVED_MESSAGE;
        notifyEvent.object = receivedMessage;
        triggerEvent(notifyEvent);
    }
    
	public void sendText(TextMessageEntity textMessage) {
        logger.i("IMMessageManager#sendText");
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        DBInterface.instance().insertOrUpdateMessage(textMessage);
        imSessionManager.updateSession(textMessage);
		sendMessage(textMessage);
	}

	public void sendVoice(AudioMessageEntity audioMessage) {
		logger.i("IMMessageManager#audio#sendVoice");
        audioMessage.setStatus(MessageConstant.MSG_SENDING);
        DBInterface.instance().insertOrUpdateMessage(audioMessage);
        imSessionManager.updateSession(audioMessage);
		sendMessage(audioMessage);
	}
    
    public void sendSingleImage(ImageMessageEntity msg) {
        logger.d("IMMessageManager#sendSingleImage");
        ArrayList<ImageMessageEntity> msgList = new ArrayList<>();
        msgList.add(msg);
        sendImages(msgList);
    }
    
    public void sendImages(List<ImageMessageEntity> msgList) {
        logger.i("IMMessageManager#sendImages#size:%d", msgList.size());
        if (msgList.isEmpty()) {
            return ;
        }

        int len = msgList.size();
        ArrayList<MessageEntity> needDbList = new ArrayList<>(msgList);
        DBInterface.instance().batchInsertOrUpdateMessage(needDbList);

		for (ImageMessageEntity msg : msgList) {
			logger.d("IMMessageManager#sendImage msg:%s", msg);
            int loadStatus = msg.getLoadStatus();
            switch (loadStatus) {
                case MessageConstant.IMAGE_LOADED_FAILURE:
                case MessageConstant.IMAGE_UNLOAD:
                case MessageConstant.IMAGE_LOADING:
                {
                    msg.setLoadStatus(MessageConstant.IMAGE_LOADING);
                    Intent loadImageIntent = new Intent(ctx, LoadImageService.class);
                    loadImageIntent.putExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS, msg);
                    ctx.startService(loadImageIntent);
                    break;
                }

                case MessageConstant.IMAGE_LOADED_SUCCESS:
                {
                    sendMessage(msg);
                    break;
                }
                default:
                    throw new RuntimeException("sendImages#status不可能出现的状态");
            }
		}
        imSessionManager.updateSession(msgList.get(len - 1));
	}


	public void resendMessage(MessageEntity msgInfo) {
        if (msgInfo == null) {
            return;
        }

        if (!SequenceNumberMaker.getInstance().isFailure(msgInfo.getMsgId())) {
            msgInfo.setStatus(MessageConstant.MSG_SUCCESS);
            dbInterface.insertOrUpdateMessage(msgInfo);
            triggerEvent(new MessageEvent(MessageEvent.Event.SEND_MESSAGE_SUCCESS, msgInfo));
            return;
        }
        
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        msgInfo.setUpdated(nowTime);
        msgInfo.setCreated(nowTime);
        
        int msgType = msgInfo.getDisplayType();
        switch (msgType) {
            case DBConstant.SHOW_TYPE_PLAIN_TEXT:
                  sendText((TextMessageEntity)msgInfo);
                  break;
            case DBConstant.SHOW_TYPE_IMAGE:
                sendSingleImage((ImageMessageEntity) msgInfo);
                break;
            case DBConstant.SHOW_TYPE_AUDIO:
                sendVoice((AudioMessageEntity)msgInfo); break;
            default:
                throw new IllegalArgumentException("#resendMessage#enum type is wrong!!,cause by displayType" + msgType);
        }
	}
    
    public List<MessageEntity> loadHistoryMsg(int pullTimes, String sessionKey, PeerEntity peerEntity) {
        int lastMsgId = 99999999;
        int lastCreateTime = 1455379200;
        int count = SysConstant.MSG_PAGE_SIZE;
        SessionEntity sessionEntity = IMSessionManager.getInstance().findSession(sessionKey);
        if (sessionEntity != null) {
            // 以前已经聊过天，删除之后，sessionEntity不存在
            Log.i("LShuXin", "#loadHistoryMsg# sessionEntity is not null");
            lastMsgId = sessionEntity.getLatestMsgId();
            // 这个地方设定有问题，先使用最大的时间, session 的 update 设定存在问题
            // lastCreateTime = sessionEntity.getUpdated();
        }

        if (lastMsgId < 1 || TextUtils.isEmpty(sessionKey)) {
            Log.i("LShuXin", "lastMsgId < 1 || TextUtils.isEmpty(sessionKey)");
            return Collections.emptyList();
        }

        // 确保拉取消息的数量要小于消息总数量
        if (count > lastMsgId) {
            count = lastMsgId;
        }

        return doLoadHistoryMsg(
                pullTimes,
                peerEntity.getPeerId(),
                peerEntity.getType(),
                sessionKey,
                lastMsgId,
                lastCreateTime,
                count);
    }

    // 根据次数有点粗暴
    public List<MessageEntity> loadHistoryMsg(MessageEntity entity, int pullTimes){
        int reqLastMsgId = entity.getMsgId() - 1;
        int loginId = IMLoginManager.getInstance().getLoginId();
        int reqLastCreateTime = entity.getCreated();
        String chatKey = entity.getSessionKey();
        int cnt = SysConstant.MSG_PAGE_SIZE;
        return doLoadHistoryMsg(
                pullTimes,
                entity.getPeerId(entity.isSend(loginId)),
                entity.getSessionType(),
                chatKey, reqLastMsgId, reqLastCreateTime, cnt);
    }


    private List<MessageEntity> doLoadHistoryMsg(
        int pullTimes,
        final int peerId,
        final int peerType,
        final String sessionKey,
        int lastMsgId,
        int lastCreateTime,
        int count
    ) {
        if (lastMsgId < 1 || TextUtils.isEmpty(sessionKey)) {
            return Collections.emptyList();
        }
        if (count > lastMsgId) {
            count = lastMsgId;
        }
        // 降序结果输出desc
        List<MessageEntity> listMsg = dbInterface.getHistoryMsg(sessionKey, lastMsgId, lastCreateTime, count);
        Log.i("LShuXin", "dbInterface.getHistoryMsg(" + sessionKey + ", " + lastMsgId + ", " + lastCreateTime + ", " + count + ")");
        // asyn task refresh
        int resSize = listMsg.size();
        Log.i("LShuXin", "LoadHistoryMsg return size is " + resSize);
        if (resSize == 0 || pullTimes == 1 || pullTimes %3 == 0) {
            HistoryMsgRefreshEvent historyMsgEvent = new HistoryMsgRefreshEvent();
            historyMsgEvent.pullTimes = pullTimes;
            historyMsgEvent.count = count;
            historyMsgEvent.lastMsgId = lastMsgId;
            historyMsgEvent.listMsg = listMsg;
            historyMsgEvent.peerId = peerId;
            historyMsgEvent.peerType = peerType;
            historyMsgEvent.sessionKey = sessionKey;
            triggerEvent(historyMsgEvent);
        }
        return listMsg;
    }

    private void refreshLocalMsg(HistoryMsgRefreshEvent historyMsgRefreshEvent) {
        int lastSuccessMsgId = historyMsgRefreshEvent.lastMsgId;
        List<MessageEntity> listMsg = historyMsgRefreshEvent.listMsg;

        int resSize = listMsg.size();
        if (historyMsgRefreshEvent.pullTimes > 1) {
            for (int index = resSize - 1; index >= 0; index--) {
                MessageEntity entity = listMsg.get(index);
                if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                    lastSuccessMsgId = entity.getMsgId();
                    break;
                }
            }
        }else{
            /**是第一次拉取*/
            if(SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId))
            /**正序第一个*/
                for(MessageEntity entity:listMsg){
                    if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                        lastSuccessMsgId = entity.getMsgId();
                        break;
                    }
                }
        }

        final int refreshCnt = historyMsgRefreshEvent.count * 3;
        int peerId = historyMsgRefreshEvent.peerId;
        int peerType = historyMsgRefreshEvent.peerType;
        String sessionKey = historyMsgRefreshEvent.sessionKey;
        boolean localFailure =  SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId);
        if(localFailure){
            logger.e("LoadHistoryMsg# all msg is failure!");
            if(historyMsgRefreshEvent.pullTimes ==1){
                fetchHistoryMsg(peerId,peerType,lastSuccessMsgId,refreshCnt);
            }
        }else {
            /**正常*/
            updateMissedMsg(peerId, peerType, sessionKey, lastSuccessMsgId, refreshCnt);
        }
    }

    public void updateMissedMsg(int sessionId, int sessionType, String chatKey, int msgIdEnd, int msgCnt) {
        if (msgIdEnd < 1) {
            return;
        }
        int msgIdBeg = msgIdEnd - msgCnt;
        if (msgIdBeg < 1) {
            msgIdBeg = 1;
        }

        List<Integer> msgIdList = dbInterface.refreshHistoryMsgId(chatKey, msgIdBeg, msgIdBeg);
        if (msgIdList.size() == msgIdEnd - msgIdBeg + 1) {
            logger.d("IMMessageManager#updateMissedMsg#no message missed");
            return;
        }
        List<Integer> missedMsgIds = new ArrayList<>();
        for (int i = msgIdBeg; i <= msgIdEnd; i++) {
            if (!msgIdList.contains(i)) {
                missedMsgIds.add(i);
            }
        }
        if (!missedMsgIds.isEmpty()) {
            fetchMsgById(sessionId, sessionType, missedMsgIds);
        }
    }

    private void fetchMsgById(int sessionId, int sessionType, List<Integer> msgIds) {
        int userId = IMLoginManager.getInstance().getLoginId();
        IMBaseDefine.SessionType protoSessionType = Java2ProtoBuf.getProtoSessionType(sessionType);
        IMMessage.IMGetMsgByIdReq imGetMsgByIdReq = IMMessage.IMGetMsgByIdReq.newBuilder()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setSessionType(protoSessionType)
                .addAllMsgIdList(msgIds)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_GET_BY_MSG_ID_REQ_VALUE;
        imSocketManager.sendRequest(imGetMsgByIdReq, sid, cid);
    }

    public void handleFetchMsgByIdResp(IMMessage.IMGetMsgByIdRsp resp) {
        int userId = resp.getUserId();
        int sessionId = resp.getSessionId();
        int sessionType = ProtoBuf2JavaBean.getJavaSessionType(resp.getSessionType());
        String sessionKey = EntityChangeEngine.getSessionKey(sessionId, sessionType);
        List<IMBaseDefine.MsgInfo> msgList = resp.getMsgListList();
        if (msgList.isEmpty()) {
            return;
        }
        List<MessageEntity> dbEntity = new ArrayList<>();
        for (IMBaseDefine.MsgInfo msg : msgList) {
            MessageEntity entity = ProtoBuf2JavaBean.getMessageEntity(msg);
            if (entity == null) {
                continue;
            }

            entity.setSessionKey(sessionKey);
            switch (sessionType) {
                case DBConstant.SESSION_TYPE_GROUP:
                {
                    entity.setToId(sessionId);
                    break;
                }
                case DBConstant.SESSION_TYPE_SINGLE:
                {
                    if (entity.getFromId() == userId) {
                        entity.setToId(sessionId);
                    } else {
                        entity.setToId(userId);
                    }
                    break;
                }
            }

            dbEntity.add(entity);
        }
        dbInterface.batchInsertOrUpdateMessage(dbEntity);
        MessageEvent event = new MessageEvent();
        event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAINED);
        triggerEvent(event);
    }

    public  void fetchHistoryMsg(int sessionId, int sessionType, int lastMsgId, int cnt) {
        int loginId = IMLoginManager.getInstance().getLoginId();

        IMMessage.IMGetMsgListReq req = IMMessage.IMGetMsgListReq.newBuilder()
                .setUserId(loginId)
                .setSessionType(Java2ProtoBuf.getProtoSessionType(sessionType))
                .setSessionId(sessionId)
                .setMsgIdBegin(lastMsgId)
                .setMsgCnt(cnt)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
        int cid = IMBaseDefine.MessageCmdID.CID_MSG_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(req, sid, cid);
    }
    
     public void handleFetchHistoryMsgResp(IMMessage.IMGetMsgListRsp rsp) {
           int userId = rsp.getUserId();
           int sessionType = ProtoBuf2JavaBean.getJavaSessionType(rsp.getSessionType());
           int sessionId = rsp.getSessionId();
           String sessionKey = EntityChangeEngine.getSessionKey(sessionId, sessionType);
           List<IMBaseDefine.MsgInfo> msgList = rsp.getMsgListList();
           ArrayList<MessageEntity> result = new ArrayList<>();
           for (IMBaseDefine.MsgInfo msgInfo : msgList) {
               MessageEntity messageEntity = ProtoBuf2JavaBean.getMessageEntity(msgInfo);
               if (messageEntity == null) {
                   continue;
               }
               messageEntity.setSessionKey(sessionKey);
               switch (sessionType) {
                   case DBConstant.SESSION_TYPE_GROUP:
                   {
                       messageEntity.setToId(sessionId);
                       break;
                   }
                   case DBConstant.SESSION_TYPE_SINGLE:
                   {
                       if (messageEntity.getFromId() == userId) {
                           messageEntity.setToId(sessionId);
                       } else {
                           messageEntity.setToId(userId);
                       }
                       break;
                   }
               }
               result.add(messageEntity);
            }

         if (!result.isEmpty()) {
             dbInterface.batchInsertOrUpdateMessage(result);
             MessageEvent event = new MessageEvent();
             event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAINED);
             triggerEvent(event);
         }
     }
     
    private void onImageLoadSuccess(MessageEvent messageEvent) {
        ImageMessageEntity imageMessageEntity = (ImageMessageEntity)messageEvent.getMessageEntity();
        String imageUrl = imageMessageEntity.getUrl();
        String realImageUrl = "";
        try {
            realImageUrl = URLDecoder.decode(imageUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.e(e.toString());
        }

        imageMessageEntity.setUrl(realImageUrl);
        imageMessageEntity.setStatus(MessageConstant.MSG_SUCCESS);
        imageMessageEntity.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
        dbInterface.insertOrUpdateMessage(imageMessageEntity);

        messageEvent.setEvent(MessageEvent.Event.IMAGE_UPLOAD_SUCCESS);
        messageEvent.setMessageEntity(imageMessageEntity);
        triggerEvent(messageEvent);

        imageMessageEntity.setContent(MessageConstant.IMAGE_MSG_PREFIX
                + realImageUrl + MessageConstant.IMAGE_MSG_SUFFIX);
        sendMessage(imageMessageEntity);
    }

//    /**获取session内的最后一条回话*/
//    private void reqSessionLastMsgId(int sessionId,int sessionType,Packetlistener packetlistener){
//        int userId = IMLoginManager.getInstance().getLoginId();
//        IMMessage.IMGetLatestMsgIdReq latestMsgIdReq = IMMessage.IMGetLatestMsgIdReq.newBuilder()
//                .setUserId(userId)
//                .setSessionId(sessionId)
//                .setSessionType(Java2ProtoBuf.getProtoSessionType(sessionType))
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_GET_LATEST_MSG_ID_REQ_VALUE;
//        imSocketManager.sendRequest(latestMsgIdReq,sid,cid,packetlistener);
//    }
//
//    public void onReqSessionLastMsgId(IMMessage.IMGetLatestMsgIdRsp latestMsgIdRsp){
//        int lastMsgId = latestMsgIdRsp.getLatestMsgId();
//    }
}
