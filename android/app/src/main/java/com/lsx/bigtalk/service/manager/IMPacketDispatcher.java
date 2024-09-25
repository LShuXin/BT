package com.lsx.bigtalk.service.manager;

import java.io.IOException;
import com.google.protobuf.CodedInputStream;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.IMBuddy;
import com.lsx.bigtalk.pb.IMGroup;
import com.lsx.bigtalk.pb.IMLogin;
import com.lsx.bigtalk.pb.IMMessage;
import com.lsx.bigtalk.logs.Logger;


public class IMPacketDispatcher {
    private static final Logger logger = Logger.getLogger(IMPacketDispatcher.class);

    public static void dispatchLoginPacket(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
//                case IMBaseDefine.LoginCmdID.CID_LOGIN_RES_USERLOGIN_VALUE:
//                {
//                    IMLogin.IMLoginRes  imLoginRes = IMLogin.IMLoginRes.parseFrom(buffer);
//                    IMLoginManager.getInstance().onRepMsgServerLogin(imLoginRes);
//                    break;
//                }
                case IMBaseDefine.LoginCmdID.CID_LOGIN_RES_LOGINOUT_VALUE:
                {
                    IMLogin.IMLogoutRsp imLogoutRsp = IMLogin.IMLogoutRsp.parseFrom(buffer);
                    IMLoginManager.getInstance().handleLogOutResp(imLogoutRsp);
                    break;
                }
                case IMBaseDefine.LoginCmdID.CID_LOGIN_KICK_USER_VALUE:
                {
                    IMLogin.IMKickUser imKickUser = IMLogin.IMKickUser.parseFrom(buffer);
                    IMLoginManager.getInstance().onKickout(imKickUser);
                    break;
                }
            }
        } catch (IOException e) {
            logger.e("dispatchLoginPacket#error, cid:%d", commandId);
        }
    }

    public static void dispatchBuddyPacket(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_USER_RESPONSE_VALUE:
                {
                    IMBuddy.IMAllUserRsp imAllUserRsp = IMBuddy.IMAllUserRsp.parseFrom(buffer);
                    IMContactManager.getInstance().handleFetchAllUsersResp(imAllUserRsp);
                    break;
                }
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_RESPONSE_VALUE:
                {
                    IMBuddy.IMUsersInfoRsp imUsersInfoRsp = IMBuddy.IMUsersInfoRsp.parseFrom(buffer);
                    IMContactManager.getInstance().handleFetchUsersDetailResp(imUsersInfoRsp);
                    break;
                }
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_RECENT_CONTACT_SESSION_RESPONSE_VALUE:
                {
                    IMBuddy.IMRecentContactSessionRsp recentContactSessionRsp = IMBuddy.IMRecentContactSessionRsp.parseFrom(buffer);
                    IMSessionManager.getInstance().handleRecentContactsFetchRes(recentContactSessionRsp);
                    break;
                }
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_RES_VALUE:
                {
                    IMBuddy.IMRemoveSessionRsp removeSessionRsp = IMBuddy.IMRemoveSessionRsp.parseFrom(buffer);
                    IMSessionManager.getInstance().onRepRemoveSession(removeSessionRsp);
                    break;
                }
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_PC_LOGIN_STATUS_NOTIFY_VALUE:
                {
                    IMBuddy.IMPCLoginEventNotify statusNotify = IMBuddy.IMPCLoginEventNotify.parseFrom(buffer);
                    IMLoginManager.getInstance().onLoginEventNotify(statusNotify);
                    break;
                }
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEPARTMENT_RESPONSE_VALUE:
                {
                    IMBuddy.IMDepartmentRsp departmentRsp = IMBuddy.IMDepartmentRsp.parseFrom(buffer);
                    IMContactManager.getInstance().handleFetchAllDeptsResp(departmentRsp);
                    break;
                }
            }
        } catch (IOException e) {
            logger.e("buddyPacketDispatcher# error,cid:%d", commandId);
        }
    }

    public static void dispatchMsgPacket(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.MessageCmdID.CID_MSG_DATA_ACK_VALUE:
                {
                    // have some problem  todo
                    return;
                }
                case IMBaseDefine.MessageCmdID.CID_MSG_LIST_RESPONSE_VALUE:
                {
                    IMMessage.IMGetMsgListRsp rsp = IMMessage.IMGetMsgListRsp.parseFrom(buffer);
                    IMMessageManager.getInstance().handleFetchHistoryMsgResp(rsp);
                    return;
                }
                case IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE:
                {
                    IMMessage.IMMsgData imMsgData = IMMessage.IMMsgData.parseFrom(buffer);
                    IMMessageManager.getInstance().onReceiveMessage(imMsgData);
                    return;
                }
                case IMBaseDefine.MessageCmdID.CID_MSG_READ_NOTIFY_VALUE:
                {
                    IMMessage.IMMsgDataReadNotify readNotify = IMMessage.IMMsgDataReadNotify.parseFrom(buffer);
                    IMUnreadMsgManager.getInstance().onNotifyRead(readNotify);
                    return;
                }
                case IMBaseDefine.MessageCmdID.CID_MSG_UNREAD_CNT_RESPONSE_VALUE:
                {
                    IMMessage.IMUnreadMsgCntRsp unreadMsgCntRsp = IMMessage.IMUnreadMsgCntRsp.parseFrom(buffer);
                    IMUnreadMsgManager.getInstance().handleFetchUnreadMsgListResp(unreadMsgCntRsp);
                    return;
                }
                case IMBaseDefine.MessageCmdID.CID_MSG_GET_BY_MSG_ID_RES_VALUE:
                {
                    IMMessage.IMGetMsgByIdRsp getMsgByIdRsp = IMMessage.IMGetMsgByIdRsp.parseFrom(buffer);
                    IMMessageManager.getInstance().handleFetchMsgByIdResp(getMsgByIdRsp);
                    break;
                }
            }
        } catch (IOException e) {
            logger.e("msgPacketDispatcher# error,cid:%d", commandId);
        }
    }

    public static void dispatchGroupPacket(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.GroupCmdID.CID_GROUP_CREATE_RESPONSE_VALUE:
//                {
//                    IMGroup.IMGroupCreateRsp groupCreateRsp = IMGroup.IMGroupCreateRsp.parseFrom(buffer);
//                    IMGroupManager.getInstance().handleCreateTempGroupResp(groupCreateRsp);
//                    break;
//                }
                case IMBaseDefine.GroupCmdID.CID_GROUP_NORMAL_LIST_RESPONSE_VALUE:
                {
                    IMGroup.IMNormalGroupListRsp normalGroupListRsp = IMGroup.IMNormalGroupListRsp.parseFrom(buffer);
                    IMGroupManager.getInstance().handleFetchNormalGroupInfoListResp(normalGroupListRsp);
                    break;
                }
                case IMBaseDefine.GroupCmdID.CID_GROUP_INFO_RESPONSE_VALUE:
                {
                    IMGroup.IMGroupInfoListRsp groupInfoListRsp = IMGroup.IMGroupInfoListRsp.parseFrom(buffer);
                    IMGroupManager.getInstance().handleFetchGroupDetailInfoResp(groupInfoListRsp);
                    break;
                }
                case IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_RESPONSE_VALUE:
                {
                    IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp = IMGroup.IMGroupChangeMemberRsp.parseFrom(buffer);
                    IMGroupManager.getInstance().onReqChangeGroupMember(groupChangeMemberRsp);
                    return;
                }
                case IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_NOTIFY_VALUE:
                {
                    IMGroup.IMGroupChangeMemberNotify notify = IMGroup.IMGroupChangeMemberNotify.parseFrom(buffer);
                    IMGroupManager.getInstance().handleGroupMemberChangeNotify(notify);
                    break;
                }
                case IMBaseDefine.GroupCmdID.CID_GROUP_SHIELD_GROUP_RESPONSE_VALUE:
                {

                }
            }
        } catch (IOException e) {
            logger.e("groupPacketDispatcher# error,cid:%d", commandId);
        }
    }
}
