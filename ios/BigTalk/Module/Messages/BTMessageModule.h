//
//  BTMessageModule.h
//

#import <Foundation/Foundation.h>
#import "BTMessageEntity.h"
#import "BTSessionEntity.h"


typedef void(^GetLastestMessageCompletion)(BTMessageEntity *message);
typedef void(^GetUnreadMessageCountCompletion)(NSInteger count);


@interface BTMessageModule : NSObject
@property(assign)NSInteger unreadMsgCount;
+(instancetype)shareInstance;
+(NSUInteger)generateMessageId;
-(void)getLatestMessageBySessionId:(NSString *)sessionId completion:(GetLastestMessageCompletion)completion;
// TODO: what's for
-(void)removeFromUnreadMessageButNotSendRead:(NSString *)sessionId;
-(void)addUnreadMessage:(BTMessageEntity *)message;
-(void)clearUnreadMessagesBySessionId:(NSString *)sessionId;
-(NSUInteger)getUnreadMessgeCount;
-(NSArray *)getUnreadMessagesBySessionId:(NSString *)sessionId;
-(NSUInteger)getUnreadMessageCountBySessionId:(NSString *)sessionId;
-(NSArray *)popAllUnreadMessagesBySessionId:(NSString *)sessionId;
-(void)getUnreadMessageGroup:(void(^)(NSArray *array))completion;
-(void)getGetUnreadMessageUsers:(void(^)(NSArray *array))completion;
-(NSUInteger)getUnreadCountBySessionId:(NSString *)sessionId;
-(void)cleanMessageFromNotification:(NSUInteger)messageId sessionId:(NSString *)sessionId sessionType:(SessionType)sessionType;
-(void)sendMsgRead:(BTMessageEntity *)message;
-(void)getMessageFromServerFromMsgId:(NSInteger)fromMsgId
                             session:(BTSessionEntity *)session
                               count:(NSInteger)count
                          completion:(void(^)(NSMutableArray *array, NSError *error))completion;
@end
