//
//  BTUserModule.m
//

#import "BTUserModule.h"
#import "BTDatabaseUtil.h"


@interface BTUserModule(PrivateAPI)
-(void)n_receiveUserLogoutNotification:(NSNotification *)notification;
-(void)n_receiveUserLoginNotification:(NSNotification *)notification;
@end


@implementation BTUserModule
{
    NSMutableDictionary *_allUsers;
}

+(instancetype)shareInstance
{
    static BTUserModule *g_userModule;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_userModule = [[BTUserModule alloc] init];
    });
    return g_userModule;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _allUsers = [[NSMutableDictionary alloc] init];
        _recentUsers = [[NSMutableDictionary alloc] init];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveUserLoginNotification:)
                                                     name:BTNotificationUserLoginSuccess
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveUserLoginNotification:)
                                                     name:BTNotificationUserReloginSuccess
                                                   object:nil];
    }
    return self;
}

-(void)addMaintanceUser:(BTUserEntity *)user
{
    if (!user)
    {
        return;
    }
    if (!_allUsers)
    {
        _allUsers = [[NSMutableDictionary alloc] init];
    }
    [_allUsers setValue:user forKey:user.objId];
}

-(NSArray *)getAllMaintanceUser
{
    return [_allUsers allValues];
}

-(void)getUserByUserId:(NSString *)userId completion:(void(^)(BTUserEntity *user))completion
{
    return completion(_allUsers[userId]);
}

-(void)addRecentUser:(BTUserEntity *)user
{
    if (!user)
    {
        return;
    }
    if (!self.recentUsers)
    {
        self.recentUsers = [[NSMutableDictionary alloc] init];
    }
    NSArray *allKeys = [self.recentUsers allKeys];
    if (![allKeys containsObject:user.objId])
    {
        [self.recentUsers setValue:user forKey:user.objId];
        [[BTDatabaseUtil instance] insertContacts:@[user] completion:^(NSError *error) {

        }];
    }
}

-(void)loadAllRecentUsers:(LoadRecentUsersCompletion)completion
{

}


#pragma mark PrivateAPI
-(void)n_receiveUserLogoutNotification:(NSNotification *)notification
{
    _recentUsers = nil;
}

-(void)n_receiveUserLoginNotification:(NSNotification *)notification
{
    if (!_recentUsers)
    {
        _recentUsers = [[NSMutableDictionary alloc] init];
        [self loadAllRecentUsers:^{
            [BTNotificationHelper postNotification:BTNotificationRecentContactsUpdate userInfo:nil object:nil];
        }];
    }
}

-(void)clearRecentUser
{
    BTUserModule *userModule = [BTUserModule shareInstance];
    [[userModule recentUsers] removeAllObjects];
}

@end
