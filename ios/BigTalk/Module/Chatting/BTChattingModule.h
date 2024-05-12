//
//  BTChattingModule.h
//

#import <Foundation/Foundation.h>
#import "BTSessionEntity.h"
#import "BTUserEntity.h"


#define BT_PAGE_ITEM_COUNT                  20
typedef void(^BTReuestServiceCompletion)(BTUserEntity *user);
@class BTMessageEntity;
typedef void(^BTChatLoadMoreHistoryCompletion)(NSUInteger addCount, NSError *error);


@interface BTChattingModule : NSObject
@property(strong, nonatomic)BTSessionEntity *sessionEntity;
@property(strong)NSMutableArray *ids ;
@property(strong)NSMutableArray *showingMessages;
@property(assign)NSInteger isGroup;
-(void)loadMoreHistoryCompletion:(BTChatLoadMoreHistoryCompletion)completion;
-(float)messageHeight:(BTMessageEntity *)message;
-(void)addShowMessage:(BTMessageEntity *)message;
-(void)addShowMessages:(NSArray *)messages;
-(void)updateSessionUpdateTime:(NSUInteger)time;
-(void)getCurrentUser:(void(^)(BTUserEntity *))completion;
-(void)loadHistoryMessageFromServer:(NSUInteger)fromMsgId loadCount:(NSUInteger)count completion:(BTChatLoadMoreHistoryCompletion)completion;
-(void)loadHostoryMessageFromServer:(NSUInteger)FromMsgId completion:(BTChatLoadMoreHistoryCompletion)completion;
+(NSArray *)p_spliteMessage:(BTMessageEntity *)message;
-(void)getNewMsg:(BTChatLoadMoreHistoryCompletion)completion;
@end


@interface BTPromptEntity : NSObject
@property(nonatomic, retain)NSString *message;
@end
