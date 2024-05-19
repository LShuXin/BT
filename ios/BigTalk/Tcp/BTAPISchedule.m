//
//  BTAPISchedule.m
//

#import "BTAPISchedule.h"
#import "BTSuperAPI.h"
#import "BTUnrequestSuperAPI.h"

#define MAP_REQUEST_KEY(api)                                [NSString stringWithFormat:@"request_%i-%i-%i", [api requestServiceID], [api requestCommandID], [(BTSuperAPI *)api seqNo]]
#define MAP_RESPONSE_KEY(api)                               [NSString stringWithFormat:@"response_%i-%i-%i", [api responseServiceID], [api responseCommandID], [(BTSuperAPI *)api seqNo]]
#define MAP_RESPONSE_DATA_KEY(serverDataHeader)             [NSString stringWithFormat:@"response_%i-%i-%i", serverDataHeader.serviceID, serverDataHeader.commandID, serverDataHeader.seqNo]

#define MAP_SERVER_PUSH_KEY(api)                            [NSString stringWithFormat:@"server_push_%i-%i", [api responseServiceID], [api responseCommandID]]
#define MAP_SERVER_PUSH_DATA_KEY(serverDataHeader)          [NSString stringWithFormat:@"server_push_%i-%i", serverDataHeader.serviceID, serverDataHeader.commandID]


typedef NS_ENUM(NSInteger, BTAPIErrorCode)
{
    TIMEOUT = 1001,
    RESULT  = 1002
};


static NSInteger const timeInterval = 1;


@interface BTAPISchedule(PrivateAPI)
// 当请求返回时从请求 map 中查找该 api 并调用该 api 的 completion block
// 需要同时从请求 map、超时 map 中移除该 api
-(void)p_requestCompletion:(id<BTAPIScheduleProtocol>)api;
// 当请求超时时从超时 map 中查找该 api 并调用该 api 的 completion block
// 需要同时从请求 map 中移除该api
-(void)p_timeoutOnTimer:(id)timer;
@end


@implementation BTAPISchedule
{
    
    // 发出的 api 请求
    NSMutableDictionary *_apiRequestMap;
    // 发出的 api 请求对应的响应
    NSMutableDictionary *_apiResponseMap;
    // 服务端主动发送的消息
    NSMutableDictionary *_unrequestMap;
    // 需要控制超时的 api
    // NSMutableDictionary *_timeoutMap;
    
    // 用于轮训检测超时的计时器
    // NSTimer *_timeOutTimer;
}

+(instancetype)instance
{
    static BTAPISchedule *g_apiSchedule;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_apiSchedule = [[BTAPISchedule alloc] init];
    });
    return g_apiSchedule;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _apiRequestMap = [[NSMutableDictionary alloc] init];
        _apiResponseMap = [[NSMutableDictionary alloc] init];
        _unrequestMap = [[NSMutableDictionary alloc] init];
        // _timeoutMap = [[NSMutableDictionary alloc] init];
        _apiScheduleQueue = dispatch_queue_create("com.lsx.bigtalk.apiSchedule", NULL);
    }
    return self;
}


#pragma mark public
-(BOOL)registerApi:(id<BTAPIScheduleProtocol>)api
{
    __block BOOL registSuccess = NO;
    dispatch_sync(self.apiScheduleQueue, ^{
        // 只发送数据，不关注返回的 api
        if (![api analysisReturnData])
        {
            registSuccess = YES;
        }
        
        if (![[_apiRequestMap allKeys] containsObject:MAP_REQUEST_KEY(api)])
        {
            // 在请求表中标记对应的 api
            [_apiRequestMap setObject:api forKey:MAP_REQUEST_KEY(api)];
            registSuccess = YES;
        }
        else
        {
            // 该请求已经注册过了
            registSuccess = NO;
        }
        
        // 在响应表中注册对应的api
        if (![[_apiResponseMap allKeys] containsObject:MAP_RESPONSE_KEY(api)])
        {
            [_apiResponseMap setObject:api forKey:MAP_RESPONSE_KEY(api)];
        }
    });
    return registSuccess;
}

-(void)registerTimeoutApi:(id<BTAPIScheduleProtocol>)api
{
    double delayInSeconds = [api requestTimeOutTimeInterval];
    if (delayInSeconds == 0)
    {
        return;
    }

    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void) {
        // 如果请求表中还有该 api 时说明已经超时
        if ([[_apiRequestMap allKeys] containsObject:MAP_REQUEST_KEY(api)])
        {
            [[BTSundriesCenter instance] pushTaskToSerialQueue:^{
                RequestCompletion completion = [(BTSuperAPI *)api completion];
                NSError *error = [NSError errorWithDomain:@"请求超时" code:TIMEOUT userInfo:nil];
                dispatch_sync(dispatch_get_main_queue(), ^{
                    completion(nil, error);
                });
                
                // 从请求 map、响应 map 中删除该 api
                [self p_requestCompletion:api];
            }];
        }
    });
}

// 属于主动接收服务端通知的一类接口
-(BOOL)registerUnrequestAPI:(id<BTAPIUnrequestScheduleProtocol>)api
{
    __block BOOL registerSuccess = NO;
    dispatch_sync(self.apiScheduleQueue, ^{
        NSString *key = MAP_SERVER_PUSH_KEY(api);
        if ([[_unrequestMap allKeys] containsObject:key])
        {
            // 已经注册过了
            registerSuccess = NO;
        }
        else
        {
            [_unrequestMap setObject:api forKey:key];
            registerSuccess = YES;
        }
    });
    return registerSuccess;
}

-(void)receiveServerData:(NSData *)data forDataType:(BTServerDataHeader)serverDataHeader
{
    dispatch_async(self.apiScheduleQueue, ^{
        NSString *key = MAP_RESPONSE_DATA_KEY(serverDataHeader);
        // 从响应表中查找对应的 api，解包后调用其 completion
        id<BTAPIScheduleProtocol> api = _apiResponseMap[key];
        
        if (api)
        {
            // 说明这些数据是用于应答某个客户端的请求
            RequestCompletion completion = [(BTSuperAPI *)api completion];
            Analysis analysis = [api analysisReturnData];
            id response = analysis(data);
            // 从请求表和响应表中移除该 api
            [self p_requestCompletion:api];
            dispatch_async(dispatch_get_main_queue(), ^{
                @try
                {
                    completion(response, nil);
                }
                @catch (NSException *exception)
                {
                    BTLog(@"receive data error: %@", exception.userInfo.description);
                }
            });
        }
        else
        {
            // 说明这些数据是服务端主动推送的
            // 这种对服务端主动推送消息的注册不需要清理
            NSString *unrequestKey = MAP_SERVER_PUSH_DATA_KEY(serverDataHeader);
            id<BTAPIUnrequestScheduleProtocol> unrequestAPI = _unrequestMap[unrequestKey];
            if (unrequestAPI)
            {
                UnrequestAPIAnalysis unrequestAnalysis = [unrequestAPI unrequestAnalysis];
                id object = unrequestAnalysis(data);
                ReceiveData received = [(BTUnrequestSuperAPI *)unrequestAPI receivedData];
                dispatch_async(dispatch_get_main_queue(), ^{
                    received(object, nil);
                });
            }
        }
    });
}

-(void)sendData:(NSMutableData *)data
{
    dispatch_async(self.apiScheduleQueue, ^{
        [[BTTcpClientManager instance] writeToSocket:data];
    });
}


#pragma mark - privateAPI
// 移除请求 api、响应 api
-(void)p_requestCompletion:(id<BTAPIScheduleProtocol>)api
{
    [_apiRequestMap removeObjectForKey:MAP_REQUEST_KEY(api)];
    
    [_apiResponseMap removeObjectForKey:MAP_RESPONSE_KEY(api)];
}

// 以遍历超时 api 注册表的方法来检测 api 调用是否超时
// 当发现超时调用 api 的 completion block
// 该方案暂时未使用, 目前采用的方案是通过计时器直接注册超时回调
// TODO: 调用 completion block 后需要做必要的 api 清理
//-(void)p_timeoutOnTimer:(id)timer
//{
//    NSDate *date = [NSDate date];
//    NSInteger count = [_timeoutMap count];
//    if (count == 0)
//    {
//        return;
//    }
//    NSArray *allKeys = [_timeoutMap allKeys];
//    for (int index = 0; index < count; index++)
//    {
//        NSDate *key = allKeys[index];
//        id<BTAPIScheduleProtocol> api = (id<BTAPIScheduleProtocol>)[_timeoutMap objectForKey:key];
//        NSDate *beginDate = (NSDate *)key;
//        NSInteger gap = [date timeIntervalSinceDate:beginDate];
//        NSInteger apitimeval = [api requestTimeOutTimeInterval];
//        if (gap > apitimeval)
//        {
//            if ([[_apiRequestMap allKeys] containsObject:MAP_REQUEST_KEY(api)])
//            {
//                RequestCompletion completion = [(BTSuperAPI *)api completion];
//                NSError *error = [NSError errorWithDomain:@"请求超时" code:TIMEOUT userInfo:nil];
//                completion(nil, error);
//                // TODO: some cleanup work
//            }
//        }
//    }
//    
//    [_timeoutMap enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
//        
//    }];
//}

@end
