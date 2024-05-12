//
//  BTMessageSendManager.h
//

#import <Foundation/Foundation.h>
#import "BTMessageEntity.h"


@class BTSessionEntity;

typedef void(^BTSendMessageCompletion)(BTMessageEntity *message, NSError *error);

typedef NS_ENUM(NSUInteger, MessageType)
{
    STRING,
    STRING_WITH_IMAGE
};

@class BTMessageEntity;


@interface BTMessageSendManager : NSObject
@property(nonatomic, readonly)dispatch_queue_t sendMessageQueue;
@property(nonatomic, readonly)NSMutableArray *messagesWaitToBeSend;

+(instancetype)instance;

-(void)sendMessage:(BTMessageEntity *)message
           isGroup:(BOOL)isGroup
           session:(BTSessionEntity *)session
        completion:(BTSendMessageCompletion)completion
             error:(void(^)(NSError *error))errorBlock;


-(void)sendVoiceMessage:(NSData *)voice
               filePath:(NSString *)filePath
              sessionId:(NSString *)sessionId
                isGroup:(BOOL)isGroup
                message:(BTMessageEntity *)msg
                session:(BTSessionEntity *)session
             completion:(BTSendMessageCompletion)completion;

@end
