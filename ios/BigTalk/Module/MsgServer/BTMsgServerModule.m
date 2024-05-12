//
//  BTMsgServerModule.m
//

#import "BTMsgServerModule.h"
#import "BTTcpClientManager.h"
#import "BTLoginMsgServerAPI.h"


static int const timeOutTimeInterval = 10;

typedef void(^CheckSuccess)(id object);


@implementation BTMsgServerModule
{
    CheckSuccess _success;
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
    }
    return self;
}

-(void)checkUserId:(NSString *)userId
               pwd:(NSString *)password
             token:(NSString *)token
           success:(void(^)(id object))success
           failure:(void(^)(id object))failure
{
    
    if (!_connecting)
    {
        // TODO: enum
        NSNumber *clientType = @(17);
        NSString *clientVersion = [NSString stringWithFormat:@"MAC/%@-%@",
                                   [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"],
                                   [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"]];
        
        NSArray *parameter = @[userId, password, clientVersion, clientType];
        
        BTLoginMsgServerAPI *api = [[BTLoginMsgServerAPI alloc] init];
        BTLog(@"登录参数：%@", parameter);
        [api requestWithObject:parameter completion:^(id response, NSError *error) {
            if (!error)
            {
                if (response)
                {
                    NSString *resultString = response[@"resultString"];
                    BTLog(@"msg_server auth result: %@", resultString);
                    if (resultString == nil)
                    {
                         BTLog(@"login msg_server success");
                         success(response);
                    }
                }
                else
                {
                    failure(error);
                }
            }
            else
            {
                BTLog(@"error: %@", [error domain]);
                failure(error);
            }
        }];
    }
}

@end
