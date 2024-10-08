//
//  BTTcpServerModule.m
//

#import "BTTcpServerModule.h"
#import "BTTcpClientManager.h"


static NSInteger timeoutInterval = 10;

@interface BTTcpServerModule(notification)
-(void)n_receiveTcpLinkConnectCompleteNotification:(NSNotification *)notification;
-(void)n_receiveTcpLinkConnectFailureNotification:(NSNotification *)notification;
@end


@implementation BTTcpServerModule
{
    ConnectTcpServerSuccess _success;
    Failure _failure;
    BOOL _connecting;
    NSUInteger _connectTimes;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _connecting = NO;
        _connectTimes = 0;
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveTcpLinkConnectCompleteNotification:)
                                                     name:BTNotificationTcpLinkConnectComplete
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveTcpLinkConnectFailureNotification:)
                                                     name:BTNotificationTcpLinkConnectFailure
                                                   object:nil];
    }
    
    return self;
}

-(void)connectTcpServerWithIp:(NSString *)ip
                         port:(NSInteger)port
                      success:(void(^)())success
                      failure:(void(^)())failure
{
    if (!_connecting)
    {
        _connectTimes++;
        _connecting = YES;
        _success = [success copy];
        _failure = [failure copy];

       
        [[BTTcpClientManager instance] disconnect];
        [[BTTcpClientManager instance] connect:ip port:port status:1];
        BTLog(@"connectting to tcp_server %@:%d ...", ip, (int)port);
        NSUInteger nowTimes = _connectTimes;
        double delayInSeconds = timeoutInterval;
        // 相当于 js 中的setTimeout
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void) {
            if (_connecting && nowTimes == _connectTimes)
            {
                 BTLog(@"tcp connect timeout");
                _connecting = NO;
                _failure(nil);
            }
        });
    }
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:BTNotificationTcpLinkConnectComplete object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:BTNotificationTcpLinkConnectFailure object:nil];
}


#pragma mark - notification
-(void)n_receiveTcpLinkConnectCompleteNotification:(NSNotification *)notification
{
    if (_connecting)
    {
        _connecting = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            _success();
        });
    }
}

-(void)n_receiveTcpLinkConnectFailureNotification:(NSNotification *)notification
{
    if (_connecting)
    {
        _connecting = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            _failure(nil);
        });
    }
}

@end
