/*
 * ProxyConn.h
 *
 *	Connection from IM MsgServer
 *  Created on: 2014年7月25日
 *      Author: ziteng
 */

#ifndef PROXYCONN_H_
#define PROXYCONN_H_

#include <curl/curl.h>
#include "../base/util.h"
#include "imconn.h"

/**
 * 由于 socket 复用，需要加一个 uuid 字段来唯一标识进入的每一个消息
 * */
typedef struct
{
    /**
     * 唯一标识一个连接
     * */
	uint32_t	conn_uuid;

	/**
	 * 数据
	 * */
	CImPdu*		pPdu;
} ResponsePdu_t;

class CProxyConn : public CImConn
{
public:
	CProxyConn();
	virtual ~CProxyConn();

	virtual void Close();

	virtual void OnConnect(net_handle_t handle);
	virtual void OnRead();
	virtual void OnClose();

	/**
	 * 主要用于向客户端发送心跳
	 * */
	virtual void OnTimer(uint64_t curr_tick);

	/**
	 * 根据 pdu 的 commandId 查找对应的 handler，用然后新建一个任务放入线程池来执行这个任务
	 * */
	void HandlePduBuf(uchar_t* pdu_buf, uint32_t pdu_len);

    /**
     * 静态方法；
     * 工作线程调用此方法将相应消息的响应数据包放入主线程响应消息队列，主线程再实现一一响应
     * 当响应数据包的数据内容为空时，代表要关闭此连接，比如当消息体解析错误时
     * */
	static void AddResponsePdu(uint32_t conn_uuid, CImPdu* pPdu);

    /**
     * 主线程调用此方法实现对消息的回复
     * */
	static void SendResponsePduList();
private:
	// 由于处理请求和发送回复在两个线程，socket 的 handle 可能重用，所以需要用一个一直增加的 uuid 来表示一个连接
	// 该字段表示上一个uuid
	static uint32_t	            s_uuid_alloctor;

	// 当前处理消息的唯一标识
	uint32_t		            m_uuid;

	// 用于保护主线程回复消息队列的锁
	static CLock			    s_list_lock;

    // 主线程用于回复消息的响应消息队列
	static list<ResponsePdu_t*>	s_response_pdu_list;
};

/**
 * 初始化线程池、初始化主线程工作循环、初始化心跳循环，注册 term 信号处理器
 * */
int init_proxy_conn(uint32_t thread_num);

/**
 * 由于存在 socket 复用，需要使用 uuid 来找到更具体的消息连接来回复
 * */
CProxyConn* get_proxy_conn_by_uuid(uint32_t uuid);

#endif /* PROXYCONN_H_ */
