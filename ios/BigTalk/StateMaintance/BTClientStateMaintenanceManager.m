//
//  BTClientStateMaintenanceManager.m
//

#import "BTClientStateMaintenanceManager.h"
#import "BTTcpClientManager.h"
#import "BTClientState.h"
#import "BTReachability.h"
#import "BTHeartbeatAPI.h"
#import "BTLoginModule.h"
#import "BTNetworkStatusNotifyUI.h"
#import "BTRecentUsersViewController.h"


static NSInteger const heartBeatTimeinterval = 30;
static NSInteger const serverHeartBeatTimeinterval = 60;
static NSInteger const reloginTimeinterval = 5;

@interface BTClientStateMaintenanceManager(PrivateAPI)

// 注册KVO
-(void)p_registerClientStateObserver;
// 检验服务器端的心跳
-(void)p_startCheckServerHeartBeat;
-(void)p_stopCheckServerHeartBeat;
-(void)p_onCheckServerHeartTimer:(NSTimer *)timer;
-(void)n_receiveServerHeartBeat;
// 客户端心跳
-(void)p_onSendHeartBeatTimer:(NSTimer *)timer;
// 断线重连
-(void)p_startRelogin;
-(void)p_onReloginTimer:(NSTimer *)timer;
-(void)p_onReserverHeartTimer:(NSTimer *)timer;

@end

@implementation BTClientStateMaintenanceManager
{
    NSTimer *_sendHeartTimer;
    NSTimer *_reloginTimer;
    NSTimer *_serverHeartBeatTimer;
    
    BOOL _receiveServerHeart;
    NSUInteger _reloginInterval;
}

+(instancetype)shareInstance
{
    static BTClientStateMaintenanceManager* g_clientStateManintenanceManager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_clientStateManintenanceManager = [[BTClientStateMaintenanceManager alloc] init];
    });
    return g_clientStateManintenanceManager;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        [self p_registerClientStateObserver];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveServerHeartBeat)
                                                     name:BTNotificationServerHeartBeat
                                                   object:nil];
    }
    return self;
}

-(void)dealloc
{
    BTLog(@"BTClientStateMaintenanceManager release");
    [[BTClientState shareInstance] removeObserver:self
                                       forKeyPath:kBTNetworkState];
    
    [[BTClientState shareInstance] removeObserver:self
                                       forKeyPath:kBTUserState];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:BTNotificationServerHeartBeat
                                                  object:nil];
}

#pragma mark KVO
-(void)observeValueForKeyPath:(NSString *)keyPath
                     ofObject:(id)object
                       change:(NSDictionary *)change
                      context:(void *)context
{
    BTClientState *clientState = [BTClientState shareInstance];
    
    // 网络状态变化
    if ([keyPath isEqualToString:kBTNetworkState])
    {
        if ([BTClientState shareInstance].networkState != NETWORK_DISCONNECT)
        {
           
            BOOL shouldRelogin = !_reloginTimer &&
                ![_reloginTimer isValid] &&
                clientState.userState != USER_ONLINE &&
                clientState.userState != USER_KICKED_OUT &&
                clientState.userState != USER_OFF_LINE_INITIATIVE;

            if (shouldRelogin)
            {
                NSLog(@"进入重连");
                _reloginTimer = [NSTimer scheduledTimerWithTimeInterval:reloginTimeinterval
                                                                 target:self
                                                               selector:@selector(p_onReloginTimer:)
                                                               userInfo:nil
                                                                repeats:YES];
                _reloginInterval = 0;
                [_reloginTimer fire];
            }
        }
        else
        {
            clientState.userState = USER_OFF_LINE;
            [BTRecentUsersViewController shareInstance].title = @"连接失败";
        }
    }
    // 用户状态变化
    else if ([keyPath isEqualToString:kBTUserState])
    {
        switch ([BTClientState shareInstance].userState)
        {
            case USER_KICKED_OUT:
                [BTRecentUsersViewController shareInstance].title = @"未连接";
                // 停止检测服务端心跳
                [self p_stopCheckServerHeartBeat];
                // 停止向服务端发送心跳
                [self p_stopHeartBeat];
                break;
            case USER_OFF_LINE:
                [BTRecentUsersViewController shareInstance].title = @"未连接";
                [self p_stopCheckServerHeartBeat];
                [self p_stopHeartBeat];
                [self p_startRelogin];
                break;
            case USER_OFF_LINE_INITIATIVE:
                [BTRecentUsersViewController shareInstance].title = @"未连接";
                [self p_stopCheckServerHeartBeat];
                [self p_stopHeartBeat];
                break;
            case USER_ONLINE:
                [BTRecentUsersViewController shareInstance].title = @"TeamTalk";
                [self p_startCheckServerHeartBeat];
                [self p_startHeartBeat];
                break;
            case USER_LOGINING:
                [BTRecentUsersViewController shareInstance].title = @"收取中";
                break;
        }
    }
}

#pragma mark private API

// 注册KVO
-(void)p_registerClientStateObserver
{
    // 网络状态
    [[BTClientState shareInstance] addObserver:self
                                    forKeyPath:kBTNetworkState
                                       options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld
                                       context:nil];
    
    // 用户状态
    [[BTClientState shareInstance] addObserver:self
                                    forKeyPath:kBTUserState
                                       options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld
                                       context:nil];
}

// 开启发送心跳的Timer
-(void)p_startHeartBeat
{
    BTLog(@"begin heart beat");
    if (!_sendHeartTimer && ![_sendHeartTimer isValid])
    {
        _sendHeartTimer = [NSTimer scheduledTimerWithTimeInterval: heartBeatTimeinterval
                                                           target: self
                                                         selector: @selector(p_onSendHeartBeatTimer:)
                                                         userInfo: nil
                                                          repeats: YES];
    }
}

// 关闭发送心跳的Timer
-(void)p_stopHeartBeat
{
    if (_sendHeartTimer)
    {
        [_sendHeartTimer invalidate];
        _sendHeartTimer = nil;
    }
}

// 开启检验服务器端心跳的Timer
-(void)p_startCheckServerHeartBeat
{
    // delete by kuaidao 20141022, In order to save mobile power, remove server heart beat
    if (!_serverHeartBeatTimer)
    {
        BTLog(@"begin maintenance _serverHeartBeatTimer");
        _serverHeartBeatTimer = [NSTimer scheduledTimerWithTimeInterval:serverHeartBeatTimeinterval
                                                                 target:self
                                                               selector:@selector(p_onCheckServerHeartTimer:)
                                                               userInfo:nil
                                                                repeats:YES];
        [_serverHeartBeatTimer fire];
    }
}

// 停止检验服务器端心跳的Timer
-(void)p_stopCheckServerHeartBeat
{
    if (_serverHeartBeatTimer)
    {
        [_serverHeartBeatTimer invalidate];
        _serverHeartBeatTimer = nil;
    }
}

// 开启重连Timer
-(void)p_startRelogin
{
    if (!_reloginTimer)
    {

        _reloginTimer = [NSTimer scheduledTimerWithTimeInterval:reloginTimeinterval
                                                         target:self
                                                       selector:@selector(p_onReloginTimer:)
                                                       userInfo:nil repeats:YES];
        [_reloginTimer fire];
    }
}

// 运行在发送心跳的Timer上（即心跳请求）
-(void)p_onSendHeartBeatTimer:(NSTimer*)timer
{
    BTLog(@"*********ping*********");
    BTHeartbeatAPI* heartBeatAPI = [[BTHeartbeatAPI alloc] init];
    [heartBeatAPI requestWithObject:nil completion:nil];
}

// 收到服务器端的数据包
-(void)n_receiveServerHeartBeat
{
    _receiveServerHeart = YES;
}

// 运行在检验服务器端心跳的Timer上
// 如果在下次检查时，标志没有被设置为true，就会认为长时间没有收到服务端数据包
// 每收到一次服务端心跳标志就会被置为true，没检查一次标志就会被设置为false
-(void)p_onCheckServerHeartTimer:(NSTimer*)timer
{
    if (_receiveServerHeart)
    {
        _receiveServerHeart = NO;
    }
    else
    {
        [_serverHeartBeatTimer invalidate];
        _serverHeartBeatTimer = nil;
        // 太久没收到服务器端数据包了
        BTLog(@"太久没收到服务器端数据包了~");
        [BTClientState shareInstance].userState = USER_OFF_LINE;
    }
}

// 运行在断线重连的Timer上（重连动作）
-(void)p_onReloginTimer:(NSTimer*)timer
{
    static NSUInteger time = 0;
    static NSUInteger powN = 0;
    time++;
    
    // 检测出断线之后不会立即重连
    // 只有检测到断线 _reloginInterval 次之后才会重连
    // _reloginInterval 非固定值，指数增加
    if (time >= _reloginInterval)
    {
        [[BTLoginModule instance] reloginSuccess:^{
            [_reloginTimer invalidate];
            _reloginTimer = nil;
            time = 0;
            _reloginInterval = 0;
            powN = 0;
            [BTRecentUsersViewController shareInstance].title = @"TeamTalk";
            [BTNotificationHelper postNotification:BTNotificationUserReloginSuccess userInfo:nil object:nil];
            } failure:^(NSString *error) {
                BTLog(@"relogin failure:%@", error);
                if ([error isEqualToString:@"未登录"])
                {
                    [_reloginTimer invalidate];
                    _reloginTimer = nil;
                    time = 0;
                    _reloginInterval = 0;
                    powN = 0;
                    [BTRecentUsersViewController shareInstance].title = @"TeamTalk";
                }
                else
                {
                    // [NetwrokStatusNotifyUI showErrorWithStatus:@"重新连接失败"];
                    [BTRecentUsersViewController shareInstance].title = @"未连接";
                    powN++;
                    time = 0;
                    _reloginInterval = pow(2, powN);
                }
        }];
    }
}

@end
