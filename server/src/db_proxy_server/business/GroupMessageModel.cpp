/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupMessageModel.cpp
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#include <map>
#include <set>

#include "../DBPool.h"
#include "../CachePool.h"
#include "GroupMessageModel.h"
#include "AudioModel.h"
#include "SessionModel.h"
#include "MessageCounter.h"
#include "Common.h"
#include "GroupModel.h"

using namespace std;

extern string strAudioEnc;

CGroupMessageModel* CGroupMessageModel::m_pInstance = NULL;

CGroupMessageModel::CGroupMessageModel()
{

}

CGroupMessageModel::~CGroupMessageModel()
{

}

CGroupMessageModel* CGroupMessageModel::getInstance()
{
	if (!m_pInstance)
	{
		m_pInstance = new CGroupMessageModel();
	}

	return m_pInstance;
}

bool CGroupMessageModel::sendMessage(
        uint32_t nFromId,
        uint32_t nGroupId,
        IM::BaseDefine::MsgType nMsgType,
        uint32_t nCreateTime,
        uint32_t nMsgId,
        const string& strMsgContent)
{
    bool bRet = false;
    if (CGroupModel::getInstance()->isInGroup(nFromId, nGroupId))
    {
        CDBManager* pDBManager = CDBManager::getInstance();
        CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
        if (pDBConn)
        {
            string strTableName = "IMGroupMessage_" + int2string(nGroupId % 8);
            string strSql = "insert into " + strTableName + " (`groupId`, `userId`, `msgId`, `content`, `type`, `status`, `updated`, `created`) "\
            "values(?, ?, ?, ?, ?, ?, ?, ?)";
            
            // 必须在释放连接前delete CPrepareStatement对象，否则有可能多个线程操作mysql对象，会crash
            CPrepareStatement* pStmt = new CPrepareStatement();
            if (pStmt->Init(pDBConn->GetMysql(), strSql))
            {
                uint32_t nStatus = 0;
                uint32_t nType = nMsgType;
                uint32_t index = 0;
                pStmt->SetParam(index++, nGroupId);
                pStmt->SetParam(index++, nFromId);
                pStmt->SetParam(index++, nMsgId);
                pStmt->SetParam(index++, strMsgContent);
                pStmt->SetParam(index++, nType);
                pStmt->SetParam(index++, nStatus);
                pStmt->SetParam(index++, nCreateTime);
                pStmt->SetParam(index++, nCreateTime);
                
                bool bRet = pStmt->ExecuteUpdate();
                if (bRet)
                {
                    CGroupModel::getInstance()->updateGroupChat(nGroupId);
                    incMessageCount(nFromId, nGroupId);
                    clearMessageCount(nFromId, nGroupId);
                } else {
                    log("insert message failed: %s", strSql.c_str());
                }
            }
            delete pStmt;
            pDBManager->RelDBConn(pDBConn);
        }
        else
        {
            log("no db connection for teamtalk_master");
        }
    }
    else
    {
        log("not in the group.fromId=%u, groupId=%u", nFromId, nGroupId);
    }
    return bRet;
}

bool CGroupMessageModel::sendAudioMessage(
        uint32_t nFromId,
        uint32_t nGroupId,
        IM::BaseDefine::MsgType nMsgType,
        uint32_t nCreateTime,
        uint32_t nMsgId,
        const char* pMsgContent,
        uint32_t nMsgLen)
{
	if (nMsgLen <= 4)
	{
		return false;
	}

    if (!CGroupModel::getInstance()->isInGroup(nFromId, nGroupId))
    {
        log("not in the group.fromId=%u, groupId=%u", nFromId, nGroupId);
        return false;
    }
    
	CAudioModel* pAudioModel = CAudioModel::getInstance();
	int nAudioId = pAudioModel->saveAudioInfo(nFromId, nGroupId, nCreateTime, pMsgContent, nMsgLen);

	bool bRet = true;
	if (nAudioId != -1)
	{
		string strMsg = int2string(nAudioId);
        bRet = sendMessage(nFromId, nGroupId, nMsgType, nCreateTime, nMsgId, strMsg);
	}
	else
    {
		bRet = false;
	}

	return bRet;
}

void CGroupMessageModel::getMessage(
        uint32_t nUserId,
        uint32_t nGroupId,
        uint32_t nMsgId,
        uint32_t nMsgCnt,
        list<IM::BaseDefine::MsgInfo>& lsMsg)
{
    // 根据 count 和 lastId 获取信息
    string strTableName = "IMGroupMessage_" + int2string(nGroupId % 8);

    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn)
    {
        uint32_t nUpdated = CGroupModel::getInstance()->getUserJoinTime(nGroupId, nUserId);
        // 如果 nMsgId 为 0 表示客户端想拉取最新的 nMsgCnt 条消息
        string strSql;
        if (nMsgId == 0)
        {
            strSql = "select * from " + strTableName + " where groupId = " + int2string(nGroupId) + " and status = 0 and created>="+ int2string(nUpdated) + " order by created desc, id desc limit " + int2string(nMsgCnt);
        }
        else
        {
            strSql = "select * from " + strTableName + " where groupId = " + int2string(nGroupId) + " and msgId<=" + int2string(nMsgId) + " and status = 0 and created>="+ int2string(nUpdated) + " order by created desc, id desc limit " + int2string(nMsgCnt);
        }

        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if (pResultSet)
        {
            // map<uint32_t, IM::BaseDefine::MsgInfo> mapAudioMsg;
            while(pResultSet->Next())
            {
                IM::BaseDefine::MsgInfo msg;
                msg.set_msg_id(pResultSet->GetInt("msgId"));
                msg.set_from_session_id(pResultSet->GetInt("userId"));
                msg.set_create_time(pResultSet->GetInt("created"));
                IM::BaseDefine::MsgType nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
                if (IM::BaseDefine::MsgType_IsValid(nMsgType))
                {
                    msg.set_msg_type(nMsgType);
                    msg.set_msg_data(pResultSet->GetString("content"));
                    lsMsg.push_back(msg);
                }
                else
                {
                    log("invalid msgType. userId=%u, groupId=%u, msgType=%u", nUserId, nGroupId, nMsgType);
                }
            }
            delete pResultSet;
        }
        else
        {
            log("no result set for sql: %s", strSql.c_str());
        }
        pDBManager->RelDBConn(pDBConn);
        if (!lsMsg.empty())
        {
            CAudioModel::getInstance()->readAudios(lsMsg);
        }
    }
    else
    {
        log("no db connection for teamtalk_slave");
    }
}

bool CGroupMessageModel::clearMessageCount(uint32_t nUserId, uint32_t nGroupId)
{
    bool bRet = false;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        string strGroupKey = int2string(nGroupId) + GROUP_TOTAL_MSG_COUNTER_REDIS_KEY_SUFFIX;
        map<string, string> mapGroupCount;
        bool bRet = pCacheConn->hgetAll(strGroupKey, mapGroupCount);
        pCacheManager->RelCacheConn(pCacheConn);
        if (bRet)
        {
            string strUserKey = int2string(nUserId) + "_" + int2string(nGroupId) + GROUP_USER_MSG_COUNTER_REDIS_KEY_SUFFIX;
            string strReply = pCacheConn->hmset(strUserKey, mapGroupCount);
            if (strReply.empty())
            {
                log("hmset %s failed !", strUserKey.c_str());
            }
            else
            {
                bRet = true;
            }
        }
        else
        {
            log("hgetAll %s failed !", strGroupKey.c_str());
        }
    }
    else
    {
        log("no cache connection for unread");
    }
    return bRet;
}


bool CGroupMessageModel::incMessageCount(uint32_t nUserId, uint32_t nGroupId)
{
    bool bRet = false;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        string strGroupKey = int2string(nGroupId) + GROUP_TOTAL_MSG_COUNTER_REDIS_KEY_SUFFIX;
        pCacheConn->hincrBy(strGroupKey, GROUP_COUNTER_SUBKEY_COUNTER_FIELD, 1);
        map<string, string> mapGroupCount;
        bool bRet = pCacheConn->hgetAll(strGroupKey, mapGroupCount);
        if(bRet)
        {
            string strUserKey = int2string(nUserId) + "_" + int2string(nGroupId) + GROUP_USER_MSG_COUNTER_REDIS_KEY_SUFFIX;
            string strReply = pCacheConn->hmset(strUserKey, mapGroupCount);
            if(!strReply.empty())
            {
                bRet = true;
            }
            else
            {
                log("hmset %s failed !", strUserKey.c_str());
            }
        }
        else
        {
            log("hgetAll %s failed!", strGroupKey.c_str());
        }
        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection for unread");
    }
    return bRet;
}

void CGroupMessageModel::getUnreadMsgCount(uint32_t nUserId, uint32_t &nTotalCnt, list<IM::BaseDefine::UnreadInfo>& lsUnreadCount)
{
    list<uint32_t> lsGroupId;
    CGroupModel::getInstance()->getUserGroupIds(nUserId, lsGroupId, 0);
    uint32_t nCount = 0;
    
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        for (auto it = lsGroupId.begin(); it != lsGroupId.end(); ++it)
        {
            uint32_t nGroupId = *it;
            string strGroupKey = int2string(nGroupId) + GROUP_TOTAL_MSG_COUNTER_REDIS_KEY_SUFFIX;
            string strGroupCnt = pCacheConn->hget(strGroupKey, GROUP_COUNTER_SUBKEY_COUNTER_FIELD);
            if (strGroupCnt.empty())
            {
                // log("hget %s : count failed !", strGroupKey.c_str());
                continue;
            }
            uint32_t nGroupCnt = (uint32_t)(atoi(strGroupCnt.c_str()));
            
            string strUserKey = int2string(nUserId) + "_" + int2string(nGroupId) + GROUP_USER_MSG_COUNTER_REDIS_KEY_SUFFIX;
            string strUserCnt = pCacheConn->hget(strUserKey, GROUP_COUNTER_SUBKEY_COUNTER_FIELD);
            
            uint32_t nUserCnt = ( strUserCnt.empty() ? 0 : ((uint32_t)atoi(strUserCnt.c_str())) );
            if (nGroupCnt >= nUserCnt) {
                nCount = nGroupCnt - nUserCnt;
            }

            if (nCount > 0)
            {
                IM::BaseDefine::UnreadInfo cUnreadInfo;
                cUnreadInfo.set_session_id(nGroupId);
                cUnreadInfo.set_session_type(IM::BaseDefine::SESSION_TYPE_GROUP);
                cUnreadInfo.set_unread_cnt(nCount);
                nTotalCnt += nCount;
                string strMsgData;
                uint32_t nMsgId;
                IM::BaseDefine::MsgType nType;
                uint32_t nFromId;
                getLastMsg(nGroupId, nMsgId, strMsgData, nType, nFromId);
                if (IM::BaseDefine::MsgType_IsValid(nType))
                {
                    cUnreadInfo.set_latest_msg_id(nMsgId);
                    cUnreadInfo.set_latest_msg_data(strMsgData);
                    cUnreadInfo.set_latest_msg_type(nType);
                    cUnreadInfo.set_latest_msg_from_user_id(nFromId);
                    lsUnreadCount.push_back(cUnreadInfo);
                }
                else
                {
                    log("invalid msgType. userId=%u, groupId=%u, msgType=%u, msgId=%u", nUserId, nGroupId, nType, nMsgId);
                }
            }
        }
        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection for unread");
    }
}

uint32_t CGroupMessageModel::getMsgId(uint32_t nGroupId)
{
    uint32_t nMsgId = 0;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        // TODO
        string strKey = "group_msg_id_" + int2string(nGroupId);
        nMsgId = pCacheConn->incrBy(strKey, 1);
        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection for unread");
    }
    return nMsgId;
}

void CGroupMessageModel::getLastMsg(
        uint32_t nGroupId,
        uint32_t &nMsgId,
        string &strMsgData,
        IM::BaseDefine::MsgType &nMsgType,
        uint32_t& nFromId)
{
    string strTableName = "IMGroupMessage_" + int2string(nGroupId % 8);
    
    CDBManager* pDBManager = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
    if (pDBConn)
    {
        string strSql = "select msgId,type,userId,content from " + strTableName + " where groupId = " + int2string(nGroupId) + " and status = 0 order by created desc, id desc limit 1";
        
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if (pResultSet)
        {
            while (pResultSet->Next())
            {
                nMsgId = pResultSet->GetInt("msgId");
                nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
                nFromId = pResultSet->GetInt("userId");
                if (nMsgType == IM::BaseDefine::MSG_TYPE_GROUP_AUDIO)
                {
                    // "[语音]"加密后的字符串
                    strMsgData = strAudioEnc;
                }
                else
                {
                    strMsgData = pResultSet->GetString("content");
                }
            }
            delete pResultSet;
        }
        else
        {
            log("no result set for sql: %s", strSql.c_str());
        }
        pDBManager->RelDBConn(pDBConn);
    }
    else
    {
        log("no db connection for teamtalk_slave");
    }
}

void CGroupMessageModel::getUnReadCntAll(uint32_t nUserId, uint32_t &nTotalCnt)
{
    list<uint32_t> lsGroupId;
    CGroupModel::getInstance()->getUserGroupIds(nUserId, lsGroupId, 0);
    uint32_t nCount = 0;
    
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if (pCacheConn)
    {
        for (auto it = lsGroupId.begin(); it != lsGroupId.end(); ++it)
        {
            uint32_t nGroupId = *it;
            string strGroupKey = int2string(nGroupId) + GROUP_TOTAL_MSG_COUNTER_REDIS_KEY_SUFFIX;
            string strGroupCnt = pCacheConn->hget(strGroupKey, GROUP_COUNTER_SUBKEY_COUNTER_FIELD);
            if (strGroupCnt.empty())
            {
                // log("hget %s : count failed !", strGroupKey.c_str());
                continue;
            }
            uint32_t nGroupCnt = (uint32_t)(atoi(strGroupCnt.c_str()));
            
            string strUserKey = int2string(nUserId) + "_" + int2string(nGroupId) + GROUP_USER_MSG_COUNTER_REDIS_KEY_SUFFIX;
            string strUserCnt = pCacheConn->hget(strUserKey, GROUP_COUNTER_SUBKEY_COUNTER_FIELD);
            
            uint32_t nUserCnt = ( strUserCnt.empty() ? 0 : ((uint32_t)atoi(strUserCnt.c_str())) );
            if (nGroupCnt >= nUserCnt) {
                nCount = nGroupCnt - nUserCnt;
            }
            if (nCount > 0)
            {
                nTotalCnt += nCount;
            }
        }
        pCacheManager->RelCacheConn(pCacheConn);
    }
    else
    {
        log("no cache connection for unread");
    }
}

void CGroupMessageModel::getMsgByMsgId(uint32_t nUserId, uint32_t nGroupId, const list<uint32_t> &lsMsgId, list<IM::BaseDefine::MsgInfo> &lsMsg)
{
    if(!lsMsgId.empty())
    {
        if (CGroupModel::getInstance()->isInGroup(nUserId, nGroupId))
        {
            CDBManager* pDBManager = CDBManager::getInstance();
            CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_slave");
            if (pDBConn)
            {
                string strTableName = "IMGroupMessage_" + int2string(nGroupId % 8);
                uint32_t nUpdated = CGroupModel::getInstance()->getUserJoinTime(nGroupId, nUserId);
                string strClause ;
                bool bFirst = true;
                for(auto it= lsMsgId.begin(); it!=lsMsgId.end();++it)
                {
                    if (bFirst) {
                        bFirst = false;
                        strClause = int2string(*it);
                    }
                    else
                    {
                        strClause += ("," + int2string(*it));
                    }
                }
                
                string strSql = "select * from " + strTableName + " where groupId=" + int2string(nGroupId) + " and msgId in (" + strClause + ") and status=0 and created >= " + int2string(nUpdated) + " order by created desc, id desc limit 100";
                CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
                if (pResultSet)
                {
                    while (pResultSet->Next())
                    {
                        IM::BaseDefine::MsgInfo msg;
                        msg.set_msg_id(pResultSet->GetInt("msgId"));
                        msg.set_from_session_id(pResultSet->GetInt("userId"));
                        msg.set_create_time(pResultSet->GetInt("created"));
                        IM::BaseDefine::MsgType nMsgType = IM::BaseDefine::MsgType(pResultSet->GetInt("type"));
                        if(IM::BaseDefine::MsgType_IsValid(nMsgType))
                        {
                            msg.set_msg_type(nMsgType);
                            msg.set_msg_data(pResultSet->GetString("content"));
                            lsMsg.push_back(msg);
                        }
                        else
                        {
                            log("invalid msgType. userId=%u, groupId=%u, msgType=%u", nUserId, nGroupId, nMsgType);
                        }
                    }
                    delete pResultSet;
                }
                else
                {
                    log("no result set for sql:%s", strSql.c_str());
                }
                pDBManager->RelDBConn(pDBConn);
                if(!lsMsg.empty())
                {
                    CAudioModel::getInstance()->readAudios(lsMsg);
                }
            }
            else
            {
                log("no db connection for teamtalk_slave");
            }
        }
        else
        {
            log("%u is not in group:%u", nUserId, nGroupId);
        }
    }
    else
    {
        log("msgId is empty.");
    }
}

bool CGroupMessageModel::resetMsgId(uint32_t nGroupId)
{
    bool bRet = false;
    CacheManager* pCacheManager = CacheManager::getInstance();
    CacheConn* pCacheConn = pCacheManager->GetCacheConn("unread");
    if(pCacheConn)
    {
        string strKey = "group_msg_id_" + int2string(nGroupId);
        string strValue = "0";
        string strReply = pCacheConn->set(strKey, strValue);
        if(strReply == strValue)
        {
            bRet = true;
        }
        pCacheManager->RelCacheConn(pCacheConn);
    }
    return bRet;
}