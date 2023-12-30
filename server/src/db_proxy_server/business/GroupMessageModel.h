/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：GroupMessageModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef GROUP_MESSAGE_MODEL_H_
#define GROUP_MESSAGE_MODEL_H_

#include <list>
#include <string>

#include "util.h"
#include "ImPduBase.h"
#include "AudioModel.h"
#include "GroupModel.h"
#include "IM.BaseDefine.pb.h"

using namespace std;


class CGroupMessageModel {
public:
	virtual ~CGroupMessageModel();
	static CGroupMessageModel* getInstance();

    /**
     *  发送群消息接口
     *
     *  @param nFromId       发送者Id
     *  @param nGroupId      群组Id
     *  @param nMsgType      消息类型
     *  @param nCreateTime   消息创建时间
     *  @param nMsgId        消息Id
     *  @param strMsgContent 消息类容
     *
     *  @return 成功返回true 失败返回false
     */
    bool sendMessage(
            uint32_t nFromId,
            uint32_t nGroupId,
            IM::BaseDefine::MsgType nMsgType,
            uint32_t nCreateTime,
            uint32_t nMsgId,
            const string& strMsgContent);

    /**
     *  发送群组语音信息
     *
     *  @param nRelateId   关系Id
     *  @param nFromId     发送者Id
     *  @param nGroupId    群组Id
     *  @param nMsgType    消息类型
     *  @param nCreateTime 消息创建时间
     *  @param nMsgId      消息Id
     *  @param pMsgContent 指向语音类容的指针
     *  @param nMsgLen     语音消息长度
     *
     *  @return 成功返回true，失败返回false
     */
    bool sendAudioMessage(
            uint32_t nFromId,
            uint32_t nGroupId,
            IM::BaseDefine::MsgType nMsgType,
            uint32_t nCreateTime,
            uint32_t nMsgId,
            const char* pMsgContent,
            uint32_t nMsgLen);

    /**
     *  获取群组消息列表
     *
     *  @param nUserId  用户Id
     *  @param nGroupId 群组Id
     *  @param nMsgId   开始的msgId(最新的msgId)
     *  @param nMsgCnt  获取的长度
     *  @param lsMsg    消息列表
     */
    void getMessage(
            uint32_t nUserId,
            uint32_t nGroupId,
            uint32_t nMsgId,
            uint32_t nMsgCnt,
            list<IM::BaseDefine::MsgInfo>& lsMsg);

    /**
     *  清除群组消息计数
     *
     *  @param nUserId  用户Id
     *  @param nGroupId 群组Id
     *
     *  @return 成功返回true，失败返回false
     */
    bool clearMessageCount(uint32_t nUserId, uint32_t nGroupId);

    /**
     *  获取一个群组的msgId，自增，通过redis控制
     *
     *  @param nGroupId 群Id
     *
     *  @return 返回msgId
     */
    uint32_t getMsgId(uint32_t nGroupId);

    /**
     *  获取用户群未读消息计数
     *
     *  @param nUserId       用户Id
     *  @param nTotalCnt     总条数
     *  @param lsUnreadCount 每个会话的未读信息包含了条数，最后一个消息的 Id，最后一个消息的类型，最后一个消息的类容
     */
    void getUnreadMsgCount(
            uint32_t nUserId,
            uint32_t &nTotalCnt,
            list<IM::BaseDefine::UnreadInfo>& lsUnreadCount);

    /**
     *  获取一个群的最后一条消息
     *
     *  @param nGroupId   群Id
     *  @param nMsgId     最后一条消息的msgId,引用
     *  @param strMsgData 最后一条消息的内容,引用
     *  @param nMsgType   最后一条消息的类型,引用
     */
    void getLastMsg(
            uint32_t nGroupId,
            uint32_t& nMsgId,
            string& strMsgData,
            IM::BaseDefine::MsgType & nMsgType,
            uint32_t& nFromId);

    /**
     *  获取某个用户所有群的所有未读计数之和
     *
     *  @param nUserId   用户Id
     *  @param nTotalCnt 未读计数之和,引用
     */
    void getUnReadCntAll(uint32_t nUserId, uint32_t &nTotalCnt);

    void getMsgByMsgId(
            uint32_t nUserId,
            uint32_t nGroupId,
            const list<uint32_t>& lsMsgId,
            list<IM::BaseDefine::MsgInfo>& lsMsg);

    bool resetMsgId(uint32_t nGroupId);

private:
    CGroupMessageModel();

    /**
     *  增加群消息计数
     *
     *  @param nUserId  用户Id
     *  @param nGroupId 群组Id
     *
     *  @return 成功返回true，失败返回false
     */
    bool incMessageCount(uint32_t nUserId, uint32_t nGroupId);

private:
	static CGroupMessageModel*	m_pInstance;
};



#endif /* MESSAGE_MODEL_H_ */
