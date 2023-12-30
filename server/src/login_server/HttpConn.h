/*
 * HttpConn.h
 *
 *  Created on: 2013-9-29
 *      Author: ziteng
 */

#ifndef __HTTP_CONN_H__
#define __HTTP_CONN_H__


#include "netlib.h"
#include "util.h"
#include "HttpParserWrapper.h"

#define HTTP_CONN_TIMEOUT			60000

#define READ_BUF_SIZE	2048
#define HTTP_RESPONSE_HTML          "HTTP/1.1 200 OK\r\n"\
                                    "Connection:close\r\n"\
                                    "Content-Length:%d\r\n"\
                                    "Content-Type:text/html;charset=utf-8\r\n\r\n%s"
#define HTTP_RESPONSE_HTML_MAX      1024

enum {
    CONN_STATE_IDLE,
    CONN_STATE_CONNECTED,
    CONN_STATE_OPEN,
    CONN_STATE_CLOSED,
};

class CHttpConn : public CRefObject
{
public:
	CHttpConn();
	virtual ~CHttpConn();

	uint32_t GetConnHandle() { return m_conn_handle; }
	char* GetPeerIP() { return (char*)m_peer_ip.c_str(); }

	int Send(void* data, int len);

    void Close();
    void OnConnect(net_handle_t handle);
    // 读取 m_sock_handle 上的数据
    void OnRead();
    void OnWrite();
    void OnClose();

    // 实现判断 http 连接是否超时的逻辑
    void OnTimer(uint64_t curr_tick);

    // 关闭 http 连接
    void OnWriteComlete();
private:
    // 真正根据请求参数处理 http 请求的逻辑在这里
    // 根据简单的负载均衡逻辑将 msg_server、msfs_server 的地址返回给客户端
    void _HandleMsgServRequest(string& url, string& post_data);

protected:
    // 真正的 socket handle
	net_handle_t	m_sock_handle;
	// 用于唯一标识每个连接的数字
	uint32_t		m_conn_handle;
	// 某次数据发送成功的数量小于要求发送的数量时代表繁忙
	// 此时应该将未发送成功的数据放入发送缓冲区
	bool			m_busy;

    uint32_t        m_state;
	std::string		m_peer_ip;
	uint16_t		m_peer_port;
	// 接收 buffer
	CSimpleBuffer	m_in_buf;
	// 发送 buffer
	CSimpleBuffer	m_out_buf;
    // 最近一次发送数据的时间
	uint64_t		m_last_send_tick;
	// 最近一次接收数据的时间
	uint64_t		m_last_recv_tick;
    
    CHttpParserWrapper m_cHttpParser;
};

typedef hash_map<uint32_t, CHttpConn*> HttpConnMap_t;

// 查找指定的 http 连接对象
CHttpConn* FindHttpConnByHandle(uint32_t handle);
void init_http_conn();


#endif /* IMCONN_H_ */