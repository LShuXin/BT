//
//  BTConsts.h
//  bt_ios
//
//  Created by LShuXin on 24-4-14.
//


#ifndef BTConsts_h
#define BTConsts_h

#ifdef DEBUG

#else

#endif

#define IM_PDU_HEADER_LEN   16
#define IM_PDU_VERSION      1

#define BT_BASE_URL         @"http://www.mogujie.com/"

// login server info
#define LOGIN_SERVER_URL    @"http://192.168.214.1/login"

// tcp msg server info
#define MSG_SERVER_IP       @"192.168.214.1"
#define MSG_SERVER_PORT     8081

#define DISCOVER_URL        @"http://192.168.214.1"



extern NSString* const BTNotificationLoginMsgServerSuccess;           // 登录消息服务器成功
extern NSString* const BTNotificationLoginMsgServerFailure;           // 登录消息服务器失败

extern NSString* const BTNotificationTcpLinkConnectComplete;          // tcp连接建立完成
extern NSString* const BTNotificationTcpLinkConnectFailure;           // tcp连接建立失败
extern NSString* const BTNotificationTcpLinkDisconnect;               // tcp断开连接

extern NSString* const BTNotificationUserStartLogin;                  // 用户开始登录
extern NSString* const BTNotificationUserLoginFailure;                // 用户登录失败
extern NSString* const BTNotificationUserLoginSuccess;                // 用户登录成功
extern NSString* const BTNotificationUserAutoLoginSuccess;            // 用户自动登录成功
extern NSString* const BTNotificationUserReloginSuccess;              // 用户断线重连成功
extern NSString* const BTNotificationUserOffline;                     // 用户离线
extern NSString* const BTNotificationUserKickouted;                   // 用户被挤下线
extern NSString* const BTNotificationUserInitiativeOffline;           // 用户主动离线
extern NSString* const BTNotificationUserLogout;                      // 用户登出

extern NSString* const BTNotificationRemoveSession;                   // 移除会话成功之后的通知

extern NSString* const BTNotificationServerHeartBeat;                 // 接收到服务器端的心跳

extern NSString* const BTNotificationGetAllUsers;                     // 获得所有用户

extern NSString* const BTNotificationReceiveMessage;                  // 收到一条消息
extern NSString* const BTNotificationReloadTheRecentContacts;         // 刷新最近联系人界面
extern NSString* const BTNotificationReceiveP2PShakeMessage;          // 收到P2P消息
extern NSString* const BTNotificationReceiveP2PInputingMessage;       // 收到正在输入消息
extern NSString* const BTNotificationReceiveP2PStopInputingMessage;   // 收到停止输入消息
extern NSString* const BTNotificationLoadLocalGroupFinish;            // 本地最近联系群加载完成

extern NSString* const BTNotificationRecentContactsUpdate;            // 最近联系人更新


extern NSString* const BTNotificationSendMessageSuccess;              // 消息发送成功

#endif /* BTConsts_h */
