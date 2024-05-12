//
//  BTLoginModule.m
//

#import "BTLoginModule.h"
#import "BTLoginAPI.h"
#import "BTMsgServerModule.h"
#import "BTTcpServerModule.h"
#import "BTConsts.h"
#import "BTSpellLibrary.h"
#import "BTUserModule.h"
#import "BTUserEntity.h"
#import "BTClientState.h"
#import "BTRuntimeStatus.h"
#import "BTContactsModule.h"
#import "BTDatabaseUtil.h"
#import "BTAllUserAPI.h"
#import "BTLoginAPI.h"
#import "BTDbManager.h"
#import "BTConsts.h"


@implementation BTLoginModule
{
    NSString *_lastLoginUser;
    NSString *_lastLoginPassword;
    NSString *_lastLoginUserName;
    BOOL _relogining;
}

+(instancetype)instance
{
    static BTLoginModule *g_LoginManager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_LoginManager = [[BTLoginModule alloc] init];
    });
    return g_LoginManager;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _loginAPI = [[BTLoginAPI alloc] init];
        _tcpServerModule = [[BTTcpServerModule alloc] init];
        _msgServerModule = [[BTMsgServerModule alloc] init];
        _relogining = NO;
    }
    return self;
}


#pragma mark Public API
-(void)loginWithUsername:(NSString *)name
                password:(NSString *)password
                 success:(void(^)(BTUserEntity *loginedUser))success
                 failure:(void(^)(NSString *error))failure
{
    [_tcpServerModule connectTcpServerWithIp:MSG_SERVER_IP port:MSG_SERVER_PORT success:^{
        [_msgServerModule checkUserId:name pwd:password token:@"" success:^(id object) {
            [[NSUserDefaults standardUserDefaults] setObject:password forKey:@"password"];
            [[NSUserDefaults standardUserDefaults] setObject:name forKey:@"username"];
            [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"autoLogin"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            
            _lastLoginPassword = password;
            _lastLoginUserName = name;
            BTClientState *clientState = [BTClientState shareInstance];
            clientState.userState = USER_ONLINE;
            _relogining = YES;
            BTUserEntity *user = object[@"user"];
            BTRuntime.user = user;
            [[BTDatabaseUtil instance] openCurrentUserDb];
            // why
            [self p_loadAllUsersCompletion:^{
                
            }];
            success(user);
            [BTNotificationHelper postNotification:BTNotificationUserLoginSuccess
                                          userInfo:nil
                                            object:user];
        } failure:^(id object) {
            NSString *error = [NSString stringWithFormat:@"login failed: %@", object];
            BTLog(@"%@", error);
            failure(error);
        }];
    } failure:^{
        NSString *error = @"connect to msg_server failed";
        BTLog(@"%@", error);
        failure(error);
    }];
}

-(void)reloginSuccess:(void(^)())success
              failure:(void(^)(NSString *error))failure
{
    BTLog(@"relogin success");
    if (
        [BTClientState shareInstance].userState == USER_OFF_LINE
        &&
        _lastLoginPassword
        &&
        _lastLoginUserName
    )
    {
        BTLog(@"logining to tcp_server and msg_server..");
        [self loginWithUsername:_lastLoginUserName
                       password:_lastLoginPassword
                        success:^(BTUserEntity* user) {
            
            [[NSNotificationCenter defaultCenter] postNotificationName:BTNotificationUserReloginSuccess object:nil];
            success(YES);
        } failure:^(NSString *error) {
            failure(@"logining to tcp_server and msg_server failed");
        }];
    }
}

-(void)offlineCompletion:(void(^)())completion
{
    // TODO:
    completion();
}

-(void)p_loadAllUsersCompletion:(void(^)())completion
{
    __block NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    __block NSInteger version = [[defaults objectForKey:@"usersLatestUpdateTime"] integerValue];
        
    
    [[BTDatabaseUtil instance] getAllContacts:^(NSArray *contacts, NSError *error) {
        if ([contacts count] != 0)
        {
            BTLog(@"find %d contacts in local db", (unsigned int)[contacts count]);
            [contacts enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
                [[BTUserModule shareInstance] addMaintanceUser:obj];
            }];
        }
        else
        {
            BTLog(@"there is no contacts in local db");
            version = 0;
            BTAllUserAPI *api = [[BTAllUserAPI alloc] init];
            [api requestWithObject:@[@(version)] completion:^(id response, NSError *error) {
                if (!error)
                {
                    // TODO: rename 'alllastupdatetime' to 'usersLatestUpdateTime'
                    NSUInteger responseVersion = [response[@"alllastupdatetime"] integerValue];
                    if (responseVersion == version && responseVersion != 0)
                    {
                        BTLog(@"no user update, latest user update time: %lu", responseVersion);
                        return;
                    }
                    [defaults setObject:@(responseVersion) forKey:@"usersLatestUpdateTime"];
                    // TODO: rename 'userlist'
                    NSMutableArray *array = response[@"userlist"];
                    [[BTDatabaseUtil instance] insertContacts:array completion:^(NSError *error) {
                        
                    }];
                    
                    /**
                     * The code provided is the beginning of a Grand Central Dispatch (GCD) asynchronous task in Objective-C. It's used for
                     * concurrent or background execution of code. Here's what each part of the code does:
                     *
                     * dispatch_async: This is a GCD function used to execute a block of code asynchronously on a specified dispatch queue. It
                     * takes two arguments: the dispatch queue and the block of code to execute.
                     *
                     * dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0): This part of the code gets a reference to a global
                     * dispatch queue with a specified priority. In this case, DISPATCH_QUEUE_PRIORITY_DEFAULT indicates a default priority
                     * global queue, which is suitable for general-purpose tasks that are not particularly high or low priority. The 0 argument is a
                     * reserved value and should always be set to zero.
                    */
                    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                        [array enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
                            [[BTUserModule shareInstance] addMaintanceUser:obj];
                        }];
                    });
                }
                else
                {
                    BTLog(@"request all users error");
                }
            }];
        }
    }];
    
    // for possible update
    BTAllUserAPI *api = [[BTAllUserAPI alloc] init];
    [api requestWithObject:@[@(version)] completion:^(id response, NSError *error) {
        if (!error)
        {
            // TODO: rename
            NSUInteger responseVersion = [response[@"alllastupdatetime"] integerValue];
            if (responseVersion == version && responseVersion != 0)
            {
                return ;
            }
            [defaults setObject:@(responseVersion) forKey:@"usersLatestUpdateTime"];
            // TODO: rename
            NSMutableArray *array = response[@"userlist"];
            [[BTDatabaseUtil instance] insertContacts:array completion:^(NSError *error) {
                BTLog(@"update all contacts to db completion");
            }];
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [array enumerateObjectsUsingBlock:^(BTUserEntity *obj, NSUInteger idx, BOOL *stop) {
                    [[BTUserModule shareInstance] addMaintanceUser:obj];
                }];
            });
        }
    }];
}

@end
