//
//  BTClientState.h
//

#import <Foundation/Foundation.h>


/**
 *  用户状态
 */
typedef NS_ENUM(NSUInteger, BTUserState)
{
    /**
     *  用户在线
     */
    USER_ONLINE,
    
    /**
     *  用户被挤下线
     */
    USER_KICKED_OUT,
    
    /**
     *  用户离线
     */
    USER_OFF_LINE,
    
    /**
     *  用户主动下线
     */
    USER_OFF_LINE_INITIATIVE,
    
    /**
     *  用户正在连接
     */
    USER_LOGINING
};

/**
 *  客户端网络状态
 */
typedef NS_ENUM(NSUInteger, BTNetWorkState)
{
    /**
     *  wifi
     */
    NETWORK_WIFI,
    
    /**
     *  3G
     */
    NETWORK_3G,
    
    /**
     *  2G
     */
    NETWORK_2G,
    
    /**
     *  无网
     */
    NETWORK_DISCONNECT
};

/**
 *  Socket 连接状态
 */
typedef NS_ENUM(NSUInteger, BTSocketState)
{
    /**
     *  Socket连接登录服务器
     */
    SOCKET_LINK_LOGIN_SERVER,
    
    /**
     *  Socket连接消息服务器
     */
    SOCKET_LINK_MESSAGE_SERVER,
    
    /**
     *  Socket没有连接
     */
    SOCKET_LINK_DISCONNECT
};

#define kBTUserState               @"userState"
#define kBTNetworkState            @"networkState"
#define kBTSocketState             @"socketState"
#define kBTUserId                  @"userId"


@class BTReachability;

@interface BTClientState : NSObject
{
    BTReachability *_reachability;
}

/**
 *  当前登录用户的状态
 */
@property(nonatomic, assign)BTUserState userState;

/**
 *  网络连接状态
 */
@property(nonatomic, assign)BTNetWorkState networkState;

/**
 *  Socket连接状态
 */
@property(nonatomic, assign)BTSocketState socketState;

/**
 *  当前登录用户的ID
 */
@property(nonatomic, retain)NSString *userId;

/**
 *  单例
 *
 *  @return 客户端状态机
 */
+(instancetype)shareInstance;

-(void)setUseStateWithoutObserver:(BTUserState)userState;

@end
