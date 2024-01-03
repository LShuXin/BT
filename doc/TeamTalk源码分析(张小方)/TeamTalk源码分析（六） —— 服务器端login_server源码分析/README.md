

# TeamTalk源码分析（六） —— 服务器端 login_server 源码分析

> 原文：https://balloonwj.blog.csdn.net/article/details/72472049

login_server 从严格意义上来说，是一个登录分流器，所以名字起的有点名不符实。该服务根据已知的 msg_server 上的在线用户数量来返回告诉一个即将登录的用户登录哪个 msg_server 比较合适。关于其程序框架的非业务代码我们已经在前面的两篇文章《TeamTalk源码分析（四） —— 服务器端 db_proxy_server 源码分析》和《TeamTalk源码分析（五） —— 服务器端msg_server源码分析》中介绍过了。这篇文章主要介绍下其业务代码。

 

首先，程序初始化的时候，会初始化如下功能：

 ```
 //1. 在8008端口监听客户端连接
 
 //2. 在8100端口上监听 msg_server 的连接
 
 //3. 在8080端口上监听客户端 http 连接
 ```

其中连接对象 CLoginConn 代表着 login_server 与 msg_server 之间的连接；而 CHttpConn 代表着与客户端的 http 连接。我们先来看 CLoginConn 对象，上一篇文章中也介绍了其业务代码主要在其 HandlePdu() 函数中，可以看到这路连接主要处理哪些数据包：

    void CLoginConn::HandlePdu(CImPdu* pPdu)
    {
    	  switch (pPdu->GetCommandId())
    	  {
            case CID_OTHER_HEARTBEAT:
                break;
            case CID_OTHER_MSG_SERV_INFO:
                _HandleMsgServInfo(pPdu);
                break;
            case CID_OTHER_USER_CNT_UPDATE:
                _HandleUserCntUpdate(pPdu);
                break;
            case CID_LOGIN_REQ_MSGSERVER:
                _HandleMsgServRequest(pPdu);
                break;
            default:
                  log("wrong msg, cmd id=%d ", pPdu->GetCommandId());
                  break;
          }
    }
命令号 CID_OTHER_HEARTBEAT 是与 msg_server 的心跳包。上一篇文章《TeamTalk源码分析（五） —— 服务器端msg_server源码分析》中介绍过，msg_server 连上 login_server 后会立刻给 login_server 发一个数据包，该数据包里面含有该 msg_server 上的用户数量、最大可容纳的用户数量、自己的 ip 地址和端口号。

 ```
 list<user_conn_t> user_conn_list;
 CImUserManager::GetInstance()->GetUserConnCnt(&user_conn_list, cur_conn_cnt);
 char hostname[256] = {0};
 gethostname(hostname, 256);
 
 IM::Server::IMMsgServInfo msg;
 msg.set_ip1(g_msg_server_ip_addr1);
 msg.set_ip2(g_msg_server_ip_addr2);
 msg.set_port(g_msg_server_port);
 msg.set_max_conn_cnt(g_max_conn_cnt);
 msg.set_cur_conn_cnt(cur_conn_cnt);
 msg.set_host_name(hostname);
 CImPdu pdu;
 pdu.SetPBMsg(&msg);
 pdu.SetServiceId(SID_OTHER);
 pdu.SetCommandId(CID_OTHER_MSG_SERV_INFO);
 SendPdu(&pdu);
 ```

命令号是 CID_OTHER_MSG_SERV_INFO。我们来看下 login_server 如何处理这个命令的：

	void CLoginConn::_HandleMsgServInfo(CImPdu* pPdu)
	{
			msg_serv_info_t* pMsgServInfo = new msg_serv_info_t;
	    IM::Server::IMMsgServInfo msg;
	    msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength());
	    pMsgServInfo->ip_addr1 = msg.ip1();
	    pMsgServInfo->ip_addr2 = msg.ip2();
	    pMsgServInfo->port = msg.port();
	    pMsgServInfo->max_conn_cnt = msg.max_conn_cnt();
	    pMsgServInfo->cur_conn_cnt = msg.cur_conn_cnt();
	    pMsgServInfo->hostname = msg.host_name();
	    g_msg_serv_info.insert(make_pair(m_handle, pMsgServInfo));
	 
	    g_total_online_user_cnt += pMsgServInfo->cur_conn_cnt;
	 
	    log("MsgServInfo, ip_addr1=%s, ip_addr2=%s, port=%d, max_conn_cnt=%d, cur_conn_cnt=%d, "\
	      "hostname: %s. ",
	      pMsgServInfo->ip_addr1.c_str(), pMsgServInfo->ip_addr2.c_str(), pMsgServInfo->port,pMsgServInfo->max_conn_cnt,
	      pMsgServInfo->cur_conn_cnt, pMsgServInfo->hostname.c_str());
	}
其实所做的工作无非就是记录下的该 msg_server 上的 ip、端口号、在线用户数量和最大可容纳用户数量等信息而已。存在一个全局 map 里面：

```
map<uint32_t, msg_serv_info_t*> g_msg_serv_info;

typedef struct {
    string		ip_addr1;	// 电信IP
    string		ip_addr2;	// 网通IP
    uint16_t	port;
    uint32_t	max_conn_cnt;
    uint32_t	cur_conn_cnt;
    string 		hostname;	// 消息服务器的主机名
} msg_serv_info_t;
```

另外一个命令号 CID_OTHER_USER_CNT_UPDATE，是当 msg_server 上的用户上线或下线时，msg_server 给 login_server 发该类型的命令号，让 login_server更新保存的 msg_server 的上的在线用户数量：

	void CLoginConn::_HandleUserCntUpdate(CImPdu* pPdu)
	{
	    map<uint32_t, msg_serv_info_t*>::iterator it = g_msg_serv_info.find(m_handle);
	    if (it != g_msg_serv_info.end())
	    {
	        msg_serv_info_t* pMsgServInfo = it->second;
	        IM::Server::IMUserCntUpdate msg;
	        msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength());
	        uint32_t action = msg.user_action();
	        if (action == USER_CNT_INC)
	        {
	            pMsgServInfo->cur_conn_cnt++;
	            g_total_online_user_cnt++;
	        }
	        else
	        {
	            pMsgServInfo->cur_conn_cnt--;
	            g_total_online_user_cnt--;
	        }
	
	        log("%s:%d, cur_cnt=%u, total_cnt=%u ",
	            pMsgServInfo->hostname.c_str(),
	            pMsgServInfo->port,
	            pMsgServInfo->cur_conn_cnt,
	            g_total_online_user_cnt);
	    }
	}
命令号 CID_LOGIN_REQ_MSGSERVER 没用到。 

接着说 login_server 与客户端的 http 连接处理，这个连接收取数据和解包是直接在 CHttpConn 的 OnRead 函数里面处理的：

	void CHttpConn::OnRead()
	{
	    // 读取此连接上的所有数据
	    for (;;)
	    {
	        uint32_t free_buf_len = m_in_buf.GetAllocSize() - m_in_buf.GetWriteOffset();
	        if (free_buf_len < READ_BUF_SIZE + 1)
	        {
	            m_in_buf.Extend(READ_BUF_SIZE + 1);
	        }
	
	        int ret = netlib_recv(m_sock_handle, m_in_buf.GetBuffer() + m_in_buf.GetWriteOffset(), READ_BUF_SIZE);
	        if (ret <= 0)
	        {
	            break;
	        }
	
	        m_in_buf.IncWriteOffset(ret);
	        m_last_recv_tick = get_tick_count();
	    }
	
	    // 每次请求对应一个 HTTP 连接，所以读完数据后，不用在同一个连接里面准备读取下个请求
	    char* in_buf = (char*)m_in_buf.GetBuffer();
	    uint32_t buf_len = m_in_buf.GetWriteOffset();
	    in_buf[buf_len] = '\0';
	
	    // 如果buf_len 过长可能是受到攻击，则断开连接
	    // 正常的 url 最大长度为2048，我们接受的所有数据长度不得大于1K
	    if (buf_len > 1024)
	    {
	        log("get too much data:%s ", in_buf);
	        Close();
	        return;
	    }
	
	    // log("OnRead, buf_len=%u, conn_handle=%u\n", buf_len, m_conn_handle); // for debug
	    m_cHttpParser.ParseHttpContent(in_buf, buf_len);
	    if (m_cHttpParser.IsReadAll())
	    {
	        string url = m_cHttpParser.GetUrl();
	        if (strncmp(url.c_str(), "/msg_server", 11) == 0)
	        {
	            string content = m_cHttpParser.GetBodyContent();
	            _HandleMsgServRequest(url, content);
	        }
	        else
	        {
	            log("url unknown, url=%s ", url.c_str());
	            Close();
	        }
	    }
	}


如果用户发送的 http 请求的地址形式是 http://192.168.226.128:8080/msg_server，即路径是 /msg_server，则调用 _HandleMsgServRequest() 函数处理：   

    // 为用户分配一个 msg_server
    void CHttpConn::_HandleMsgServRequest(string& url, string& post_data)
    {
        msg_serv_info_t* pMsgServInfo;
        uint32_t min_user_cnt = (uint32_t)-1;
        map<uint32_t, msg_serv_info_t*>::iterator it_min_conn = g_msg_serv_info.end();
        map<uint32_t, msg_serv_info_t*>::iterator it;
        if (g_msg_serv_info.size() <= 0)
        {
            Json::Value value;
            value["code"] = 1;
            value["msg"] = "没有msg_server";
            string strContent = value.toStyledString();
            char* szContent = new char[HTTP_RESPONSE_HTML_MAX];
            snprintf(szContent, HTTP_RESPONSE_HTML_MAX, HTTP_RESPONSE_HTML, strContent.length(), strContent.c_str());
            Send((void*)szContent, strlen(szContent));
            delete [] szContent;
            return ;
        }
        for (it = g_msg_serv_info.begin() ; it != g_msg_serv_info.end(); it++)
        {
            pMsgServInfo = it->second;
            if ((pMsgServInfo->cur_conn_cnt < pMsgServInfo->max_conn_cnt) &&
                (pMsgServInfo->cur_conn_cnt < min_user_cnt))
            {
                it_min_conn = it;
                min_user_cnt = pMsgServInfo->cur_conn_cnt;
            }
        }
    
        if (it_min_conn == g_msg_serv_info.end())
        {
            log("All TCP MsgServer are full ");
            Json::Value value;
            value["code"] = 2;
            value["msg"] = "负载过高";
            string strContent = value.toStyledString();
            char* szContent = new char[HTTP_RESPONSE_HTML_MAX];
            snprintf(szContent, HTTP_RESPONSE_HTML_MAX, HTTP_RESPONSE_HTML, strContent.length(), strContent.c_str());
            Send((void*)szContent, strlen(szContent));
            delete [] szContent;
            return;
        }
        else
        {
            Json::Value value;
            value["code"] = 0;
            value["msg"] = "";
            if (pIpParser->isTelcome(GetPeerIP()))
            {
                value["priorIP"] = string(it_min_conn->second->ip_addr1);
                value["backupIP"] = string(it_min_conn->second->ip_addr2);
                value["msfsPrior"] = strMsfsUrl;
                value["msfsBackup"] = strMsfsUrl;
            }
            else
            {
                value["priorIP"] = string(it_min_conn->second->ip_addr2);
                value["backupIP"] = string(it_min_conn->second->ip_addr1);
                value["msfsPrior"] = strMsfsUrl;
                value["msfsBackup"] = strMsfsUrl;
            }
            value["discovery"] = strDiscovery;
            value["port"] = int2string(it_min_conn->second->port);
            string strContent = value.toStyledString();
            char* szContent = new char[HTTP_RESPONSE_HTML_MAX];
            uint32_t nLen = strContent.length();
            snprintf(szContent, HTTP_RESPONSE_HTML_MAX, HTTP_RESPONSE_HTML, nLen, strContent.c_str());
            Send((void*)szContent, strlen(szContent));
            delete [] szContent;
            return;
        }
    }
其实就是根据记录的 msg_server 的负载情况，返回一个可用的 msg_server ip和端口给客户端，这是一个 json 格式：

 ```
 {
     "backupIP" : "localhost",
     "code" : 0,
     "discovery" : "http://192.168.226.128</span>/api/discovery",
     "msfsBackup" : "http://127.0.0.1:8700/",
     "msfsPrior" : "http://127.0.0.1:8700/",
     "msg" : "",
     "port" : "8000",
     "priorIP" : "localhost"
  }
 ```


里面含有 msg_server 和聊天图片存放的服务器地址（msfsPrior）字段。这样客户端可以拿着这个地址去登录 msg_server 和图片服务器了。

发出去这个 json 之后会调用 OnWriteComplete() 函数，这个函数立刻关闭该 http 连接，也就是说这个与客户端的 http 连接是短连接：

 ```
 void CHttpConn::OnWriteComlete()
 {
     log("write complete ");
     Close();
 }login_server就这么多内容了。
 ```

如果您对服务器开发技术感兴趣，可以关注我的微信公众号『高性能服务器开发』，这个微信公众号致力于将服务器开发技术通俗化、平民化，让服务器开发技术不再神秘，其中整理了将服务器开发需要掌握的一些基础技术归纳整理，既有基础理论部分，也有实战部分。