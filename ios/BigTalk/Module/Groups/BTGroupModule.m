//
//  BTGroupModule.m
//

#import "BTGroupModule.h"
#import "BTRuntimeStatus.h"
#import "BTGetGroupInfoAPI.h"
#import "BTReceiveGroupAddMemberAPI.h"
#import "BTDatabaseUtil.h"
#import "BTGroupAvatarImage.h"
#import "BTNotificationHelper.h"
#import "NSDictionary+BTSafe.h"


@implementation BTGroupModule

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.allGroups = [NSMutableDictionary new];
        [[BTDatabaseUtil instance] loadAllGroupsCompletion:^(NSArray *contacts, NSError *error) {
            [contacts enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                BTGroupEntity *group = (BTGroupEntity *)obj;
                if (group.objId)
                {
                    [self addGroup:group];
                    BTGetGroupInfoAPI *request = [[BTGetGroupInfoAPI alloc] init];
                    // 获取群信息时要带上群信息版本
                    [request requestWithObject:@[@([BTRuntime convertLocalIdToPbId:group.objId]), @(group.objectVersion)]
                                    completion:^(id response, NSError* error) {
                        
                        if (!error)
                        {
                            if ([response count])
                            {
                                BTGroupEntity *group = (BTGroupEntity *)response[0];
                                if (group)
                                {
                                    [self addGroup:group];
                                    [[BTDatabaseUtil instance] updateGroup:group completion:^(NSError *error) {
                                        BTLog(@"insert group into database error");
                                    }];
                                }
                            }
                        }
                    }];
                }
            }];
        }];
        [self registerAPI];
    }
    return self;
}

+(instancetype)instance
{
    static BTGroupModule *group;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        group = [[BTGroupModule alloc] init];
    });
    return group;
}

-(void)getGroupFromDb
{
    
}

-(void)addGroup:(BTGroupEntity *)newGroup
{
    if (!newGroup)
    {
        return;
    }
    BTGroupEntity *group = newGroup;
    [_allGroups setObject:group forKey:group.objId];
    newGroup = nil;
}

-(NSArray *)getAllGroups
{
    return [_allGroups allValues];
}

// memory serach
-(BTGroupEntity *)getGroupByGroupId:(NSString *)groupId
{
    BTGroupEntity *entity= [_allGroups safeObjectForKey:groupId];
    return entity;
}

// remote search
-(void)getGroupInfoByGroupId:(NSString *)groupId completion:(GetGroupInfoCompletion)completion
{
    BTGroupEntity *group = [self getGroupByGroupId:groupId];
    if (group)
    {
        completion(group);
    }
    else
    {
        BTGetGroupInfoAPI *request = [[BTGetGroupInfoAPI alloc] init];
        [request requestWithObject:@[@([BTRuntime convertLocalIdToPbId:groupId]), @(group.objectVersion)]
                        completion:^(id response, NSError *error) {
            
            if (!error)
            {
                if ([response count])
                {
                    BTGroupEntity *group = (BTGroupEntity *)response[0];
                    if (group)
                    {
                        [self addGroup:group];
                        [[BTDatabaseUtil instance] updateGroup:group completion:^(NSError *error) {
                            BTLog(@"insert group to database error.");
                        }];
                    }
                    completion(group);
                }
            }
        }];
    }
}

-(BOOL)isContainGroup:(NSString *)gId
{
    return ([_allGroups valueForKey:gId] != nil);
}

-(void)registerAPI
{
    BTReceiveGroupAddMemberAPI *addmemberAPI = [[BTReceiveGroupAddMemberAPI alloc] init];
    [addmemberAPI registerAPIInAPIScheduleReceiveData:^(id object, NSError *error) {
        if (!error)
        {
            BTGroupEntity *groupEntity = (BTGroupEntity *)object;
            if (!groupEntity)
            {
                return;
            }
            if ([self getGroupByGroupId:groupEntity.objId])
            {
                // 成员本身就在组中
            }
            else
            {
                // 自己被添加进组中
                groupEntity.lastUpdateTime = [[NSDate date] timeIntervalSince1970];
                [[BTGroupModule instance] addGroup:groupEntity];
                [[NSNotificationCenter defaultCenter] postNotificationName:BTNotificationRecentContactsUpdate object:nil];
            }
        }
        else
        {
            BTLog(@"error:%@",[error domain]);
        }
    }];
    
//    DDReceiveGroupDeleteMemberAPI* deleteMemberAPI = [[DDReceiveGroupDeleteMemberAPI alloc] init];
//    [deleteMemberAPI registerAPIInAPIScheduleReceiveData:^(id object, NSError *error) {
//        if (!error)
//        {
//            GroupEntity* groupEntity = (BTGroupEntity *)object;
//            if (!groupEntity)
//            {
//                return;
//            }
//            DDUserlistModule* userModule = getDDUserlistModule();
//            if ([groupEntity.groupUserIds containsObject:userModule.myUserId])
//            {
//                //别人被踢了
//                [[DDMainWindowController instance] updateCurrentChattingViewController];
//            }
//            else
//            {
//                //自己被踢了
//                [self.recentlyGroupIds removeObject:groupEntity.groupId];
//                DDSessionModule* sessionModule = getDDSessionModule();
//                [sessionModule.recentlySessionIds removeObject:groupEntity.groupId];
//                DDMessageModule* messageModule = getDDMessageModule();
//                [messageModule popArrayMessage:groupEntity.groupId];
//                [NotificationHelp postNotification:notificationReloadTheRecentContacts userInfo:nil object:nil];
//            }
//        }
//    }];
}


@end
