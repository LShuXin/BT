package com.lsx.bigtalk.pb.helper;

import com.google.protobuf.ByteString;
import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.entity.DepartmentEntity;

import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.storage.db.entity.SessionEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;

import com.lsx.bigtalk.service.entity.AudioMessageEntity;
import com.lsx.bigtalk.service.callback.MsgAnalyzeEngine;
import com.lsx.bigtalk.service.entity.UnreadMessageEntity;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.IMGroup;
import com.lsx.bigtalk.pb.IMMessage;
import com.lsx.bigtalk.utils.CommonUtil;
import com.lsx.bigtalk.utils.FileUtil;
import com.lsx.bigtalk.utils.pinyin.PinYin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 *
 */
public class ProtoBuf2JavaBean {

    public static DepartmentEntity getDepartEntity(IMBaseDefine.DepartInfo departInfo){
        DepartmentEntity departmentEntity = new DepartmentEntity();

        int timeNow = (int) (System.currentTimeMillis()/1000);

        departmentEntity.setDepartId(departInfo.getDeptId());
        departmentEntity.setDepartName(departInfo.getDeptName());
        departmentEntity.setPriority(departInfo.getPriority());
        departmentEntity.setStatus(getDepartStatus(departInfo.getDeptStatus()));

        departmentEntity.setCreated(timeNow);
        departmentEntity.setUpdated(timeNow);

        // 设定pinyin 相关
        PinYin.getPinYin(departInfo.getDeptName(), departmentEntity.getPinyinElement());

        return departmentEntity;
    }

    public static UserEntity getUserEntity(IMBaseDefine.UserInfo userInfo){
        UserEntity userEntity = new UserEntity();
        int timeNow = (int) (System.currentTimeMillis()/1000);

        userEntity.setStatus(userInfo.getStatus());
        userEntity.setAvatar(userInfo.getAvatarUrl());
        userEntity.setCreated(timeNow);
        userEntity.setDepartmentId(userInfo.getDepartmentId());
        userEntity.setEmail(userInfo.getEmail());
        userEntity.setGender(userInfo.getUserGender());
        userEntity.setMainName(userInfo.getUserNickName());
        userEntity.setPhone(userInfo.getUserTel());
        userEntity.setPinyinName(userInfo.getUserDomain());
        userEntity.setRealName(userInfo.getUserRealName());
        userEntity.setUpdated(timeNow);
        userEntity.setPeerId(userInfo.getUserId());

        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
        return userEntity;
    }

    public static SessionEntity getSessionEntity(IMBaseDefine.ContactSessionInfo sessionInfo){
        SessionEntity sessionEntity = new SessionEntity();

        int msgType = getJavaMsgType(sessionInfo.getLatestMsgType());
        sessionEntity.setLatestMsgType(msgType);
        sessionEntity.setPeerType(getJavaSessionType(sessionInfo.getSessionType()));
        sessionEntity.setPeerId(sessionInfo.getSessionId());
        sessionEntity.buildSessionKey();
        sessionEntity.setTalkId(sessionInfo.getLatestMsgFromUserId());
        sessionEntity.setLatestMsgId(sessionInfo.getLatestMsgId());
        sessionEntity.setCreated(sessionInfo.getUpdatedTime());

        String content  = sessionInfo.getLatestMsgData().toStringUtf8();
        String desMessage = new String(com.lsx.bigtalk.Security.getInstance().DecryptMsg(content));
        // 判断具体的类型是什么
        if(msgType == AppConstant.DBConstant.MSG_TYPE_GROUP_TEXT ||
                msgType ==AppConstant.DBConstant.MSG_TYPE_SINGLE_TEXT){
            desMessage =  MsgAnalyzeEngine.analyzeMessageDisplayType(desMessage);
        }

        sessionEntity.setLatestMsgData(desMessage);
        sessionEntity.setUpdated(sessionInfo.getUpdatedTime());

        return sessionEntity;
    }


    public static GroupEntity getGroupEntity(IMBaseDefine.GroupInfo groupInfo){
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis()/1000);
        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setMainName(groupInfo.getGroupName());
        groupEntity.setAvatar(groupInfo.getGroupAvatar());
        groupEntity.setCreatorId(groupInfo.getGroupCreatorId());
        groupEntity.setPeerId(groupInfo.getGroupId());
        groupEntity.setGroupType(getJavaGroupType(groupInfo.getGroupType()));
        groupEntity.setStatus(groupInfo.getShieldStatus());
        groupEntity.setUserCnt(groupInfo.getGroupMemberListCount());
        groupEntity.setVersion(groupInfo.getVersion());
        groupEntity.setlistGroupMemberIds(groupInfo.getGroupMemberListList());

        // may be not good place
        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());

        return groupEntity;
    }


    /**
     * 创建群时候的转化
     * @param groupCreateRsp
     * @return
     */
    public static GroupEntity getGroupEntity(IMGroup.IMGroupCreateRsp groupCreateRsp){
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis()/1000);
        groupEntity.setMainName(groupCreateRsp.getGroupName());
        groupEntity.setlistGroupMemberIds(groupCreateRsp.getUserIdListList());
        groupEntity.setCreatorId(groupCreateRsp.getUserId());
        groupEntity.setPeerId(groupCreateRsp.getGroupId());

        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setAvatar("");
        groupEntity.setGroupType(AppConstant.DBConstant.GROUP_TYPE_TEMP);
        groupEntity.setStatus(AppConstant.DBConstant.GROUP_STATUS_ONLINE);
        groupEntity.setUserCnt(groupCreateRsp.getUserIdListCount());
        groupEntity.setVersion(1);

        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());
        return groupEntity;
    }


    /**
     * 拆分消息在上层做掉 图文混排
     * 在这判断
    */
    public static MessageEntity getMessageEntity(IMBaseDefine.MsgInfo msgInfo) {
        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgInfo.getMsgType();
        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    /**语音的解析不能转自 string再返回来*/
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    return null;
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                break;

            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        return messageEntity;
    }

    public static MessageEntity analyzeText(IMBaseDefine.MsgInfo msgInfo){
       return MsgAnalyzeEngine.analyzeMessage(msgInfo);
    }


    public static AudioMessageEntity analyzeAudio(IMBaseDefine.MsgInfo msgInfo) throws JSONException, UnsupportedEncodingException {
        AudioMessageEntity audioMessage = new AudioMessageEntity();
        audioMessage.setFromId(msgInfo.getFromSessionId());
        audioMessage.setMsgId(msgInfo.getMsgId());
        audioMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        audioMessage.setStatus(AppConstant.MessageConstant.MSG_SUCCESS);
        audioMessage.setReadStatus(AppConstant.MessageConstant.AUDIO_UNREAD);
        audioMessage.setDisplayType(AppConstant.DBConstant.SHOW_TYPE_AUDIO);
        audioMessage.setCreated(msgInfo.getCreateTime());
        audioMessage.setUpdated(msgInfo.getCreateTime());

        ByteString bytes = msgInfo.getMsgData();

        byte[] audioStream = bytes.toByteArray();
        if(audioStream.length < 4){
            audioMessage.setReadStatus(AppConstant.MessageConstant.AUDIO_READ);
            audioMessage.setAudioPath("");
            audioMessage.setAudioLength(0);
        }else {
            int msgLen = audioStream.length;
            byte[] playTimeByte = new byte[4];
            byte[] audioContent = new byte[msgLen - 4];

            System.arraycopy(audioStream, 0, playTimeByte, 0, 4);
            System.arraycopy(audioStream, 4, audioContent, 0, msgLen - 4);
            int playTime = CommonUtil.byteArray2int(playTimeByte);
//            String audioSavePath = FileUtil.saveAudioResourceToFile(audioContent, audioMessage.getFromId());
//            audioMessage.setAudioLength(playTime);
//            audioMessage.setAudioPath(audioSavePath);
        }

        /**抽离出来 或者用gson*/
        JSONObject extraContent = new JSONObject();
        extraContent.put("audioPath",audioMessage.getAudioPath());
        extraContent.put("audiolength",audioMessage.getAudioLength());
        extraContent.put("readStatus",audioMessage.getReadStatus());
        String audioContent = extraContent.toString();
        audioMessage.setContent(audioContent);

        return audioMessage;
    }


    public static MessageEntity getMessageEntity(IMMessage.IMMsgData msgData){

        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgData.getMsgType();
        IMBaseDefine.MsgInfo msgInfo = IMBaseDefine.MsgInfo.newBuilder()
                .setMsgData(msgData.getMsgData())
                .setMsgId(msgData.getMsgId())
                .setMsgType(msgType)
                .setCreateTime(msgData.getCreateTime())
                .setFromSessionId(msgData.getFromUserId())
                .build();

        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        if(messageEntity != null){
            messageEntity.setToId(msgData.getToSessionId());
        }

        /**
         消息的发送状态与 展示类型需要在上层做掉
         messageEntity.setStatus();
         messageEntity.setDisplayType();
         */
        return messageEntity;
    }

    public static UnreadMessageEntity getUnreadEntity(IMBaseDefine.UnreadInfo pbInfo){
        UnreadMessageEntity unreadEntity = new UnreadMessageEntity();
        unreadEntity.setSessionType(getJavaSessionType(pbInfo.getSessionType()));
        unreadEntity.setLatestMsgData(pbInfo.getLatestMsgData().toString());
        unreadEntity.setPeerId(pbInfo.getSessionId());
        unreadEntity.setLatestMsgId(pbInfo.getLatestMsgId());
        unreadEntity.setUnReadCnt(pbInfo.getUnreadCnt());
        unreadEntity.buildSessionKey();
        return unreadEntity;
    }

    /**----enum 转化接口--*/
    public static int getJavaMsgType(IMBaseDefine.MsgType msgType){
        switch (msgType){
            case MSG_TYPE_GROUP_TEXT:
                return AppConstant.DBConstant.MSG_TYPE_GROUP_TEXT;
            case MSG_TYPE_GROUP_AUDIO:
                return AppConstant.DBConstant.MSG_TYPE_GROUP_AUDIO;
            case MSG_TYPE_SINGLE_AUDIO:
                return AppConstant.DBConstant.MSG_TYPE_SINGLE_AUDIO;
            case MSG_TYPE_SINGLE_TEXT:
                return AppConstant.DBConstant.MSG_TYPE_SINGLE_TEXT;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }

    public static int getJavaSessionType(IMBaseDefine.SessionType sessionType){
        switch (sessionType){
            case SESSION_TYPE_SINGLE:
                return AppConstant.DBConstant.SESSION_TYPE_SINGLE;
            case SESSION_TYPE_GROUP:
                return AppConstant.DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }

    public static int getJavaGroupType(IMBaseDefine.GroupType groupType){
        switch (groupType){
            case GROUP_TYPE_NORMAL:
                return AppConstant.DBConstant.GROUP_TYPE_NORMAL;
            case GROUP_TYPE_TMP:
                return AppConstant.DBConstant.GROUP_TYPE_TEMP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +groupType);
        }
    }

    public static int getGroupChangeType(IMBaseDefine.GroupModifyType modifyType){
        switch (modifyType){
            case GROUP_MODIFY_TYPE_ADD:
                return AppConstant.DBConstant.GROUP_MODIFY_TYPE_ADD;
            case GROUP_MODIFY_TYPE_DEL:
                return AppConstant.DBConstant.GROUP_MODIFY_TYPE_DEL;
            default:
                throw new IllegalArgumentException("GroupModifyType is illegal,cause by " +modifyType);
        }
    }

    public static int getDepartStatus(IMBaseDefine.DepartmentStatusType statusType){
        switch (statusType){
            case DEPT_STATUS_OK:
                return AppConstant.DBConstant.DEPT_STATUS_OK;
            case DEPT_STATUS_DELETE:
                return AppConstant.DBConstant.DEPT_STATUS_DELETED;
            default:
                throw new IllegalArgumentException("getDepartStatus is illegal,cause by " +statusType);
        }

    }
}
