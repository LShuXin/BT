/*================================================================
 *   Copyright (C) 2014 All rights reserved.
 *
 *   文件名称：AudioModel.h
 *   创 建 者：Zhang Yuanhao
 *   邮    箱：bluefoxah@gmail.com
 *   创建日期：2014年12月15日
 *   描    述：
 *
 ================================================================*/

#ifndef AUDIO_MODEL_H_
#define AUDIO_MODEL_H_


#include <list>
#include <map>
#include "public_define.h"
#include "util.h"
#include "IM.BaseDefine.pb.h"

using namespace std;


class CAudioModel {
public:
	virtual ~CAudioModel();

	static CAudioModel* getInstance();

	// 设置文件服务器地址
    void setUrl(string& strFileUrl);

    /**
     *  读取语音消息
     *
     *  @param nAudioId 语音的 Id
     *  @param cMsg     语音消息，引用
     *
     *  @return bool    成功返回 true，失败返回 false
     */
    bool readAudios(list<IM::BaseDefine::MsgInfo>& lsMsg);

    /**
     *  存储语音消息
     *
     *  @param nFromId     发送者Id
     *  @param nToId       接收者Id
     *  @param nCreateTime 发送时间
     *  @param pAudioData  指向语音消息的指针
     *  @param nAudioLen   语音消息的长度
     *
     *  @return 成功返回语音id，失败返回-1
     */
    int saveAudioInfo(uint32_t nFromId, uint32_t nToId, uint32_t nCreateTime, const char* pAudioData, uint32_t nAudioLen);

private:
	CAudioModel();
//    void GetAudiosInfo(uint32_t nAudioId, IM::BaseDefine::MsgInfo& msg);
    /**
     *  读取语音的具体内容
     *
     *  @param nCostTime 语音时长
     *  @param nSize     语音大小
     *  @param strPath   语音存储的url
     *  @param cMsg      msg结构体
     *
     *  @return 成功返回true，失败返回false
     */
    bool readAudioContent(uint32_t nCostTime, uint32_t nSize, const string& strPath, IM::BaseDefine::MsgInfo& msg);
    
private:
	static CAudioModel*	m_pInstance;
    string m_strFileSite;
};



#endif /* AUDIO_MODEL_H_ */
