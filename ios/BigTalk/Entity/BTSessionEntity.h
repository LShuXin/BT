//
//  BTSessionEntity.h
//

#import <Foundation/Foundation.h>
#import "IMBaseDefine.pbobjc.h"


@class BTUserEntity, BTGroupEntity;


@interface BTSessionEntity : NSObject

@property(nonatomic, retain)NSString *sessionId;
@property(nonatomic, assign)SessionType sessionType;
@property(nonatomic, readonly)NSString *name;
@property(assign)NSInteger unReadMsgCount;
@property(assign)NSUInteger timeInterval;
@property(nonatomic, strong, readonly)NSString *pbId;
@property(assign)BOOL isShield;
@property(strong)NSString *lastMsg;
@property(assign)NSInteger lastMsgId;
@property(strong)NSString *avatar;

-(NSArray *)sessionUsers;
-(id)initWithSessionId:(NSString *)sessionId sessionType:(SessionType)sessionType;
-(id)initWithSessionId:(NSString *)sessionId sessionName:(NSString *)sessionName sessionType:(SessionType)sessionType;
-(id)initByUser:(BTUserEntity *)userEntity;
-(id)initByGroup:(BTGroupEntity *)groupEntity;
-(void)updateUpdateTime:(NSUInteger)date;
-(NSString *)getSessionGroupId;
-(void)setSessionName:(NSString *)sessionName;
-(BOOL)isGroup;
-(id)dicToGroup:(NSDictionary *)dic;
-(void)setSessionUser:(NSArray *)array;

@end
