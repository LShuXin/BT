//
//  BTSessionModule.m
//

#import "BTSessionModule.h"
#import "BTSessionEntity.h"
#import "NSDictionary+BTSafe.h"
#import "BTGetUnreadMessagesAPI.h"
#import "BTRemoveSessionAPI.h"
#import "BTDatabaseUtil.h"
#import "BTGetRecentSession.h"
#import "BTMessageEntity.h"
#import "BTChattingMainViewController.h"
#import "BTMsgReadNotify.h"
#import "BTMsgReadACKAPI.h"
#import "BTSpellLibrary.h"
#import "BTGroupModule.h"
#import "BTNotificationHelper.h"


@interface BTSessionModule()
@property(strong)NSMutableDictionary *sessions;
@end


@implementation BTSessionModule
DEF_SINGLETON(BTSessionModule)

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.sessions = [NSMutableDictionary new];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(sentMessageSuccessfull:)
                                                     name:BTNotificationSendMessageSuccess
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(getMessageReadACK:)
                                                     name:@"MessageReadACK"
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(logout)
                                                     name:BTNotificationUserLogout
                                                   object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveMessageNotification:)
                                                     name:BTNotificationReceiveMessage
                                                   object:nil];
        
        BTMsgReadNotify *msgReadNotify = [[BTMsgReadNotify alloc] init];
        [msgReadNotify registerAPIInAPIScheduleReceiveData:^(NSDictionary *object, NSError *error) {
            NSString *fromId = [object objectForKey:@"from_id"];
            NSInteger msgID = [[object objectForKey:@"msgId"] integerValue];
            SessionType type = [[object objectForKey:@"type"] intValue];
            [self cleanMessageFromNotifi:msgID sessionId:fromId sessionType:type];
        }];
    }
    return self;
}

-(BTSessionEntity *)getSessionById:(NSString *)sessionId
{
    return [self.sessions safeObjectForKey:sessionId];
}

-(void)removeSessionById:(NSString *)sessionId
{
    [self.sessions removeObjectForKey:sessionId];
}

-(void)addToSessionModel:(BTSessionEntity *)session
{
    [self.sessions safeSetObject:session forKey:session.sessionId];
}

-(NSUInteger)getAllUnreadMessageCount
{
    return [[[self getAllSessions] valueForKeyPath:@"@sum.unReadMsgCount"] integerValue];
}

-(void)addSessionsToSessionModel:(NSArray *)sessionArray
{
    [sessionArray enumerateObjectsUsingBlock:^(BTSessionEntity *session, NSUInteger idx, BOOL *stop) {
        [self.sessions safeSetObject:session forKey:session.sessionId];
    }];
}

// 从服务端获取含有未读消息的回话
// 这是一种方案，另一种方案也比较常用，即不直接获取含有未读消息的会话，
// 而是直接拉取未读消息,拉取消息时再根据情况决定是否拉取会话
-(void)getHadUnreadMessageSession:(void(^)(NSUInteger count))block
{
    BTGetUnreadMessagesAPI *getUnreadMessage = [BTGetUnreadMessagesAPI new];
    [getUnreadMessage requestWithObject:BTRuntime.user.objId completion:^(NSDictionary *dic, NSError *error) {
        NSInteger mTotalCnt = [dic[@"m_total_cnt"] integerValue];
        NSArray *updatedSessions = dic[@"sessions"];
        [updatedSessions enumerateObjectsUsingBlock:^(BTSessionEntity *obj, NSUInteger idx, BOOL *stop) {
            if ([self getSessionById:obj.sessionId])
            {
                BTSessionEntity *session = [self getSessionById:obj.sessionId];
                NSInteger lostMsgCount = obj.lastMsgId - session.lastMsgId;
                obj.lastMsg = session.lastMsg;
                if ([[BTChattingMainViewController shareInstance].module.sessionEntity.sessionId isEqualToString:obj.sessionId])
                {
                    [[NSNotificationCenter defaultCenter] postNotificationName:@"ChattingSessionUpdate"
                                                                        object:@{@"session": obj, @"count": @(lostMsgCount)}];
                }
                session = obj;
                [self addToSessionModel:obj];
            }
            if (self.delegate && [self.delegate respondsToSelector:@selector(sessionUpdate:action:)])
            {
                [self.delegate sessionUpdate:obj action:ADD];
            }
        }];
        // [self addSessionsToSessionModel:sessions];
        block(mTotalCnt);
        // 通知外层sessionmodel发生更新
    }];
}

-(NSUInteger)getMaxTime
{
    NSArray *array = [self getAllSessions];
    NSUInteger maxTime = [[array valueForKeyPath:@"@max.timeInterval"] integerValue];
    if (maxTime)
    {
        return maxTime;
    }
    return 0;
}

// 获取从某个时间点以后又有新变动的会话
-(void)getRecentSession:(void(^)(NSUInteger count))block
{
    BTGetRecentSession *getRecentSession = [[BTGetRecentSession alloc] init];
    NSInteger localMaxTime = [self getMaxTime];
    [getRecentSession requestWithObject:@[@(localMaxTime)] completion:^(NSArray *response, NSError *error) {
        NSMutableArray *array = [NSMutableArray arrayWithArray:response];
        [self addSessionsToSessionModel:array];
        response = array;
        // 获取有未读消息的会话
        [self getHadUnreadMessageSession:^(NSUInteger count) {
            
        }];
        [response enumerateObjectsUsingBlock:^(BTSessionEntity *obj, NSUInteger idx, BOOL *stop) {
            //同步到数据库中
            [[BTDatabaseUtil instance] updateSession:obj completion:^(NSError *error) {
                        
            }];
        }];
        block(0);
    }];
}

-(NSArray *)getAllSessions
{
    return [self.sessions allValues];
}

-(void)removeSessionByServer:(BTSessionEntity *)session
{
    // 内存删除
    [self.sessions removeObjectForKey:session.sessionId];
    // 数据库删除
    [[BTDatabaseUtil instance] removeSession:session.sessionId];
    // 服务端删除
    BTRemoveSessionAPI *removeSession = [BTRemoveSessionAPI new];
    SessionType sessionType = session.sessionType;
    [removeSession requestWithObject:@[session.sessionId, @(sessionType)] completion:^(id response, NSError *error) {
        
    }];
}

// 某个会话的未读消息数减1
-(void)getMessageReadACK:(NSNotification *)notification
{
    BTMessageEntity *message = [notification object];
    if ([[self.sessions allKeys] containsObject:message.sessionId])
    {
        BTSessionEntity *session = [self.sessions objectForKey:message.sessionId];
        session.unReadMsgCount = session.unReadMsgCount - 1;
    }
}

-(void)n_receiveMessageNotification:(NSNotification *)notification
{
    BTMessageEntity *message = [notification object];
    SessionType sessionType;
    BTSessionEntity *session;
    if ([message isGroupMessage])
    {
        sessionType = SessionType_SessionTypeGroup;
    }
    else
    {
        sessionType = SessionType_SessionTypeSingle;
    }
   
    if ([[self.sessions allKeys] containsObject:message.sessionId])
    {
        session = [self.sessions objectForKey:message.sessionId];
        session.lastMsg = message.msgContent;
        session.lastMsgId = message.msgId;
        session.timeInterval = message.msgTime;
        // 收到消息的会话的未读消息数量+1
        if (![message.sessionId isEqualToString:[BTChattingMainViewController shareInstance].module.sessionEntity.sessionId])
        {
            if (![message.senderId isEqualToString:BTRuntime.user.objId])
            {
                session.unReadMsgCount = session.unReadMsgCount + 1;
            }
        }
    }
    else
    {
        session = [[BTSessionEntity alloc] initWithSessionId:message.sessionId sessionType:sessionType];
        session.lastMsg = message.msgContent;
        session.lastMsgId = message.msgId;
        session.timeInterval = message.msgTime;
        // 收到消息的会话的未读消息数+1
        if (![message.sessionId isEqualToString:[BTChattingMainViewController shareInstance].module.sessionEntity.sessionId])
        {
            if (![message.senderId isEqualToString:BTRuntime.user.objId])
            {
                session.unReadMsgCount = session.unReadMsgCount + 1;
            }
            
        }
        [self addSessionsToSessionModel:@[session]];
    }
    
    // 将更新的会话同步到数据
    // 应该先写数据库，然后更新UI
    [self updateToDatabase:session];
   
    if (self.delegate && [self.delegate respondsToSelector:@selector(sessionUpdate:action:)])
    {
        [self.delegate sessionUpdate:session action:ADD];
    }
}

// 将会话同步到数据库
-(void)updateToDatabase:(BTSessionEntity *)session
{
    [[BTDatabaseUtil instance] updateSession:session completion:^(NSError *error) {
        
    }];
}

// once message send success, we should update instead of add
-(void)sentMessageSuccessfull:(NSNotification *)notification
{
    BTSessionEntity *session = [notification object];
    [self addSessionsToSessionModel:@[session]];
    if (self.delegate && [self.delegate respondsToSelector:@selector(sessionUpdate:action:)])
    {
        [self.delegate sessionUpdate:session action:ADD];
    }
    [self updateToDatabase:session];
}

// add db sessions into memory
-(void)loadLocalSession:(void(^)(bool isok))block
{
    [[BTDatabaseUtil instance] loadAllSessionsCompletion:^(NSArray *session, NSError *error) {
        [self addSessionsToSessionModel:session];
        block(YES);
    }];
}

// no idea what's for
-(void)cleanMessageFromNotifi:(NSUInteger)messageId
                    sessionId:(NSString *)sessionId
                  sessionType:(SessionType)type
{
    // if the message is from peer
    if (![sessionId isEqualToString:BTRuntime.user.objId])
    {
        BTSessionEntity *session = [self getSessionById:sessionId];
        if (session)
        {
            NSInteger readCount = messageId - session.lastMsgId;
            
            if (readCount == 0)
            {
                // the lastest message is exactly the one we need to delete
                session.unReadMsgCount = 0;
                if (self.delegate && [self.delegate respondsToSelector:@selector(sessionUpdate:Action:)])
                {
                    [self.delegate sessionUpdate:session action:ADD];
                }
                [self updateToDatabase:session];
            }
            else if (readCount > 0)
            {
                session.unReadMsgCount = readCount;
                if (self.delegate && [self.delegate respondsToSelector:@selector(sessionUpdate:Action:)])
                {
                    [self.delegate sessionUpdate:session action:ADD];
                }
                [self updateToDatabase:session];
            }
            BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
            [readACK requestWithObject:@[sessionId, @(messageId), @(type)] completion:nil];
        }
    }
}

-(void)logout
{
    [self.sessions removeAllObjects];
}
@end
