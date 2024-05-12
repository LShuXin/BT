//
//  BTMessageModule.m
//

#import "BTDatabaseUtil.h"
#import "BTMsgReadNotify.h"
#import "BTConsts.h"
#import "BTReceiveMessageAPI.h"
#import "BTGetUnreadMessagesAPI.h"
#import "BTMsgReadACKAPI.h"
#import "BTGetMessageQueueAPI.h"
#import "BTReceiveMessageACKAPI.h"
#import "BTAFClient.h"
#import "BTSessionEntity.h"
#import "BTRuntimeStatus.h"
#import "BTUserModule.h"
#import "BTMessageModule.h"
#import "BTGroupModule.h"
#import "BTAnalysisImage.h"
#import "BTRecentUsersViewController.h"


@interface BTMessageModule(PrivateAPI)
-(void)p_registerReceiveMessageAPI;
-(void)p_saveReceivedMessage:(BTMessageEntity *)message;
-(void)n_receiveLoginSuccessNotification:(NSNotification *)notification;
-(void)n_receiveUserLogoutNotification:(NSNotification *)notification;
-(NSArray *)p_spliteMessage:(BTMessageEntity *)message;
@end


@implementation BTMessageModule
{
    NSMutableDictionary *_unreadMessages;
}

+(instancetype)shareInstance
{
    static BTMessageModule *g_messageModule;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_messageModule = [[BTMessageModule alloc] init];
    });
    return g_messageModule;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        self.unreadMsgCount = 0;
        _unreadMessages = [[NSMutableDictionary alloc] init];
        // 注册收到消息API
        [self p_registerReceiveMessageAPI];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveLoginSuccessNotification:)
                                                     name:BTNotificationUserLoginSuccess
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveLoginSuccessNotification:)
                                                     name:BTNotificationUserReloginSuccess
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveUserLogoutNotification:)
                                                     name:BTNotificationUserLogout
                                                   object:nil];
    }
    
    return self;
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

+(NSUInteger)generateMessageId
{
    NSInteger messageId = [[NSUserDefaults standardUserDefaults] integerForKey:@"msgId"];
    if (messageId == 0)
    {
        // TODO: why
        messageId = LOCAL_MSG_BEGIN_ID;
    }
    else
    {
        messageId++;
    }
    [[NSUserDefaults standardUserDefaults] setInteger:messageId forKey:@"msgId"];
    [[NSUserDefaults standardUserDefaults] synchronize];
    return messageId;
}

-(void)getLastMessageBySessionId:(NSString *)sessionId completion:(GetLastestMessageCompletion)completion
{
    [[BTDatabaseUtil instance] getLastestMessageBySessionId:sessionId completion:^(BTMessageEntity *message, NSError *error) {
        completion(message);
    }];
}

-(void)addUnreadMessage:(BTMessageEntity *)message
{
    // 在 iOS 开发中，@synchronized(self) 是一种用于保护多线程环境下代码同步的机制。
    // 它通过创建一个互斥锁，保证同一时刻只有一个线程能够执行被锁住的代码块，从而避免对共享资源的访问冲突。
    @synchronized(self)
    {
        if (!message)
        {
            return;
        }
        
        // TODO: why
        if ([message.sessionId isEqualToString:@"1szei2"])
        {
            return;
        }
        
        if (![message isGroupMessage])
        {
            // 单聊
            // 目前单聊与群聊的逻辑是一致的
            // 以会话为单位存储多条未读消息
            if ([[_unreadMessages allKeys] containsObject:message.sessionId])
            {
                NSMutableArray *unreadMessage = _unreadMessages[message.sessionId];
                [unreadMessage addObject:message];
            }
            else
            {
                NSMutableArray *unreadMessages = [[NSMutableArray alloc] init];
                [unreadMessages addObject:message];
                [_unreadMessages setObject:unreadMessages forKey:message.sessionId];
            }
        }
        else
        {
            // 群聊
            if ([[_unreadMessages allKeys] containsObject:message.sessionId])
            {
                NSMutableArray *unreadMessage = _unreadMessages[message.sessionId];
                [unreadMessage addObject:message];
            }
            else
            {
                NSMutableArray *unreadMessages = [[NSMutableArray alloc] init];
                [unreadMessages addObject:message];
                [_unreadMessages setObject:unreadMessages forKey:message.sessionId];
            }
        }
    }
}

-(void)sendMsgRead:(BTMessageEntity *)message
{
    BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
    [readACK requestWithObject:@[message.sessionId, @(message.msgId), @(message.sessionType)] completion:nil];
}

-(void)clearUnreadMessagesBySessionId:(NSString *)sessionId
{
    NSMutableArray *unreadMessages = _unreadMessages[sessionId];
    if (unreadMessages)
    {
        [unreadMessages enumerateObjectsUsingBlock:^(BTMessageEntity *messageEntity, NSUInteger idx, BOOL *stop) {
            [[BTDatabaseUtil instance] insertMessages:@[messageEntity] success:^{
                BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
                [readACK requestWithObject:@[messageEntity.sessionId, @(messageEntity.msgId), @(messageEntity.msgType)] completion:nil];
            } failure:^(NSString *error) {
                NSLog(@"消息插入DB失败");
            }];
        }];
    }
    [unreadMessages removeAllObjects];
    [self setApplicationUnreadMsgCount];
}

-(NSUInteger)getUnreadMessageCountBySessionId:(NSString *)sessionId
{
    if ([sessionId isEqualToString:BTRuntime.userId])
    {
        return 0;
    }
    
    NSMutableArray *unreadMessages = _unreadMessages[sessionId];
    return [unreadMessages count];
}

-(NSArray *)getUnreadMessagesBySessionId:(NSString *)sessionId
{
    return _unreadMessages[sessionId];
}

-(NSUInteger)getUnreadMessgeCount
{
    __block NSUInteger count = 0;
    [_unreadMessages enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        count += [obj count];
    }];
    
    return count;
}

-(void)removeFromUnreadMessageButNotSendRead:(NSString *)sessionId
{
    NSMutableArray *messages = _unreadMessages[sessionId];
    BTLog(@"remove messages，count：%d--->, sessionId：%@",  (unsigned int)[messages count], sessionId);
    if ([messages count] > 0)
    {
        [_unreadMessages removeObjectForKey:sessionId];
    }
}

-(NSArray *)popAllUnreadMessagesBySessionId:(NSString *)sessionId
{
    NSMutableArray *messages = _unreadMessages[sessionId];
    if ([messages count] > 0)
    {
        // 只要ack最新的就可以
        [[BTDatabaseUtil instance] insertMessages:messages success:^{
            BTMessageEntity *message = messages[0];
            SessionType sessionType = [message getMessageSessionType];
            BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
            [readACK requestWithObject:@[message.sessionId, @(message.msgId), @(sessionType)] completion:nil];
        } failure:^(NSString *error) {
            NSLog(@"ack failed");
        }];
        [_unreadMessages removeObjectForKey:sessionId];
        return messages;
    }
    else
    {
        return nil;
    }
}

#pragma mark - privateAPI
-(void)p_registerReceiveMessageAPI
{
    BTReceiveMessageAPI *receiveMessageAPI = [[BTReceiveMessageAPI alloc] init];
    [receiveMessageAPI registerAPIInAPIScheduleReceiveData:^(BTMessageEntity* object, NSError* error) {
        object.state = MSG_SEND_SUCCESS;
        BTReceiveMessageACKAPI *rmack = [[BTReceiveMessageACKAPI alloc] init];
        [rmack requestWithObject:@[object.senderId, @(object.msgId), object.sessionId, @(object.sessionType)] completion:^(id response, NSError *error) {
            
        }];
        NSArray *messages = [self p_spliteMessage:object];

        if ([object isGroupMessage])
        {
            BTGroupEntity *group = [[BTGroupModule instance] getGroupByGroupId:object.sessionId];
            if (group.isShield == 1)
            {
                BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
                [readACK requestWithObject:@[object.sessionId, @(object.msgId), @(object.sessionType)] completion:nil];
            }
        }
        [[BTDatabaseUtil instance] insertMessages:@[object] success:^{
            
        } failure:^(NSString *error) {
            
        }];
        [BTNotificationHelper postNotification:BTNotificationReceiveMessage userInfo:nil object:object];
    }];
    
    // 消息已读通知
    // TODO:
//    BTMsgReadNotify *msgReadNotify = [BTMsgReadNotify new];
//    [msgReadNotify requestWithObject:nil completion:^(NSDictionary *object, NSError *error) {
//        NSString *fromId = [object objectForKey:@"fromId"];
//        UInt32 msgId = [[object objectForKey:@"msgId"] integerValue];
//        UInt32 sessionType = [[object objectForKey:@"sessionType"] integerValue];
//        [self cleanMessageFromNotifi:msgId sessionId:fromId sessionType:sessionType];
//    }];
}

// 收到了新消息
-(void)p_saveReceivedMessage:(BTMessageEntity *)messageEntity
{
    BTSessionEntity *session = [[BTSessionEntity alloc] initWithSessionId:messageEntity.sessionId
                                                              sessionType:messageEntity.sessionType];
    [session updateUpdateTime:messageEntity.msgTime];
    if (messageEntity)
    {
        messageEntity.state = MSG_SEND_SUCCESS;
        [self addUnreadMessage:messageEntity];
        [BTNotificationHelper postNotification:BTNotificationReceiveMessage userInfo:nil object:messageEntity];
    }
}

// 从服务端分页获取消息
-(void)getMessageFromServerFromMsgId:(NSInteger)fromMsgId
                             session:(BTSessionEntity *)session
                                  count:(NSInteger)count
                             completion:(void(^)(NSMutableArray *array, NSError *error))completion
{
    BTGetMessageQueueAPI *getMessageQueue = [BTGetMessageQueueAPI new];
    [getMessageQueue requestWithObject:@[@(fromMsgId), @(count), @(session.sessionType), session.sessionId] completion:^(NSMutableArray *response, NSError *error) {
        completion(response, error);
    }];
}

-(void)n_receiveLoginSuccessNotification:(NSNotification *)notification
{
    BTLog(@"message module received login success notification");
    //_unreadMessages = [[NSMutableDictionary alloc] init];
}

-(void)removeArrayMessage:(NSString *)sessionId
{
    if (!sessionId)
    {
        return;
    }
    [_unreadMessages removeObjectForKey:sessionId];
    [self setApplicationUnreadMsgCount];
}

-(void)n_receiveUserLogoutNotification:(NSNotification *)notification
{
    BTLog(@"message module received logout notification");
    [_unreadMessages removeAllObjects];
}

// rocesses a message entity (BTMessageEntity) and potentially splits it into separate messages.
-(NSArray *)p_spliteMessage:(BTMessageEntity *)message
{
    NSMutableArray *messageContentArray = [[NSMutableArray alloc] init];
    if (
        message.msgContentType == MSG_TYPE_IMAGE
        ||
        (message.msgContentType == MSG_TYPE_TEXT && [message.msgContent rangeOfString:kBTImageMessagePrefix].length > 0)
    )
    {
        NSString *messageContent = [message msgContent];
        NSArray *tempMessageContent = [messageContent componentsSeparatedByString:kBTImageMessagePrefix];
        [tempMessageContent enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL* stop) {
            NSString *content = (NSString *)obj;
            if ([content length] > 0)
            {
                NSRange suffixRange = [content rangeOfString:kBTImageMessageSuffix];
                // 夹带的图片消息
                if (suffixRange.length > 0)
                {
                    NSString *imageContent = [NSString stringWithFormat:@"%@%@", kBTImageMessagePrefix, [content substringToIndex:suffixRange.location + suffixRange.length]];
                    
                    BTMessageEntity *messageEntity = [[BTMessageEntity alloc] initWithMsgId:[BTMessageModule generateMessageId]
                                                                                    msgType:message.msgType
                                                                                    msgTime:message.msgTime
                                                                                  sessionId:message.sessionId
                                                                                   senderId:message.senderId
                                                                                 msgContent:imageContent
                                                                                   toUserId:message.toUserId];
                    messageEntity.msgContentType = MSG_TYPE_IMAGE;
                    messageEntity.state = MSG_SEND_SUCCESS;
                    [messageContentArray addObject:messageEntity];
                    
                    // 查看图片后面是否还有文本消息
                    NSString *secondComponent = [content substringFromIndex:suffixRange.location + suffixRange.length];
                    if (secondComponent.length > 0)
                    {
                        BTMessageEntity *secondmessageEntity = [[BTMessageEntity alloc] initWithMsgId:[BTMessageModule generateMessageId]
                                                                                              msgType:message.msgType
                                                                                              msgTime:message.msgTime
                                                                                            sessionId:message.sessionId
                                                                                             senderId:message.senderId
                                                                                           msgContent:secondComponent
                                                                                             toUserId:message.toUserId];
                        secondmessageEntity.msgContentType = MSG_TYPE_TEXT;
                        secondmessageEntity.state = MSG_SEND_SUCCESS;
                        [messageContentArray addObject:secondmessageEntity];
                    }
                }
                else
                {
                    // 文本消息
                    BTMessageEntity *messageEntity = [[BTMessageEntity alloc] initWithMsgId:[BTMessageModule generateMessageId]
                                                                                    msgType:message.msgType
                                                                                    msgTime:message.msgTime
                                                                                  sessionId:message.sessionId
                                                                                   senderId:message.senderId
                                                                                 msgContent:content
                                                                                   toUserId:message.toUserId];
                    messageEntity.msgContentType = MSG_TYPE_TEXT;
                    messageEntity.state = MSG_SEND_SUCCESS;
                    [messageContentArray addObject:messageEntity];
                }
            }
        }];
    }
    if ([messageContentArray count] == 0)
    {
        [messageContentArray addObject:message];
    }
    return messageContentArray;
}

-(void)setApplicationUnreadMsgCount
{
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:[self getUnreadMessgeCount]];
}

-(NSUInteger)getUnreadCountById:(NSString *)sessionId
{
    return 0;
}

// TODO: why
-(void)cleanMessageFromNotifi:(NSUInteger)messageId
                    sessionId:(NSString *)sessionId
                  sessionType:(SessionType)sessionType
{
//    NSMutableArray *messages = _unreadMessages[sessionId];
//    [messages enumerateObjectsUsingBlock:^(BTMessageEntity *obj, NSUInteger idx, BOOL *stop) {
//        if (obj.msgId < messageId)
//        {
//          [[BTDatabaseUtil instance] insertMessages:@[obj] success:^{
//            SessionType sessionType =[obj getMessageSessionType];
//            BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
//            [readACK requestWithObject:@[obj.sessionId, @(obj.msgId), @(sessionType)] completion:nil];
//            [messages removeObject:obj];
//            [[NSNotificationCenter defaultCenter] postNotificationName:@"MessageReadACK" object:obj];
//          } failure:^(NSString *error) {
//
//          }];
//        }
//    }];
}

@end
