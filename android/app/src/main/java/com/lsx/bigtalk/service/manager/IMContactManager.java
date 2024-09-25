package com.lsx.bigtalk.service.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.storage.db.BTDB;
import com.lsx.bigtalk.storage.db.entity.DepartmentEntity;
import com.lsx.bigtalk.storage.db.entity.UserEntity;
import com.lsx.bigtalk.service.event.ContactEvent;
import com.lsx.bigtalk.pb.helper.ProtoBuf2JavaBean;
import com.lsx.bigtalk.pb.IMBaseDefine;
import com.lsx.bigtalk.pb.IMBuddy;
import com.lsx.bigtalk.ui.helper.IMUIHelper;
import com.lsx.bigtalk.logs.Logger;
import com.lsx.bigtalk.utils.pinyin.PinYin;


public class IMContactManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMContactManager.class);
    private final IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private final BTDB btDb = BTDB.instance();
    private boolean isContactDataReady = false;
    private final Map<Integer, UserEntity> userMap = new ConcurrentHashMap<>();
    private final Map<Integer, DepartmentEntity> deptMap = new ConcurrentHashMap<>();
    @SuppressLint("StaticFieldLeak")
    private static IMContactManager instance;
    public static synchronized IMContactManager getInstance() {
        if (null == instance) {
            instance = new IMContactManager();
        }
        return instance;
    }

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        isContactDataReady = false;
        userMap.clear();
        deptMap.clear();
    }

    public void onNormalLoginOk() {
        logger.i("IMContactManager#onNormalLoginOk");
        onLocalLoginOk();
        onRemoteLoginOk();
    }

    public void onLocalLoginOk() {
        logger.d("IMContactManager#onLocalLoginOk");
        List<DepartmentEntity> deptList = btDb.loadAllDept();
        List<UserEntity> userList = btDb.loadAllUsers();

        for (UserEntity userInfo : userList) {
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userMap.put(userInfo.getPeerId(), userInfo);
        }

        for (DepartmentEntity deptInfo : deptList) {
            PinYin.getPinYin(deptInfo.getDepartName(), deptInfo.getPinyinElement());
            deptMap.put(deptInfo.getDepartId(), deptInfo);
        }
        triggerEvent(ContactEvent.CONTACT_INFO_OK);
    }

    public void onRemoteLoginOk() {
        logger.i("IMContactManager#onRemoteLoginOk");

        int latestDeptUpdateTime = btDb.getDeptLastTime();
        logger.i("IMContactManager#fetch dept list(latest update time: %d)", latestDeptUpdateTime);
        fetchAllDepts(latestDeptUpdateTime);

        int latestUserUpdateTime = btDb.getUserInfoLastTime();
        logger.i("IMContactManager#fetch user list(latest update time: %d)", latestUserUpdateTime);
        fetchAllUsers(latestUserUpdateTime);
    }

    public void triggerEvent(ContactEvent event) {
        if (event == ContactEvent.CONTACT_INFO_OK) {
            isContactDataReady = true;
        }
        EventBus.getDefault().postSticky(event);
    }

    private void fetchAllUsers(int latestUserUpdateTime) {
		int userId = IMLoginManager.getInstance().getLoginId();

        IMBuddy.IMAllUserReq imAllUserReq = IMBuddy.IMAllUserReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(latestUserUpdateTime)
                .build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_USER_REQUEST_VALUE;
        imSocketManager.sendRequest(imAllUserReq, sid, cid);
	}
    
	public void handleFetchAllUsersResp(IMBuddy.IMAllUserRsp imAllUserRsp) {
		logger.i("IMContactManager#handleFetchAllUsersResp");
        int userId = imAllUserRsp.getUserId();
        int count = imAllUserRsp.getUserListCount();
        if (count <= 0) {
            return;
        }

		int loginId = IMLoginManager.getInstance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by handleFetchAllUsersResp");
            return;
        }

        List<IMBaseDefine.UserInfo> changeList = imAllUserRsp.getUserListList();
        ArrayList<UserEntity> usersNeedToBeUpdate = new ArrayList<>();
        for (IMBaseDefine.UserInfo userInfo : changeList) {
            UserEntity entity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            userMap.put(entity.getPeerId(), entity);
            usersNeedToBeUpdate.add(entity);
        }

        btDb.batchInsertOrUpdateUser(usersNeedToBeUpdate);
        triggerEvent(ContactEvent.CONTACT_INFO_UPDATE);
	}

    public UserEntity findContact(int buddyId) {
        if (buddyId > 0 && userMap.containsKey(buddyId)) {
            return userMap.get(buddyId);
        }
        return null;
    }

    public void fetchUsersDetail(ArrayList<Integer> userIds) {
        logger.i("IMContactManager#fetchUsersDetail");
        if (null == userIds || userIds.isEmpty()) {
            logger.i("IMContactManager#fetchUsersDetail return, cause by null or empty");
            return;
        }
        int loginId = IMLoginManager.getInstance().getLoginId();
        IMBuddy.IMUsersInfoReq imUsersInfoReq = IMBuddy.IMUsersInfoReq.newBuilder()
                .setUserId(loginId)
                .addAllUserIdList(userIds)
                .build();

        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_REQUEST_VALUE;
        imSocketManager.sendRequest(imUsersInfoReq, sid, cid);
    }
    
    public void handleFetchUsersDetailResp(IMBuddy.IMUsersInfoRsp imUsersInfoRsp) {
        int loginId = imUsersInfoRsp.getUserId();
        boolean needEvent = false;
        List<IMBaseDefine.UserInfo> userInfoList = imUsersInfoRsp.getUserInfoListList();

        ArrayList<UserEntity> usersNeedToBeUpdate = new ArrayList<>();
        for (IMBaseDefine.UserInfo userInfo : userInfoList) {
            UserEntity userEntity = ProtoBuf2JavaBean.getUserEntity(userInfo);
            int userId = userEntity.getPeerId();
            if (userMap.containsKey(userId) && Objects.equals(userMap.get(userId), userEntity)) {
                logger.i("IMContactManager#fetchUsersDetail#user already exists");
            } else {
                needEvent = true;
                userMap.put(userEntity.getPeerId(), userEntity);
                usersNeedToBeUpdate.add(userEntity);
                if (userInfo.getUserId() == loginId) {
                    IMLoginManager.getInstance().setUserEntity(userEntity);
                }
            }
        }

        btDb.batchInsertOrUpdateUser(usersNeedToBeUpdate);
        
        if (needEvent) {
            triggerEvent(ContactEvent.CONTACT_INFO_UPDATE);
        }
    }

    public DepartmentEntity findDepartment(int deptId) {
        return deptMap.get(deptId);
    }

    public ArrayList<DepartmentEntity> getSortedDeptList() {
        ArrayList<DepartmentEntity> departmentList = new ArrayList<>(deptMap.values());
        departmentList.sort(new Comparator<DepartmentEntity>() {
            @Override
            public int compare(DepartmentEntity entity1, DepartmentEntity entity2) {
                if (entity1.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity1.getDepartName(), entity1.getPinyinElement());
                }
                if (entity2.getPinyinElement().pinyin == null) {
                    PinYin.getPinYin(entity2.getDepartName(), entity2.getPinyinElement());
                }
                return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
            }
        });
        return departmentList;
    }

    public List<UserEntity> getSortedContactList() {
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        contactList.sort(new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if (entity1.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                    }
                    if (entity2.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity2.getMainName(), entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return contactList;
    }


    public List<UserEntity> getDepartmentTabSortedList() {
        // todo eric efficiency
        List<UserEntity> contactList = new ArrayList<>(userMap.values());
        contactList.sort(new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                DepartmentEntity dept1 = deptMap.get(entity1.getDepartmentId());
                DepartmentEntity dept2 = deptMap.get(entity2.getDepartmentId());

                if (entity1.getDepartmentId() == entity2.getDepartmentId()) {
                    // start compare
                    if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                        return -1;
                    } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                        // todo eric guess: latter is > 0
                        return 1;
                    } else {
                        if (entity1.getPinyinElement().pinyin == null) {
                            PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                        }
                        if (entity2.getPinyinElement().pinyin == null) {
                            PinYin.getPinYin(entity2.getMainName(), entity2.getPinyinElement());
                        }
                        return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                    }
                    // end compare
                } else {
                    return dept1.getDepartName().compareToIgnoreCase(dept2.getDepartName());
                }
            }
        });
        return contactList;
    }

    public List<UserEntity> searchUserContact(String key){
       List<UserEntity> resultList = new ArrayList<>();
       for (Map.Entry<Integer, UserEntity> entry : userMap.entrySet()) {
           UserEntity user = entry.getValue();
           if (IMUIHelper.handleContactSearch(key, user)) {
               resultList.add(user);
           }
       }
       return resultList;
    }

    public List<DepartmentEntity> searchDeptContact(String key) {
        List<DepartmentEntity> searchList = new ArrayList<>();
        for (Map.Entry<Integer, DepartmentEntity> entry : deptMap.entrySet()) {
            DepartmentEntity dept = entry.getValue();
            if (IMUIHelper.handleDepartmentSearch(key, dept)) {
                searchList.add(dept);
            }
        }
        return searchList;
    }
    
    public void fetchAllDepts(int lastDeptUpdateTime) {
        int userId = IMLoginManager.getInstance().getLoginId();

        IMBuddy.IMDepartmentReq imDepartmentReq = IMBuddy.IMDepartmentReq.newBuilder()
                .setUserId(userId)
                .setLatestUpdateTime(lastDeptUpdateTime).build();
        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEPARTMENT_REQUEST_VALUE;
        imSocketManager.sendRequest(imDepartmentReq, sid, cid);
    }

    public void handleFetchAllDeptsResp(IMBuddy.IMDepartmentRsp imDepartmentRsp) {
        logger.i("IMContactManager#handleFetchAllDeptsResp");
        int userId = imDepartmentRsp.getUserId();
        int latestDeptsUpdateTime = imDepartmentRsp.getLatestUpdateTime();

        int count = imDepartmentRsp.getDeptListCount();
        if (count <= 0) {
            return;
        }

        int loginId = IMLoginManager.getInstance().getLoginId();
        if (userId != loginId) {
            logger.e("[fatal error] userId not equels loginId ,cause by handleFetchAllDeptsResp");
            return ;
        }
        List<IMBaseDefine.DepartInfo> changeList = imDepartmentRsp.getDeptListList();
        ArrayList<DepartmentEntity> needDb = new ArrayList<>();

        for (IMBaseDefine.DepartInfo departInfo : changeList) {
            DepartmentEntity entity = ProtoBuf2JavaBean.getDepartEntity(departInfo);
            deptMap.put(entity.getDepartId(), entity);
            needDb.add(entity);
        }
        btDb.batchInsertOrUpdateDepart(needDb);
        triggerEvent(ContactEvent.CONTACT_INFO_UPDATE);
    }

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }

    public Map<Integer, DepartmentEntity> getDeptMap() {
        return deptMap;
    }

    public boolean getIsContactDataReady() {
        return isContactDataReady;
    }

}
