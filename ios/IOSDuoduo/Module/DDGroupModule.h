//
//  DDGroupModule.h
//  IOSDuoduo
//
//  Created by Michael Scofield on 2014-08-11.
//  Copyright (c) 2014 dujia. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GroupEntity.h"


typedef void(^GetGroupInfoCompletion)(GroupEntity* group);


@interface DDGroupModule : NSObject

@property(assign) NSInteger recentGroupCount;
@property(strong) NSMutableDictionary* allGroups;         // 所有群列表, key:group id，value:GroupEntity
@property(strong) NSMutableDictionary* allFixedGroup;     // 所有固定群列表

+(instancetype)instance;
-(GroupEntity*)getGroupByGId:(NSString*)gId;
-(void)addGroup:(GroupEntity*)newGroup;
-(void)getGroupInfoByGroupID:(NSString*)groupID completion:(GetGroupInfoCompletion)completion;
-(NSArray*)getAllGroups;

@end
