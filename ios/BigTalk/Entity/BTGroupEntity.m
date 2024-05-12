#import "BTGroupEntity.h"
#import "BTUserEntity.h"
#import "NSDictionary+BTSafe.h"
#import "BTDatabaseUtil.h"


@implementation BTGroupEntity

-(void)setGroupUserIds:(NSMutableArray *)groupUserIds
{
    if (_groupUserIds)
    {
        _groupUserIds = nil;
        _fixGroupUserIds = nil;
    }
    _groupUserIds = groupUserIds;
    [groupUserIds enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        [self addFixOrderGroupUserIds:obj];
    }];
}

-(void)copyContent:(BTGroupEntity *)entity
{
    self.groupType = entity.groupType;
    self.lastUpdateTime = entity.lastUpdateTime;
    self.name = entity.name;
    self.avatar = entity.avatar;
    self.groupUserIds = entity.groupUserIds;
}

+(NSString *)getSessionId:(NSString *)groupId
{
     return groupId;
}

+(NSString *)pbGroupIdToLocalGroupId:(UInt32)pbId
{
    return [NSString stringWithFormat:@"%@%ld", GROUP_PRE, (unsigned long)pbId];
}

+(UInt32)localGroupIdToPbGroupId:(NSString *)localId
{
    if (![localId hasPrefix:GROUP_PRE])
    {
        return 0;
    }
    return [[localId substringFromIndex:[GROUP_PRE length]] integerValue];
}

+(BTGroupEntity *)initGroupEntityWithPbData:(GroupInfo *)groupInfo
{
    BTGroupEntity *group = [BTGroupEntity new];
    group.objId = [self pbGroupIdToLocalGroupId:groupInfo.groupId];
    group.objectVersion = groupInfo.version;
    group.name = groupInfo.groupName;
    group.avatar = groupInfo.groupAvatar;
    group.groupCreatorId = [BTRuntime convertPbIdToLocalId:groupInfo.groupCreatorId
                                                   sessionType:SessionType_SessionTypeSingle];
    group.groupType = groupInfo.groupType;
    group.isShield = groupInfo.shieldStatus;
    NSMutableArray *ids = [NSMutableArray new];
    for (int i = 0; i < [groupInfo.groupMemberListArray count]; i++)
    {
        [ids addObject:[BTRuntime convertPbIdToLocalId:[[groupInfo groupMemberListArray] valueAtIndex:i]
                                           sessionType:SessionType_SessionTypeSingle]];
    }
    group.groupUserIds = ids;
    group.lastMsg = @"";
    return group;
}

-(void)addFixOrderGroupUserIds:(NSString *)idString
{
    if (!_fixGroupUserIds)
    {
        _fixGroupUserIds = [[NSMutableArray alloc] init];
    }
    [_fixGroupUserIds addObject:idString];
}

+(BTGroupEntity *)dicToGroupEntity:(NSDictionary *)dic
{
    BTGroupEntity *group = [BTGroupEntity new];
    group.groupCreatorId = [dic safeObjectForKey:@"creatorId"];
    group.objId = [dic safeObjectForKey:@"groupId"];
    group.avatar = [dic safeObjectForKey:@"avatar"];
    group.groupType = [[dic safeObjectForKey:@"groupType"] integerValue];
    group.name = [dic safeObjectForKey:@"name"];
    group.isShield = [[dic safeObjectForKey:@"isShield"] boolValue];
    NSString *string = [dic safeObjectForKey:@"users"];
    NSMutableArray *array = [NSMutableArray arrayWithArray:[string componentsSeparatedByString:@"-"]] ;
    if ([array count] > 0)
    {
        group.groupUserIds = [array copy];
    }
    group.lastMsg = [dic safeObjectForKey:@"lastMessage"];
    group.objectVersion = [[dic safeObjectForKey:@"version"] integerValue];
    group.lastUpdateTime = [[dic safeObjectForKey:@"lastUpdateTime"] longValue];
    return group;
}

-(BOOL)theVersionIsChanged
{
    return YES;
}

-(void)updateGroupInfo
{
    
}

@end
