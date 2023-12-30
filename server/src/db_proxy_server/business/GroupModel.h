/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef __IM_GROUP_MODEL__
#define __IM_GROUP_MODEL__

#include <stdio.h>
#include <iostream>
#include <string>
#include <list>
#include <map>
#include <set>

#include "MessageModel.h"
#include "IM.BaseDefine.pb.h"

using namespace std;

const uint32_t MAX_UNREAD_COUNT = 100;


class CGroupModel
{
public:
    virtual ~CGroupModel();
    static CGroupModel* getInstance();
    
public:
    /**
     *  创建群
     *
     *  @param nUserId        创建者
     *  @param strGroupName   群名
     *  @param strGroupAvatar 群头像
     *  @param nGroupType     群类型1,固定群;2,临时群
     *  @param setMember      群成员列表，为了踢出重复的userId，使用set存储
     *
     *  @return 成功返回群Id，失败返回0;
     */
    uint32_t createGroup(uint32_t nUserId, const string& strGroupName, const string& strGroupAvatar, uint32_t nGroupType, set<uint32_t>& setMember);
    bool removeGroup(uint32_t nUserId, uint32_t nGroupId, list<uint32_t>& lsCurUserId);
    void getUserGroup(uint32_t nUserId, list<IM::BaseDefine::GroupVersionInfo>& lsGroup, uint32_t nGroupType);
    // 获取用户加入的群的 id 列表
    void getUserGroupIds(uint32_t nUserId, list<uint32_t>& lsGroupId, uint32_t nLimited = 100);
    void getGroupInfo(map<uint32_t, IM::BaseDefine::GroupVersionInfo>& mapGroupId, list<IM::BaseDefine::GroupInfo>& lsGroupInfo);
    bool setPush(uint32_t nUserId, uint32_t nGroupId, uint32_t nType, uint32_t nStatus);
    void getPush(uint32_t nGroupId, list<uint32_t>& lsUser, list<IM::BaseDefine::ShieldStatus>& lsPush);
    bool modifyGroupMember(uint32_t nUserId, uint32_t nGroupId, IM::BaseDefine::GroupModifyType nType, set<uint32_t>& setUserId, list<uint32_t>& lsCurUserId);
    void getGroupUser(uint32_t nGroupId, list<uint32_t>& lsUserId);
    bool isInGroup(uint32_t nUserId, uint32_t nGroupId);
    void updateGroupChat(uint32_t nGroupId);
    bool isValidateGroupId(uint32_t nGroupId);
    uint32_t getUserJoinTime(uint32_t nGroupId, uint32_t nUserId);

private:
    CGroupModel();
    
    bool insertNewGroup(uint32_t reqUserId, const string& groupName, const string& groupAvatar, uint32_t groupType, uint32_t memCnt, uint32_t& groupId);
    bool insertNewMember(uint32_t nGroupId,set<uint32_t>& setUsers);
    string GenerateGroupAvatar(uint32_t groupId);
    void getGroupVersion(list<uint32_t>&lsGroupId, list<IM::BaseDefine::GroupVersionInfo>& lsGroup, uint32_t nGroupType);
    bool hasModifyPermission(uint32_t nUserId, uint32_t nGroupId, IM::BaseDefine::GroupModifyType nType);
    bool addMember(uint32_t nGroupId, set<uint32_t>& setUser,list<uint32_t>& lsCurUserId);
    bool removeMember(uint32_t nGroupId, set<uint32_t>& setUser,list<uint32_t>& lsCurUserId);
    void removeRepeatUser(uint32_t nGroupId, set<uint32_t>& setUser);
    void removeSession(uint32_t nGroupId, const set<uint32_t>& lsUser);
    bool incGroupVersion(uint32_t nGroupId);
    void clearGroupMember(uint32_t nGroupId);
    void fillGroupMember(list<IM::BaseDefine::GroupInfo>& lsGroups);
        
private:
    static CGroupModel*	m_pInstance;
};

#endif /* defined(__IM_GROUP_MODEL__) */
