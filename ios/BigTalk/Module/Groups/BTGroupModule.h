//
//  BTGroupModule.h
//

#import <Foundation/Foundation.h>
#import "BTGroupEntity.h"


typedef void(^GetGroupInfoCompletion)(BTGroupEntity *group);


@interface BTGroupModule : NSObject

@property(assign)NSInteger recentGroupCount;
@property(strong)NSMutableDictionary *allGroups;         // 所有群列表, key:group id，value:GroupEntity
@property(strong)NSMutableDictionary *allFixedGroup;     // 所有固定群列表

+(instancetype)instance;
-(BTGroupEntity *)getGroupByGroupId:(NSString *)groupId;
-(void)addGroup:(BTGroupEntity *)newGroup;
-(void)getGroupInfoByGroupId:(NSString *)groupId completion:(GetGroupInfoCompletion)completion;
-(NSArray *)getAllGroups;

@end
