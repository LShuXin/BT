//
//  BTSessionModule.h
//

#import <Foundation/Foundation.h>
#import "BTPublicDefine.h"


@class BTSessionEntity;


typedef enum
{
    ADD       = 0,
    REFRESH   = 1,
    DELETE    = 2
} BTSessionAction;


@protocol BTSessionModuelDelegate<NSObject>
@optional
-(void)sessionUpdate:(BTSessionEntity *)session action:(BTSessionAction)action;
@end


@interface BTSessionModule : NSObject
AS_SINGLETON(BTSessionModule)

@property(strong)id<BTSessionModuelDelegate> delegate;

-(NSArray *)getAllSessions;
-(void)addToSessionModel:(BTSessionEntity *)session;
-(void)addSessionsToSessionModel:(NSArray *)sessionArray;
-(BTSessionEntity *)getSessionById:(NSString *)sessionId;
-(void)getRecentSession:(void(^)(NSUInteger count))block;
-(void)removeSessionByServer:(BTSessionEntity *)session;
-(void)loadLocalSession:(void(^)(bool isok))block;
-(void)getHadUnreadMessageSession:(void(^)(NSUInteger count))block;
-(NSUInteger)getAllUnreadMessageCount;

@end
