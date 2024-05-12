#import <Foundation/Foundation.h>
#import "BTBaseEntity.h"


static NSString *const GROUP_PRE = @"group_";                    // group id 前缀

enum
{
    GROUP_TYPE_FIXED = 1,       // 固定群
    GROUP_TYPE_TEMPORARY,       // 临时群
};

@interface BTGroupEntity : BTBaseEntity

@property(copy)NSString *groupCreatorId;                         // 群创建者ID
@property(nonatomic,assign)int groupType;                        // 群类型
@property(nonatomic,strong)NSString *name;                       // 群名称
@property(nonatomic,strong)NSString *avatar;                     // 群头像
@property(nonatomic,strong)NSMutableArray *groupUserIds;         // 群用户id列表
@property(nonatomic,readonly)NSMutableArray *fixGroupUserIds;    // 固定的群用户id列表
@property(strong)NSString *lastMsg;
@property(assign)BOOL isShield;

-(void)copyContent:(BTGroupEntity*)entity;
+(UInt32)localGroupIdToPbGroupId:(NSString *)localId;
+(NSString *)pbGroupIdToLocalGroupId:(UInt32)pbId;
-(void)addFixOrderGroupUserIds:(NSString *)idString;
+(BTGroupEntity *)dicToGroupEntity:(NSDictionary *)dic;
+(NSString *)getSessionId:(NSString *)groupId;
+(BTGroupEntity *)initGroupEntityWithPbData:(GroupInfo *)groupInfo;

@end
