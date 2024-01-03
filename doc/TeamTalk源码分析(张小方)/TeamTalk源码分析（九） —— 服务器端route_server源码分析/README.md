# TeamTalk源码分析（九） —— 服务器端route_server源码分析

> 原文： https://balloonwj.blog.csdn.net/article/details/72577765


route_server 的作用主要是用户不同 msg_server 之间消息路由，其框架部分和前面的服务类似，没有什么好说的。我们这里重点介绍下它的业务代码，也就是其路由细节：

 


        
	void CRouteConn::HandlePdu(CImPdu* pPdu)
	{
		  switch (pPdu->GetCommandId())
		  {
	        case CID_OTHER_HEARTBEAT:
	            // do not take any action, heart beat only update m_last_recv_tick
	            break;
	        case CID_OTHER_ONLINE_USER_INFO:
	            _HandleOnlineUserInfo( pPdu );
	            break;
	        case CID_OTHER_USER_STATUS_UPDATE:
	            _HandleUserStatusUpdate( pPdu );
	            break;
	        case CID_OTHER_ROLE_SET:
	            _HandleRoleSet( pPdu );
	            break;
	        case CID_BUDDY_LIST_USERS_STATUS_REQUEST:
	            _HandleUsersStatusRequest( pPdu );
	            break;
	        case CID_MSG_DATA:
	        case CID_SWITCH_P2P_CMD:
	        case CID_MSG_READ_NOTIFY:
	        case CID_OTHER_SERVER_KICK_USER:
	        case CID_GROUP_CHANGE_MEMBER_NOTIFY:
	        case CID_FILE_NOTIFY:
	        case CID_BUDDY_LIST_REMOVE_SESSION_NOTIFY:
	            _BroadcastMsg(pPdu, this);
	            break;
	        case CID_BUDDY_LIST_SIGN_INFO_CHANGED_NOTIFY:
	            _BroadcastMsg(pPdu);
	            break;
	        default:
	            log("CRouteConn::HandlePdu, wrong cmd id: %d ", pPdu->GetCommandId());
	            break;
	      }
	}
上面是 route_server 处理的消息类型，我们逐一来介绍：

 

## CID_OTHER_ONLINE_USER_INFO

这个消息是 msg_server 连接上 route_server 后告知 route_server 自己上面的用户登录情况。route_server 处理后，只是简单地记录一下每个 msg_server 上的用户数量和用户 id：

	void CRouteConn::_HandleOnlineUserInfo(CImPdu* pPdu)
	{
	    IM::Server::IMOnlineUserInfo msg;
	    CHECK_PB_PARSE_MSG(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()));
	    uint32_t user_count = msg.user_stat_list_size();
	
	    log("HandleOnlineUserInfo, user_cnt=%u ", user_count);
	
	    for (uint32_t i = 0; i < user_count; i++)
	    {
	        IM::BaseDefine::ServerUserStat server_user_stat = msg.user_stat_list(i);
	        _UpdateUserStatus(server_user_stat.user_id(), server_user_stat.status(), server_user_stat.client_type());
	    }
	}


## CID_OTHER_USER_STATUS_UPDATE

这个消息是当某个 msg_server 上有用户上下线时，msg_server 会给 route_server 发送自己最近的用户数量和在线用户id信息，route_server 的处理也就是更新下记录的该 msg_server上的用户数量和用户id。



## CID_OTHER_ROLE_SET

这个消息没看懂，感觉是确定主从关系的，不过感觉没什么用？



## CID_OTHER_GET_DEVICE_TOKEN_RSP

这个消息用于获取某个用户的登录情况，比如用户是 pc 、android 还是 ios 登录，用于某些特殊消息的处理，比如文件发送不会推给移动在线的用户。



## CID_MSG_DATA、CID_SWITCH_P2P_CMD、CID_MSG_READ_NOTIFY、CID_OTHER_SERVER_KICK_USER、CID_GROUP_CHANGE_MEMBER_NOTIFY、CID_FILE_NOTIFY、CID_BUDDY_LIST_REMOVE_SESSION_NOTIFY、CID_BUDDY_LIST_SIGN_INFO_CHANGED_NOTIFY

这几个消息都是往外广播消息，由于 msg_server 可以配置多个，A 给 B 发了一条消息，必须广播在各个 msg_server 才能知道 B 到底在哪个 msg_server 上。

```
void CRouteConn::_BroadcastMsg(CImPdu* pPdu, CRouteConn* pFromConn)
{
    ConnMap_t::iterator it;
    for (it = g_route_conn_map.begin(); it != g_route_conn_map.end(); it++)
    {
        CRouteConn* pRouteConn = (CRouteConn*)it->second;
        if (pRouteConn != pFromConn)
        {
          	pRouteConn->SendPdu(pPdu);
        }
    }
}
```

也就是说 CRouteConn 代表着到 msg_server 的连接。

route_server 的介绍就这么多了，虽然比较简单，但是这种路由的思想却是非常值得我们学习。网络通信数据包的在不同 ip 地址的路由最终被送达目的地，也就是这个原理。
