package com.lsx.bigtalk.service.manager;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.CodedInputStream;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.storage.db.BTDB;
import com.lsx.bigtalk.storage.db.entity.GroupEntity;
import com.lsx.bigtalk.storage.db.entity.SessionEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.service.callback.PacketListener;
import com.lsx.bigtalk.service.event.GroupEvent;
import com.lsx.bigtalk.service.event.SessionEvent;
import com.lsx.bigtalk.pb.helper.EntityChangeEngine;
import com.lsx.bigtalk.pb.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.IMGroup;
import com.lsx.bigtalk.ui.helper.IMUIHelper;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.utils.pinyin.PinYin;


public class IMGroupManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMGroupManager.class);
    private final IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private final IMLoginManager imLoginManager = IMLoginManager.getInstance();
    private final BTDB btDb = BTDB.instance();
    private final Map<Integer, GroupEntity> groupMap = new ConcurrentHashMap<>();
    private boolean isGroupDataReady = false;
    @SuppressLint("StaticFieldLeak")
    private static IMGroupManager instance;
    public static synchronized IMGroupManager getInstance() {
        if (null == instance) {
            instance = new IMGroupManager();
        }
        return instance;
    }
    
    @Override
    public void doOnStart() {
        groupMap.clear();
    }

    @Override
    public void reset() {
        isGroupDataReady = false;
        groupMap.clear();
        EventBus.getDefault().unregister(instance);
    }

    public void onNormalLoginOk() {
        onLocalLoginOk();
        onRemoteLoginOk();
    }
    
    public void onLocalLoginOk() {
        logger.i("IMGroupManager#onLocalLoginOk");

        if (!EventBus.getDefault().isRegistered(instance)) {
            EventBus.getDefault().registerSticky(instance);
        }

        List<GroupEntity> localGroupInfoList = btDb.loadAllGroup();
        for (GroupEntity groupInfo : localGroupInfoList) {
            groupMap.put(groupInfo.getPeerId(), groupInfo);
        }

        triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_OK));
    }

    public void onRemoteLoginOk() {
        fetchNormalGroupInfoList();
    }

    public void onEvent(SessionEvent event) {
        if (event == SessionEvent.SESSION_UPDATE) {
            loadSessionGroupInfo();
        }
    }

    public  synchronized void triggerEvent(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_OK:
            case GROUP_INFO_UPDATED:
                isGroupDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    private void loadSessionGroupInfo(){
        logger.i("IMGroupManager#loadSessionGroupInfo");

        List<SessionEntity> sessionInfoList =  IMSessionManager.getInstance().getRecentSessionList();
        List<IMBaseDefine.GroupVersionInfo> needReqList = new ArrayList<>();
        for (SessionEntity sessionInfo : sessionInfoList) {
            int version = 0;
            if (sessionInfo.getPeerType() == AppConstant.DBConstant.SESSION_TYPE_GROUP) {
                if (groupMap.containsKey(sessionInfo.getPeerId())) {
                    version = groupMap.get(sessionInfo.getPeerId()).getVersion();
                }

                IMBaseDefine.GroupVersionInfo versionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                        .setVersion(version)
                        .setGroupId(sessionInfo.getPeerId())
                        .build();
                needReqList.add(versionInfo);
            }
        }

        if(!needReqList.isEmpty()) {
            fetchGroupDetailInfo(needReqList);
        }
    }

    private void fetchNormalGroupInfoList() {
        logger.i("IMGroupManager#fetchNormalGroupInfoList");
        int loginId = imLoginManager.getLoginId();
        IMGroup.IMNormalGroupListReq normalGroupListReq = IMGroup.IMNormalGroupListReq.newBuilder()
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_NORMAL_LIST_REQUEST_VALUE;
        imSocketManager.sendRequest(normalGroupListReq, sid, cid);
    }

    public void handleFetchNormalGroupInfoListResp(IMGroup.IMNormalGroupListRsp normalGroupListRsp) {
        logger.i("IMGroupManager#handleFetchNormalGroupInfoListResp");
        List<IMBaseDefine.GroupVersionInfo> versionInfoList = normalGroupListRsp.getGroupVersionListList();
        List<IMBaseDefine.GroupVersionInfo> needInfoList = new ArrayList<>();
        for (IMBaseDefine.GroupVersionInfo groupVersionInfo : versionInfoList) {
            int groupId = groupVersionInfo.getGroupId();
            int version = groupVersionInfo.getVersion();
            if (groupMap.containsKey(groupId)
                && null != groupMap.get(groupId)
                && Objects.requireNonNull(groupMap.get(groupId)).getVersion() == version) {
                continue;
            }
            IMBaseDefine.GroupVersionInfo versionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                    .setVersion(0)
                    .setGroupId(groupId)
                    .build();
            needInfoList.add(versionInfo);
        }

        if (!needInfoList.isEmpty()) {
            fetchGroupDetailInfo(needInfoList);
        }
    }

    public void fetchGroupDetailInfo(int groupId) {
        IMBaseDefine.GroupVersionInfo groupVersionInfo = IMBaseDefine.GroupVersionInfo.newBuilder()
                .setGroupId(groupId)
                .setVersion(0)
                .build();
        ArrayList<IMBaseDefine.GroupVersionInfo> list = new ArrayList<>();
        list.add(groupVersionInfo);
        fetchGroupDetailInfo(list);
    }
    
    public void fetchGroupDetailInfo(List<IMBaseDefine.GroupVersionInfo> versionInfoList) {
        logger.i("IMGroupManager#fetchGroupDetailInfo");
        if (versionInfoList == null || versionInfoList.isEmpty()) {
            return;
        }
        int loginId = imLoginManager.getLoginId();
        IMGroup.IMGroupInfoListReq groupInfoListReq = IMGroup.IMGroupInfoListReq.newBuilder()
                .setUserId(loginId)
                .addAllGroupVersionList(versionInfoList)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(groupInfoListReq, sid, cid);
    }
    
    public void handleFetchGroupDetailInfoResp(IMGroup.IMGroupInfoListRsp groupInfoListRsp){
        logger.i("IMGroupManager#handleFetchGroupDetailInfoResp");
        int groupSize = groupInfoListRsp.getGroupInfoListCount();
        int userId = groupInfoListRsp.getUserId();
        int loginId = imLoginManager.getLoginId();
        if (groupSize <= 0 || userId != loginId) {
            return;
        }
        ArrayList<GroupEntity> needDb = new ArrayList<>();
        for (IMBaseDefine.GroupInfo groupInfo : groupInfoListRsp.getGroupInfoListList()) {
            GroupEntity groupEntity = ProtoBuf2JavaBean.getGroupEntity(groupInfo);
            groupMap.put(groupEntity.getPeerId(), groupEntity);
            needDb.add(groupEntity);
        }

        btDb.batchInsertOrUpdateGroup(needDb);
        triggerEvent(new GroupEvent(GroupEvent.Event.GROUP_INFO_UPDATED));
    }

    public void createTempGroup(String groupName, Set<Integer> memberList) {
        logger.i("IMGroupManager#reqCreateTempGroup");

        int loginId = imLoginManager.getLoginId();
        IMGroup.IMGroupCreateReq groupCreateReq = IMGroup.IMGroupCreateReq.newBuilder()
                .setUserId(loginId)
                .setGroupType(IMBaseDefine.GroupType.GROUP_TYPE_TMP)
                .setGroupName(groupName)
                .setGroupAvatar("")
                .addAllMemberIdList(memberList)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CREATE_REQUEST_VALUE;
        imSocketManager.sendRequest(groupCreateReq, sid, cid, new PacketListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupCreateRsp groupCreateRsp = IMGroup.IMGroupCreateRsp.parseFrom((CodedInputStream)response);
                    IMGroupManager.getInstance().handleCreateTempGroupResp(groupCreateRsp);
                } catch (IOException e) {
                    logger.e("reqCreateTempGroup parse error");
                    triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
                }
            }

            @Override
            public void onFailed() {
              triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
            }

            @Override
            public void onTimeout() {
              triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_TIMEOUT));
            }
        });

    }

    public void handleCreateTempGroupResp(IMGroup.IMGroupCreateRsp groupCreateRsp) {
        logger.d("IMGroupManager#onReqCreateTempGroup");

        int resultCode = groupCreateRsp.getResultCode();
        if(0 != resultCode){
            logger.e("group#createGroup failed");
            triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_FAIL));
            return;
        }
        GroupEntity groupEntity = ProtoBuf2JavaBean.getGroupEntity(groupCreateRsp);
        // 更新DB 更新map
        groupMap.put(groupEntity.getPeerId(),groupEntity);

        IMSessionManager.getInstance().updateSession(groupEntity);
        btDb.insertOrUpdateGroup(groupEntity);
        triggerEvent(new GroupEvent(GroupEvent.Event.CREATE_GROUP_OK, groupEntity)); // 接收到之后修改UI
    }

    /**
     * 删除群成员
     * REMOVE_CHANGE_MEMBER_TYPE
     * 可能会触发头像的修改
     */
    public void reqRemoveGroupMember(int groupId,Set<Integer> removeMemberlist){
        reqChangeGroupMember(groupId,IMBaseDefine.GroupModifyType.GROUP_MODIFY_TYPE_DEL, removeMemberlist);
    }
    /**
     * 新增群成员
     * ADD_CHANGE_MEMBER_TYPE
     * 可能会触发头像的修改
     */
    public void reqAddGroupMember(int groupId,Set<Integer> addMemberlist){
        reqChangeGroupMember(groupId,IMBaseDefine.GroupModifyType.GROUP_MODIFY_TYPE_ADD, addMemberlist);
    }

    private void reqChangeGroupMember(int groupId,IMBaseDefine.GroupModifyType groupModifyType, Set<Integer> changeMemberlist) {
        logger.i("IMGroupManager#reqChangeGroupMember, changeGroupMemberType = %s", groupModifyType.toString());

        final int loginId = imLoginManager.getLoginId();
        IMGroup.IMGroupChangeMemberReq groupChangeMemberReq = IMGroup.IMGroupChangeMemberReq.newBuilder()
                .setUserId(loginId)
                .setChangeType(groupModifyType)
                .addAllMemberIdList(changeMemberlist)
                .setGroupId(groupId)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_REQUEST_VALUE;
        imSocketManager.sendRequest(groupChangeMemberReq, sid, cid,new PacketListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp = IMGroup.IMGroupChangeMemberRsp.parseFrom((CodedInputStream)response);
                    IMGroupManager.getInstance().onReqChangeGroupMember(groupChangeMemberRsp);
                } catch (IOException e) {
                    logger.e("reqChangeGroupMember parse error!");
                    triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
                }
            }

            @Override
            public void onFailed() {
                triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
            }

            @Override
            public void onTimeout() {
                triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_TIMEOUT));
            }
        });

    }

    public void onReqChangeGroupMember(IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp){
        int resultCode = groupChangeMemberRsp.getResultCode();
        if (0 != resultCode){
            triggerEvent(new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_FAIL));
            return;
        }

        int groupId = groupChangeMemberRsp.getGroupId();
        List<Integer> changeUserIdList = groupChangeMemberRsp.getChgUserIdListList();
        IMBaseDefine.GroupModifyType groupModifyType = groupChangeMemberRsp.getChangeType();


        GroupEntity groupEntityRet = groupMap.get(groupId);
        groupEntityRet.setlistGroupMemberIds(groupChangeMemberRsp.getCurUserIdListList());
        groupMap.put(groupId,groupEntityRet);
        btDb.insertOrUpdateGroup(groupEntityRet);


        GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
        groupEvent.setChangeList(changeUserIdList);
        groupEvent.setChangeType(ProtoBuf2JavaBean.getGroupChangeType(groupModifyType));
        groupEvent.setGroupEntity(groupEntityRet);
        triggerEvent(groupEvent);
    }

    public void shieldGroup(final int groupId, final int shieldType) {
        final GroupEntity entity = groupMap.get(groupId);
        if (entity == null) {
            return;
        }
        final int loginId = IMLoginManager.getInstance().getLoginId();
        IMGroup.IMGroupShieldReq shieldReq = IMGroup.IMGroupShieldReq.newBuilder()
                .setShieldStatus(shieldType)
                .setGroupId(groupId)
                .setUserId(loginId)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_GROUP_VALUE;
        int cid = IMBaseDefine.GroupCmdID.CID_GROUP_SHIELD_GROUP_REQUEST_VALUE;
        imSocketManager.sendRequest(shieldReq, sid, cid, new PacketListener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMGroup.IMGroupShieldRsp groupShieldRsp = IMGroup.IMGroupShieldRsp.parseFrom((CodedInputStream)response);
                    int resCode = groupShieldRsp.getResultCode();
                    if (resCode != 0) {
                        triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_FAIL));
                        return;
                    }
                    if (groupShieldRsp.getGroupId() != groupId || groupShieldRsp.getUserId() != loginId) {
                        return;
                    }
                    entity.setStatus(shieldType);
                    btDb.insertOrUpdateGroup(entity);
                    boolean isFor = shieldType == AppConstant.DBConstant.GROUP_STATUS_SHIELD;
                    IMUnreadMsgManager.getInstance().setIsShield(
                            EntityChangeEngine.getSessionKey(groupId,AppConstant.DBConstant.SESSION_TYPE_GROUP), isFor);
                    triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_OK, entity));
                } catch (IOException e) {
                    logger.e("reqChangeGroupMember parse error!");
                    triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_FAIL));
                }
            }
            @Override
            public void onFailed() {
                triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_FAIL));
            }

            @Override
            public void onTimeout() {
                triggerEvent(new GroupEvent(GroupEvent.Event.SHIELD_GROUP_TIMEOUT));
            }
        });
    }
    
    public void handleGroupMemberChangeNotify(IMGroup.IMGroupChangeMemberNotify notify) {
       int groupId = notify.getGroupId();
       int changeType = ProtoBuf2JavaBean.getGroupChangeType(notify.getChangeType());
       List<Integer> changeList = notify.getChgUserIdListList();
       List<Integer> curMemberList = notify.getCurUserIdListList();
       if (groupMap.containsKey(groupId) && null != groupMap.get(groupId)) {
           GroupEntity entity = groupMap.get(groupId);
           Objects.requireNonNull(entity).setlistGroupMemberIds(curMemberList);
           btDb.insertOrUpdateGroup(entity);
           groupMap.put(groupId, entity);

           GroupEvent groupEvent = new GroupEvent(GroupEvent.Event.CHANGE_GROUP_MEMBER_SUCCESS);
           groupEvent.setChangeList(changeList);
           groupEvent.setChangeType(changeType);
           groupEvent.setGroupEntity(entity);
           triggerEvent(groupEvent);
       }
    }

	public List<GroupEntity> getNormalGroupList() {
		List<GroupEntity> normalGroupList = new ArrayList<>();
		for (Entry<Integer, GroupEntity> entry : groupMap.entrySet()) {
			GroupEntity group = entry.getValue();
			if (group == null) {
				continue;
			}
			if (group.getGroupType() == AppConstant.DBConstant.GROUP_TYPE_NORMAL) {
				normalGroupList.add(group);
			}
		}
		return normalGroupList;
	}

    // 该方法只有正式群
    // todo eric efficiency
    public  List<GroupEntity> getNormalGroupSortedList() {
        List<GroupEntity> groupList = getNormalGroupList();
        Collections.sort(groupList, new Comparator<GroupEntity>(){
            @Override
            public int compare(GroupEntity entity1, GroupEntity entity2) {
                if(entity1.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                }
                if(entity2.getPinyinElement().pinyin==null)
                {
                    PinYin.getPinYin(entity2.getMainName(),entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
            }
        });

        return groupList;
    }

	public GroupEntity findGroup(int groupId) {
		logger.d("IMGroupManager#findGroup");
        if (groupMap.containsKey(groupId)) {
            return groupMap.get(groupId);
        }
        return null;
	}

    public List<GroupEntity> searchGroup(String key) {
        List<GroupEntity> searchList = new ArrayList<>();
        for (Map.Entry<Integer, GroupEntity> entry : groupMap.entrySet()) {
            GroupEntity groupEntity = entry.getValue();
            if (IMUIHelper.handleGroupSearch(key, groupEntity)) {
                searchList.add(groupEntity);
            }
        }
        return searchList;
    }

	public List<UserEntity> getGroupMembers(int groupId) {
		logger.d("IMGroupManager#getGroupMembers");

		GroupEntity group = findGroup(groupId);
		if (group == null) {
			return null;
		}
        Set<Integer> userList = group.getlistGroupMemberIds();
		ArrayList<UserEntity> memberList = new ArrayList<UserEntity>();
		for (Integer id : userList) {
			UserEntity contact = IMContactManager.getInstance().findContact(id);
			if (contact == null) {
				continue;
			}
			memberList.add(contact);
		}
		return memberList;
	}

    public Map<Integer, GroupEntity> getGroupMap() {
        return groupMap;
    }

    public boolean isGroupDataReady() {
        return isGroupDataReady;
    }
}
