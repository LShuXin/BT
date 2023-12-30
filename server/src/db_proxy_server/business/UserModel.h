/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：UserModel.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年01月05日
*   描    述：
*
#pragma once
================================================================*/
#ifndef __USERMODEL_H__
#define __USERMODEL_H__

#include "IM.BaseDefine.pb.h"
#include "ImPduBase.h"
#include "public_define.h"
class CUserModel
{
public:
    static CUserModel* getInstance();
    ~CUserModel();

    // 获取指定时间以后有信息更新的用户id数组、最近一次用户信息更新发生的时间
    void getChangedId(uint32_t& nLastTime, list<uint32_t>& lsIds);
    // 用用户id数组换取用户信息数组
    void getUsers(list<uint32_t> lsIds, list<IM::BaseDefine::UserInfo>& lsUsers);
    // 用用户id换取用户信息
    bool getUser(uint32_t nUserId, DBUserInfo_t& cUser);
    // 更新用户信息
    bool updateUser(DBUserInfo_t& cUser);
    // 新增用户
    bool insertUser(DBUserInfo_t& cUser);
    // void getUserByNick(const list<string>& lsNicks, list<IM::BaseDefine::UserInfo>& lsUsers);
    // 清除 nPeerId 发送的、nUserId 看到的未读消息数
    void clearUserCounter(uint32_t nUserId, uint32_t nPeerId, IM::BaseDefine::SessionType nSessionType);
private:
    CUserModel();
private:
    static CUserModel* m_pInstance;
};

#endif /*defined(__USERMODEL_H__) */
