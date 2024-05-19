
//
//  BTMessageSendManager.m
//

#import "BTMessageSendManager.h"
#import "BTUserModule.h"
#import "BTMessageEntity.h"
#import "BTMessageModule.h"
#import "BTTcpClientManager.h"
#import "BTSendMessageAPI.h"
#import "BTRuntimeStatus.h"
#import "BTRecentUsersViewController.h"
#import "BTEmotionsModule.h"
#import "NSDictionary+BTJSON.h"
#import "BTUnAckMessageManager.h"
#import "BTGroupModule.h"
#import "BTClientState.h"
#import "NSData+BTConversion.h"
#import "BTDatabaseUtil.h"
#import "security.h"


static uint32_t seqNo = 0;

@interface BTMessageSendManager(PrivateAPI)

-(NSString*)toSendmessageContentFromContent:(NSString*)content;

@end

@implementation BTMessageSendManager
{
    NSUInteger _uploadImageCount;
}

+(instancetype)instance
{
    static BTMessageSendManager *g_messageSendManager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_messageSendManager = [[BTMessageSendManager alloc] init];
    });
    return g_messageSendManager;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _uploadImageCount = 0;
        _messagesWaitToBeSend = [[NSMutableArray alloc] init];
        _sendMessageQueue = dispatch_queue_create("com.lsx.bigtalk.sendmessagequeue", NULL);
    }
    return self;
}

-(void)sendMessage:(BTMessageEntity *)message
           isGroup:(BOOL)isGroup
           session:(BTSessionEntity *)session
        completion:(BTSendMessageCompletion)completion
             error:(void(^)(NSError *))errorBlock
{

    dispatch_async(self.sendMessageQueue, ^{
        BTSendMessageAPI *sendMessageAPI = [[BTSendMessageAPI alloc] init];
        uint32_t nowSeqNo = ++seqNo;
        message.seqNo = nowSeqNo;
   
        NSString *newContent = message.msgContent;
        if ([message isImageMessage])
        {
            NSDictionary *dic = [NSDictionary initWithJsonString:message.msgContent];
            NSString *urlPath = dic[kBTImageRemoteUrl];
            newContent = urlPath;
        }

        // 消息加密
        char *pOut;
        uint32_t nOutLen;
        const char *temp = [newContent cStringUsingEncoding:NSUTF8StringEncoding];
        uint32_t nInLen = strlen(temp);
        EncryptMsg(temp, nInLen, &pOut, nOutLen);
        NSData *data = [[NSString stringWithCString:pOut encoding:NSUTF8StringEncoding] dataUsingEncoding:NSUTF8StringEncoding];
        Free(pOut);
        NSArray *object = @[BTRuntime.user.objId, session.sessionId, data, @(message.msgType), @(message.msgId)];
        if ([message isImageMessage])
        {
            session.lastMsg = @"[图片]";
        }
        else if ([message isVoiceMessage])
        {
            session.lastMsg = @"[语音]";
        }
        else
        {
             session.lastMsg = message.msgContent;
        }
        // TODO: sending state
        [[BTUnAckMessageManager instance] addMessageToUnAckQueue:message];
        [[NSNotificationCenter defaultCenter] postNotificationName:BTNotificationSendMessageSuccess object:session];
        [sendMessageAPI requestWithObject:object completion:^(id response, NSError *error) {
            if (!error)
            {
                NSLog(@"message send success, message id: %d, message content: %@", (unsigned int)message.msgId, message.msgContent);
                [[BTDatabaseUtil instance] deleteMessages:message completion:^(BOOL success) {
                   
                }];
            
                [[BTUnAckMessageManager instance] removeMessageFromUnAckQueue:message];
                message.msgId = [response[0] integerValue];
                message.state = MSG_SEND_SUCCESS;
                session.lastMsgId = message.msgId;
                session.timeInterval = message.msgTime;
                [[NSNotificationCenter defaultCenter] postNotificationName:BTNotificationSendMessageSuccess object:session];
                [[BTDatabaseUtil instance] insertMessages:@[message] success:^{
                    completion(message, nil);
                } failure:^(NSString *error) {
                    BTLog(@"message return from server insert into db failed: %@", error);
                }];
            }
            else
            {
                message.state = MSG_SEND_FAILURE;
                [[BTDatabaseUtil instance] insertMessages:@[message] success:^{
                    
                } failure:^(NSString *error) {
                    
                }];
                NSError *err = [NSError errorWithDomain:@"message send failed" code:0 userInfo:nil];
                errorBlock(err);
            }
        }];
    });
}

-(void)sendVoiceMessage:(NSData *)voice
               filePath:(NSString *)filePath
              sessionId:(NSString *)sessionId
                isGroup:(BOOL)isGroup
                message:(BTMessageEntity *)msg
                session:(BTSessionEntity *)session
             completion:(BTSendMessageCompletion)completion
{
    dispatch_async(self.sendMessageQueue, ^{
        BTSendMessageAPI *sendMessageAPI = [[BTSendMessageAPI alloc] init];
        NSString *myUserId = [BTRuntimeStatus instance].user.objId;
        
        NSArray *array = @[@(1), sessionId, voice, @(msg.msgType), @(0)];
        [sendMessageAPI requestWithObject:array completion:^(id response, NSError *error) {
            if (!error)
            {
                NSLog(@"send void message success: %@", [((BTMessageEntity *)msg) msgContent]);
                [[BTDatabaseUtil instance] deleteMessages:msg completion:^(BOOL success) {
                    
                }];
                
                NSUInteger messageTime = [[NSDate date] timeIntervalSince1970];
                msg.msgTime = messageTime;
                msg.msgId = [response[0] integerValue];
                msg.state = MSG_SEND_SUCCESS;
                session.lastMsg = @"[语音]";
                session.lastMsgId = msg.msgId;
                session.timeInterval = msg.msgTime;
                [[NSNotificationCenter defaultCenter] postNotificationName:BTNotificationSendMessageSuccess object:session];
                // 只更新了消息没有更新会话？
                [[BTDatabaseUtil instance] insertMessages:@[msg] success:^{
                    
                } failure:^(NSString *error) {
                    
                }];

                completion(msg, nil);
            }
            else
            {
                BTLog(@"send voice message error: %@", error);
                NSError *error = [NSError errorWithDomain:@"send void message failed" code:0 userInfo:nil];
                completion(nil, error);
            }
        }];

    });
}

#pragma mark Private API
-(NSString *)toSendmessageContentFromContent:(NSString *)content
{
    BTEmotionsModule *emotionModule = [BTEmotionsModule shareInstance];
    NSDictionary *unicodeDic = emotionModule.unicodeEmotionDic;
    NSArray *allEmotions = emotionModule.emotions;
    NSMutableString *newContent = [NSMutableString stringWithString:content];
    [allEmotions enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        NSString *emotion = (NSString *)obj;
        if ([newContent rangeOfString:emotion].length > 0)
        {
            NSString *placeholder = unicodeDic[emotion];
            NSRange range = NSMakeRange(0, newContent.length);
            [newContent replaceOccurrencesOfString:emotion withString:placeholder options:0 range:range];
        }
    }];
    return newContent;
}

@end
