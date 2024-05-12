//
//  BTClientState.m
//

#import "BTClientState.h"
#import "BTReachability.h"


@interface BTClientState(PrivateAPI)
-(void)n_receiveReachabilityChangedNotification:(NSNotification *)notification;
@end


@implementation BTClientState
+(instancetype)shareInstance
{
    static BTClientState *g_clientState;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_clientState = [[BTClientState alloc] init];
    });
    return g_clientState;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _userState = USER_OFF_LINE;
        _socketState = SOCKET_LINK_DISCONNECT;
        _reachability = [BTReachability reachabilityForInternetConnection];
        [_reachability startNotifier];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveReachabilityChangedNotification:)
                                                     name:kBTReachabilityChangedNotification
                                                   object:nil];
    }
    return self;
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(void)setUseStateWithoutObserver:(BTUserState)userState
{
    _userState = userState;
}

#pragma mark - privateAPI
-(void)n_receiveReachabilityChangedNotification:(NSNotification *)notification
{
    BTReachability *reach = [notification object];
    NetworkStatus netWorkStatus = [reach currentReachabilityStatus];
    switch (netWorkStatus)
    {
        case NotReachable:
            [self setValue:@(NETWORK_DISCONNECT) forKeyPath:kBTNetworkState];
            break;
        case ReachableViaWiFi:
            [self setValue:@(NETWORK_WIFI) forKeyPath:kBTNetworkState];
            break;
        case ReachableViaWWAN:
            [self setValue:@(NETWORK_3G) forKeyPath:kBTNetworkState];
            break;
    }
}
@end
