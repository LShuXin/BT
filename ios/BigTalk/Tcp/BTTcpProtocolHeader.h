//
// BTTcpProtocolHeader.h
//


#import <Foundation/Foundation.h>
#import <stdint.h>


// SID
enum
{
    SID_LOGIN                       = 0x0001,            // 登录相关
    SID_SESSION                     = 0x0002,            // Session相关
    SID_MESSAGE                     = 0x0003,            // 消息相关
    SID_GROUP                       = 0x0004,            // 群相关
    SID_HEARTBEAT                   = 0x0007             // 心跳相关
};

// CID
enum
{
    CID_LOGIN_REQ                   = 0x0103,            // 登录请求
    CID_LOGIN_RES                   = 0x0104,            // 登录请求结果
    CID_LOGOUT_REQ                  = 0x0105,            // 用户退出请求
    CID_LOGOUT_RES                  = 0x0106,            // 用户退出请求结果
    CID_KICK_USER_RES               = 0x0107,            // 踢出用户请求
    CID_PUSH_TOKEN_REQ              = 0x0108,            // 发送token请求
    CID_PUSH_TOKEN_RES              = 0x0109,            // 发送token请求结果
    
    CID_RECENT_SESSION_REQ          = 0x0201,            // 获取最近会话请求
    CID_RECENT_SESSION_RES          = 0x0202,            // 获取最近会话请求结果
    CID_REMOVE_SESSION_REQ          = 0x0206,            // 删除最近会话请求
    CID_REMOVE_SESSION_RES          = 0x0207,            // 删除最近会话请求结果
    CID_FRI_ALL_USER_REQ            = 0x0208,            // 获取公司全部员工信息请求
    CID_FRI_ALL_USER_RES            = 0x0209,            // 获取公司全部员工信息请求结果
    CID_DEPARTINFO_REQ              = 0x0210,            // 部门信息请求
    CID_DEPARTINFO_RES              = 0x0211,            // 部门信息请求结果
    
    CID_MSG_DATA                    = 0x0301,            // 收到聊天消息
    CID_MSG_DATA_ACK                = 0x0302,            // 收到聊天消息之后的ack
    CID_MSG_READ_ACK                = 0x0303,            // 消息已读确认（是否要改为 CID_MSG_RECV_ACK）
    CID_MSG_READ_NOTIFY             = 0x0304,            // 消息已读通知，用于实现对方已读功能
    CID_MSG_UNREAD_CNT_REQ          = 0x0307,            // 未读消息数请求
    CID_MSG_UNREAD_CNT_RES          = 0x0308,            // 未读消息数请求结果
    CID_MSG_LIST_REQ                = 0x0309,            // 获取一组消息请求
    CID_MSG_LIST_RES                = 0x030a,            // 获取一组消息请求结果
    CID_LASTEST_MSG_ID_REQ          = 0x030b,            // 获取最近一条消息请求
    CID_LASTEST_MSG_ID_RES          = 0x030c,            // 获取最近一条消息请求结果
    CID_GET_MSG_BY_IDS_REQ          = 0x030d,            // 获取指定消息请求
    CID_GET_MSG_BY_IDS_RES          = 0x030e,            // 获取指定消息请求结果
    
    CID_GROUP_LIST_REQ              = 0x0401,            // 群列表请求
    CID_GROUP_LIST_RES              = 0x0402,            // 群列表请求结果
    CID_GROUP_USER_LIST_REQ         = 0x0403,            // 群成员列表请求
    CID_GROUP_USER_LIST_RES         = 0x0404,            // 群成员列表请求结果
    CID_CREATE_TMP_GROUP_REQ        = 0x0405,            // 创建临时群请求
    CID_CREATE_TMP_GROUP_RES        = 0x0406,            // 创建临时群请求结果
    CID_CHANGE_GROUP_REQ            = 0x0407,            // 编辑群请求
    CID_CHANGE_GROUP_RES            = 0x0408,            // 编辑群请求结果
    CID_GROUP_SHIELD_GROUP_REQUEST  = 0x0409,            // 屏蔽群请求
    CID_GROUP_SHIELD_GROUP_RESPONSE = 0x040a,            // 屏蔽群请求结果
    
    CID_HEARTBEAT_REQ               = 0x0701,            // 心跳
    CID_HEARTBEAT_RES               = 0x0701             // 心跳
};

@interface BTTcpProtocolHeader : NSObject
@property(nonatomic, assign)UInt16 version;
@property(nonatomic, assign)UInt16 flag;
@property(nonatomic, assign)UInt16 serviceID;
@property(nonatomic, assign)UInt16 commandID;
@property(nonatomic, assign)UInt16 reserved;
@property(nonatomic, assign)UInt16 error;
@end
