/*================================================================
*     Copyright (c) 2014年 lanhu. All rights reserved.
*   
*   文件名称：CacheManager.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2014年12月02日
*   描    述：
*
#pragma once
================================================================*/
#ifndef __CACHEMANAGER_H__
#define __CACHEMANAGER_H__

#include <list>
#include <map>

#include "ostype.h"
#include "Lock.h"
#include "Condition.h"
#include "ImPduBase.h"
#include "public_define.h"
#include "IM.BaseDefine.pb.h"

class CSyncCenter
{
public:
    static CSyncCenter* getInstance();

    // 获取上一次更新群组的时间
    uint32_t getLastUpdateGroup() { return m_nLastUpdateGroup; }

    // 通过部门 id 获取部门名称
    string getDeptName(uint32_t nDeptId);

    // 开始同步群组
    void startSync();

    // 停止同步群组
    void stopSync();

    // 初始化函数，从缓存中获取上次同步群组的时间
    void init();
private:
    CSyncCenter();
    ~CSyncCenter();

    /**
     *  更新上次同步群组信息时间
     *  @param nUpdated 时间
     */
    void updateLastUpdateGroup(uint32_t nUpdated);

    // 同步群组聊天信息，主要工作内容是：
    // 1. 找出在上次群组同步之后，又有新聊天消息的群组，然后更新这些群组中用户的会话
    // 2. 修改 redis 中最近一次同步群组会话的时间
    static void* doSyncGroupChat(void* arg);
    
private:
    static CSyncCenter* m_pInstance;
    
    // 上次更新群组的时间
    uint32_t            m_nLastUpdateGroup;

    // 群组条件变量
    CCondition*         m_pCondGroupChat;

    // 群组锁
    CLock*              m_pLockGroupChat;

    // 是否正在进行群组同步
    static bool         m_bSyncGroupChatRuning;

    // 是否正在等待群组同步
    bool                m_bSyncGroupChatWaitting;
#ifdef _WIN32
    DWORD		        m_nGroupChatThreadId;
#else
    pthread_t	        m_nGroupChatThreadId;
#endif

};


#endif /*defined(__CACHEMANAGER_H__) */
