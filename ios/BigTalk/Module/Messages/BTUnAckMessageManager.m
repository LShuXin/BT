//
//  BTUnAckMessageManager.m
//

#import "BTUnAckMessageManager.h"
#import "BTMessageEntity.h"
#import "BTDatabaseUtil.h"


#define MESSAGE_TIMEOUT_SEC 5


@interface MessageAndTime : NSObject
@property(strong)BTMessageEntity *msg;
@property(assign)NSUInteger nowDate;
@end

@implementation MessageAndTime
@end

@interface BTUnAckMessageManager()
@property(strong)NSMutableDictionary *msgDic;
@property(strong)NSTimer *ack_Timer;
@end


@implementation BTUnAckMessageManager

+(instancetype)instance
{
    static BTUnAckMessageManager *g_unackManager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_unackManager = [[BTUnAckMessageManager alloc] init];
    });
    return g_unackManager;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.msgDic = [NSMutableDictionary new];
        self.ack_Timer = [NSTimer scheduledTimerWithTimeInterval:MESSAGE_TIMEOUT_SEC
                                                          target:self
                                                        selector:@selector(checkMessageTimeout)
                                                        userInfo:nil
                                                         repeats:YES];
    }
    return self;
}

-(BOOL)isInUnAckQueue:(BTMessageEntity *)message
{
    if ([[self.msgDic allKeys] containsObject:@(message.msgId)])
    {
        return YES;
    }
    return NO;
}

-(void)removeMessageFromUnAckQueue:(BTMessageEntity *)message
{
    if ([[self.msgDic allKeys] containsObject:@(message.msgId)])
    {
        [self.msgDic removeObjectForKey:@(message.msgId)];
    }
}

-(void)addMessageToUnAckQueue:(BTMessageEntity *)message
{
    BTLog(@"add message to unAck queue, message id: %lu, message description: %@", message.msgId, [message description]);
    MessageAndTime *msgAndTime = [MessageAndTime new];
    msgAndTime.msg = message;
    msgAndTime.nowDate = [[NSDate date] timeIntervalSince1970];
    if (self.msgDic)
    {
        [self.msgDic setObject:msgAndTime forKey:@(message.msgId)];
    }
}

-(void)checkMessageTimeout
{
    [[self.msgDic allValues] enumerateObjectsUsingBlock:^(MessageAndTime *obj, NSUInteger idx, BOOL *stop) {
        NSUInteger timeNow = [[NSDate date] timeIntervalSince1970];
        NSUInteger msgTimeOut = obj.nowDate + MESSAGE_TIMEOUT_SEC;
        if (timeNow >= msgTimeOut)
        {
            NSLog(@"message timeout, because the message was not acked by server in %lu, the message id is %lu", msgTimeOut, obj.msg.msgId);
            obj.msg.state = MSG_SEND_FAILURE;
            [[BTDatabaseUtil instance] updateMessage:obj.msg completion:^(BOOL result) {
                
            }];
            [self removeMessageFromUnAckQueue:obj.msg];
        }
    }];
}

@end

