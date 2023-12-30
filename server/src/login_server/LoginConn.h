/*
 * LoginConn.h
 *
 *  Created on: 2013-6-21
 *      Author: jianqingdu
 */

#ifndef LOGINCONN_H_
#define LOGINCONN_H_


#include "imconn.h"

/**
 * 为什么要分为客户端登录与服务端登录？
 * 1. 服务端（msg_server）先登录，向本 loin_server 注册自己的信息
 * 2. 客户端后登录,获取分配给自己的 msg_server
 * */
enum
{
    // 客户端登录
	LOGIN_CONN_TYPE_CLIENT = 1,
	// 服务端登录
	LOGIN_CONN_TYPE_MSG_SERV
};

typedef struct
{
    string		ip_addr1;	// 电信IP
    string		ip_addr2;	// 网通IP
    uint16_t	port;
    uint32_t	max_conn_cnt;
    uint32_t	cur_conn_cnt;
    string 		hostname;	// 消息服务器的主机名
} msg_serv_info_t;

class CLoginConn : public CImConn
{
public:
	CLoginConn();
	virtual ~CLoginConn();

	virtual void Close();

	void OnConnect2(net_handle_t handle, int conn_type);
	virtual void OnClose();
	virtual void OnTimer(uint64_t curr_tick);

	virtual void HandlePdu(CImPdu* pPdu);
private:
    // 服务注册（来自 msg_server）
	void _HandleMsgServInfo(CImPdu* pPdu);
    // 用户数量更新（来自 msg_server，某个用户登录/退出某个 msg_server 时会触发）
	void _HandleUserCntUpdate(CImPdu* pPdu);
	// 请求分配 msg_server，来自客户端
	void _HandleMsgServRequest(CImPdu* pPdu);

private:
	int	m_conn_type;
};

void init_login_conn();


#endif /* LOGINCONN_H_ */