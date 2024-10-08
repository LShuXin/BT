package com.lsx.bigtalk.storage.db.entity;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import android.text.TextUtils;


import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.service.support.SearchElement;
import com.lsx.bigtalk.utils.pinyin.PinYin;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
// KEEP INCLUDES END
/**
 * Entity mapped to table GroupInfo.
 */
public class GroupEntity extends PeerEntity {

    private int groupType;
    private int creatorId;
    private int userCnt;
    /** Not-null value. */
    private String userList;
    private int version;
    private int status;

    // KEEP FIELDS - put your custom fields here

    private PinYin.PinYinElement pinyinElement = new PinYin.PinYinElement();
    private SearchElement searchElement = new SearchElement();
    // KEEP FIELDS END

    public GroupEntity() {
    }

    public GroupEntity(Long id) {
        this.id = id;
    }

    public GroupEntity(Long id, int peerId, int groupType, String mainName, String avatar, int creatorId, int userCnt, String userList, int version, int status, int created, int updated) {
        this.id = id;
        this.peerId = peerId;
        this.groupType = groupType;
        this.mainName = mainName;
        this.avatar = avatar;
        this.creatorId = creatorId;
        this.userCnt = userCnt;
        this.userList = userList;
        this.version = version;
        this.status = status;
        this.created = created;
        this.updated = updated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    /** Not-null value. */
    public String getMainName() {
        return mainName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    /** Not-null value. */
    public String getAvatar() {
        return avatar;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getUserCnt() {
        return userCnt;
    }

    public void setUserCnt(int userCnt) {
        this.userCnt = userCnt;
    }

    /** Not-null value. */
    public String getUserList() {
        return userList;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUserList(String userList) {
        this.userList = userList;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    // KEEP METHODS - put your custom methods here


    @Override
    public int getType() {
        return AppConstant.DBConstant.SESSION_TYPE_GROUP;
    }

    /**
     * yingmu
     * 获取群组成员的list
     * -- userList 前后去空格，按照逗号区分， 不检测空的成员(非法)
     */
    public Set<Integer> getlistGroupMemberIds(){
        if(TextUtils.isEmpty(userList)){
          return  Collections.emptySet();
        }
        String[] arrayUserIds =  userList.trim().split(",");
        if(arrayUserIds.length <=0){
            return Collections.emptySet();
        }
        /**zhe'g*/
        Set<Integer> result = new TreeSet<Integer>();
        for(int index=0;index < arrayUserIds.length;index++){
            int userId =  Integer.parseInt(arrayUserIds[index]);
            result.add(userId);
        }
        return result;
    }
    //todo 入参变为 set【自动去重】
    // 每次都要转换 性能不是太好，todo
    public void setlistGroupMemberIds(List<Integer> memberList){
        String userList = TextUtils.join(",",memberList);
        setUserList(userList);
    }

    public PinYin.PinYinElement getPinyinElement() {
        return pinyinElement;
    }

    public void setPinyinElement(PinYin.PinYinElement pinyinElement) {
        this.pinyinElement = pinyinElement;
    }

    public SearchElement getSearchElement() {
        return searchElement;
    }

    public void setSearchElement(SearchElement searchElement) {
        this.searchElement = searchElement;
    }
    // KEEP METHODS END
}
