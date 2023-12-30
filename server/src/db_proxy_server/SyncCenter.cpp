/*================================================================
*     Copyright (c) 2014年 lanhu. All rights reserved.
*   
*   文件名称：CacheManager.cpp
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2014年12月02日
*   描    述：
*
================================================================*/
#include <stdlib.h>
#include <sys/signal.h>
#include "SyncCenter.h"
#include "Lock.h"
#include "HttpClient.h"
#include "json/json.h"
#include "DBPool.h"
#include "CachePool.h"
#include "business/Common.h"
#include "business/UserModel.h"
#include "business/GroupModel.h"
#include "business/SessionModel.h"

// 初始单例的锁，避免多个线程同时初始化单例
static CLock* g_pLock = new CLock();
// static CRWLock *g_pRWDeptLock = new CRWLock();

CSyncCenter* CSyncCenter::m_pInstance = NULL;
bool CSyncCenter::m_bSyncGroupChatRuning = false;

CSyncCenter* CSyncCenter::getInstance()
{
    // 加锁，避免多个线程同时初始化单例
    CAutoLock autoLock(g_pLock);
    if (m_pInstance == NULL)
    {
        m_pInstance = new CSyncCenter();
    }
    return m_pInstance;
}

CSyncCenter::CSyncCenter()
    :m_nGroupChatThreadId(0),
    m_nLastUpdateGroup(time(NULL)),
    m_bSyncGroupChatWaitting(true),
    m_pLockGroupChat(new CLock())
    //m_pLock(new CLock())
{
    m_pCondGroupChat = new CCondition(m_pLockGroupChat);
}

CSyncCenter::~CSyncCenter()
{
    // 删除锁
    if (m_pLockGroupChat != NULL)
    {
        delete m_pLockGroupChat;
    }

    // 删除条件变量
    if (m_pCondGroupChat != NULL)
    {
        delete m_pCondGroupChat;
    }
}

/**
 *  开启内网数据同步以及群组聊天记录同步
 */
void CSyncCenter::startSync()
{
#ifdef _WIN32
    (void)CreateThread(NULL, 0, doSyncGroupChat, NULL, 0, &m_nGroupChatThreadId);
#else
    (void)pthread_create(&m_nGroupChatThreadId, NULL, doSyncGroupChat, NULL);
#endif
}

/**
 *  停止同步，为了"优雅"的同步，使用了条件变量
 */
void CSyncCenter::stopSync()
{
    m_bSyncGroupChatWaitting = false;

    // 唤醒一个被条件变量阻塞的线程
    // 实际上也只开辟了一个工作线程用于同步群组
    m_pCondGroupChat->notify();

    // 已经发出了停止同步的信号
    // 如果同步还没有停止，则应该让主线程同步
    while (m_bSyncGroupChatRuning)
    {
        usleep(500);
    }
}

void CSyncCenter::init()
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        string strLastUpdateGroup = pCacheConn->get("last_update_group");
        pCacheManager->RelCacheConn(pCacheConn);
        // BUGFIX: strLastUpdateGroup.empty() -> !strLastUpdateGroup.empty()
        if (!strLastUpdateGroup.empty())
        {
            m_nLastUpdateGroup = string2int(strLastUpdateGroup);
        }
        else
        {
            // 如果缓存中没有上次同步的时间，则直接将上次同步的时间设置为当前时间
            // 因为调用完 init 之后会立即开始同步
            updateLastUpdateGroup(time(NULL));
        }
    }
    else
    {
        log("no cache connection to get total_user_updated");
    }
}

void CSyncCenter::updateLastUpdateGroup(uint32_t nUpdated)
{
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        // 上次更新群组消息的时间
        // 此对象保存一份、redis 中也保存了一份，这是为什么？
        m_nLastUpdateGroup = nUpdated;
        string strUpdated = int2string(nUpdated);
        pCacheConn->set("last_update_group", strUpdated);
        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection to get total_user_updated");
    }
}

void* CSyncCenter::doSyncGroupChat(void* arg)
{
    // 标记群组聊天同步线程正在运行
    m_bSyncGroupChatRuning = true;
    CDBManager* pDBManager = CDBManager::getInstance();
    // 暂存自从上次同步群组消息之后，又有新聊天的群组的群 id 和对应最近一次聊天的时间
    map<uint32_t, uint32_t> mapChangedGroup;
    do {
        mapChangedGroup.clear();
        // mysql slave 数据库只用于读，master 数据库只用于写
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
        if (pDBConn)
        {
            string strSql = "select id,lastChated from IMGroup where status=0 and lastChated >=" + int2string(m_pInstance->getLastUpdateGroup());
            CResultSet* pResult = pDBConn->ExecuteQuery(strSql.c_str());
            if (pResult)
            {
                while (pResult->Next())
                {
                    uint32_t nGroupId = pResult->GetInt("id");
                    uint32_t nLastChat = pResult->GetInt("lastChated");
                    if (nLastChat != 0)
                    {
                        mapChangedGroup[nGroupId] = nLastChat;
                    }
                }
                delete pResult;
            }
            pDBManager->RelDBConn(pDBConn);
        }
        else
        {
            log("no db connection for teamtalk_slave");
        }

        // 更新最近一次同步群组消息的时间（写入redis）
        m_pInstance->updateLastUpdateGroup(time(NULL));
        // 更新会话或者创建会话
        for (auto it = mapChangedGroup.begin(); it != mapChangedGroup.end(); ++it)
        {
            uint32_t nGroupId = it->first;
            list<uint32_t> lsUsers;
            uint32_t nUpdate = it->second;
            CGroupModel::getInstance()->getGroupUser(nGroupId, lsUsers);
            for (auto it1 = lsUsers.begin(); it1 != lsUsers.end(); ++it1)
            {
                uint32_t nUserId = *it1;
                uint32_t nSessionId = INVALID_VALUE;
                nSessionId = CSessionModel::getInstance()->getSessionId(nUserId, nGroupId, IM::BaseDefine::SESSION_TYPE_GROUP, true);
                if (nSessionId != INVALID_VALUE)
                {
                    CSessionModel::getInstance()->updateSession(nSessionId, nUpdate);
                }
                else
                {
                    CSessionModel::getInstance()->addSession(nUserId, nGroupId, IM::BaseDefine::SESSION_TYPE_GROUP);
                }
            }
        }
//    } while (!m_pInstance->m_pCondSync->waitTime(5*1000));
    } while (m_pInstance->m_bSyncGroupChatWaitting && !(m_pInstance->m_pCondGroupChat->waitTime(5 * 1000)));
//    } while(m_pInstance->m_bSyncGroupChatWaitting);
    m_bSyncGroupChatRuning = false;
    return NULL;
}
