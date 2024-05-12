//
//  BTChattingModule.m
//

#import <math.h>
#import "BTChattingModule.h"
#import "BTDatabaseUtil.h"
#import "BTChatTextCell.h"
#import "NSDate+BTAddition.h"
#import "BTUserModule.h"
#import "BTGetMessageQueueAPI.h"
#import "BTMessageModule.h"
#import "BTMsgReadACKAPI.h"
#import "BTGetMsgsByMsgIdsAPI.h"
#import "BTClientState.h"

// 5 minutes
static NSUInteger const showPromptGapSec = 300;

@interface BTChattingModule(privateAPI)
-(NSUInteger)p_getMessageCount;
-(void)p_addHistoryMessages:(NSArray *)messages completion:(BTChatLoadMoreHistoryCompletion)completion;
@end

@implementation BTChattingModule
{
    // used for calculating cell height
    BTChatTextCell *_textCell;
    NSUInteger _earliestDate;
    NSUInteger _lastestDate;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.showingMessages = [[NSMutableArray alloc] init];
        self.ids = [NSMutableArray new];
    }
    return self;
}

-(void)setSessionEntity:(BTSessionEntity *)sessionEntity
{
    _sessionEntity = sessionEntity;
    self.showingMessages = nil;
    self.showingMessages = [[NSMutableArray alloc] init];
}

-(void)getNewMsg:(BTChatLoadMoreHistoryCompletion)completion
{
    [[BTMessageModule shareInstance] getMessageFromServerFromMsgId:0 session:self.sessionEntity count:BT_PAGE_ITEM_COUNT completion:^(NSMutableArray *response, NSError *error) {
        // TODO: rename
        NSUInteger msgId = [[response valueForKeyPath:@"@max.msgID"] integerValue];
        if (msgId != 0)
        {
            if (response)
            {
                NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"msgTime" ascending:YES];
                [response sortUsingDescriptors:[NSArray arrayWithObject:sortDescriptor]];
                [[BTDatabaseUtil instance] insertMessages:response success:^{
                    BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
                    // only ack the biggest msgId
                    [readACK requestWithObject:@[self.sessionEntity.sessionId, @(msgId), @(self.sessionEntity.sessionType)] completion:nil];
                } failure:^(NSString *error) {
                    BTLog(@"ack msg failed, msgId: %d", (unsigned int)msgId);
                }];
                [response enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                    [self addShowMessage:obj];
                }];
                BTLog(@"get new msg from server success, count: %d", (unsigned int)[response count]);
                completion([response count], error);
            }
        }
        else
        {
            BTLog(@"get new msg from server: empty");
            completion(0, error);
        }
    }];
}

-(void)loadHistoryMessageFromServer:(NSUInteger)fromMsgId loadCount:(NSUInteger)count completion:(BTChatLoadMoreHistoryCompletion)completion
{
    if (self.sessionEntity)
    {
        if (fromMsgId != 1)
        {
            [[BTMessageModule shareInstance] getMessageFromServerFromMsgId:fromMsgId session:self.sessionEntity count:count completion:^(NSArray *response, NSError *error) {
                // TODO: rename
                NSUInteger msgId = [[response valueForKeyPath:@"@max.msgID"] integerValue];
                if (msgId != 0)
                {
                    if (response)
                    {
                        [[BTDatabaseUtil instance] insertMessages:response success:^{
                            BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
                            [readACK requestWithObject:@[self.sessionEntity.sessionId, @(msgId), @(self.sessionEntity.sessionType)] completion:nil];
                        } failure:^(NSString *error) {
                            BTLog(@"ack msg error: %@", error);
                        }];
                        
                        NSUInteger count = [self p_getMessageCount];
                        [[BTDatabaseUtil instance] loadMessagesBySessionId:self.sessionEntity.sessionId
                                                                     limit:BT_PAGE_ITEM_COUNT
                                                                    offset:count
                                                                completion:^(NSArray *messages, NSError *error) {
                            
                            [self p_addHistoryMessages:messages completion:completion];
                            BTLog(@"load history message from server success, count: %d", (unsigned int)[messages count]);
                            completion([response count], error);
                        }];
                    }
                }
                else
                {
                    BTLog(@"load history message from server success, count: 0");
                    completion(0, error);
                }
            }];
        }
        else
        {
            BTLog(@"load history message from server fromMsgId 1, interupt");
            completion(0, nil);
        }
    }
}

-(void)loadHostoryMessageFromServer:(NSUInteger)fromMsgId completion:(BTChatLoadMoreHistoryCompletion)completion
{
    // why 19
    [self loadHistoryMessageFromServer:fromMsgId loadCount:19 completion:completion];
}

-(void)loadMoreHistoryCompletion:(BTChatLoadMoreHistoryCompletion)completion
{
    NSUInteger count = [self p_getMessageCount];
  
    [[BTDatabaseUtil instance] loadMessagesBySessionId:self.sessionEntity.sessionId
                                                 limit:BT_PAGE_ITEM_COUNT
                                                 offset:count
                                            completion:^(NSArray *messages, NSError *error) {
        
        //after loading finish ,then add to messages
        if ([BTClientState shareInstance].networkState == NETWORK_DISCONNECT)
        {
            BTLog(@"load more history message, network disconnect");
            [self p_addHistoryMessages:messages completion:completion];
        }
        else
        {
            if ([messages count] != 0)
            {
                
                BOOL isHaveMissMsg = [self p_isHaveMissMsg:messages];
                if (isHaveMissMsg || ([self getMinMsgId] - [self getMaxMsgId:messages] != 0))
                {
                    
                    [self loadHostoryMessageFromServer:[self getMinMsgId] completion:^(NSUInteger addcount, NSError *error) {
                        if (addcount)
                        {
                            completion(addcount, error);
                        }
                        else
                        {
                            [self p_addHistoryMessages:messages completion:completion];
                        }
                    }];
                }
                else
                {
                    // TODO: 检查消息是否连续
                    [self p_addHistoryMessages:messages completion:completion];
                }
            }
            else
            {
                BTLog(@"there is no more history message in db, fetch from server...");
                [self loadHostoryMessageFromServer:[self getMinMsgId] completion:^(NSUInteger addcount, NSError *error) {
                    BTLog(@"load history messsage from server success, count: %d", (unsigned int)addcount);
                    completion(addcount, error);
                }];
            }
        }
    }];
}

-(NSUInteger)getMinMsgId
{
    if ([self.showingMessages count] == 0)
    {
        return self.sessionEntity.lastMsgId;
    }
    
    __block NSInteger minMsgId = [self getMaxMsgId:self.showingMessages];
    [self.showingMessages enumerateObjectsUsingBlock:^(BTMessageEntity *obj, NSUInteger idx, BOOL *stop) {
        if ([obj isKindOfClass:[BTMessageEntity class]])
        {
            if (obj.msgId < minMsgId)
            {
                minMsgId = obj.msgId;
            }
        }
    }];
    
    return minMsgId;
}



-(float)messageHeight:(BTMessageEntity *)message
{
    
    if (message.msgContentType == MSG_TYPE_TEXT)
    {
        if (!_textCell)
        {
            _textCell = [[BTChatTextCell alloc] init];
        }
        return [_textCell cellHeightForMessage:message];
    }
    else if (message.msgContentType == MSG_TYPE_VOICE)
    {
        return 60;
    }
    else if (message.msgContentType == MSG_TYPE_IMAGE || message.msgContentType == MSG_TYPE_EMOTION)
    {
         return 151;
    }
    else
    {
        return 135;
    }
}

-(void)addShowMessage:(BTMessageEntity *)message
{
    if (![self.ids containsObject:@(message.msgId)])
    {
        // 添加时间提示
        if (message.msgTime - _lastestDate > showPromptGapSec)
        {
            _lastestDate = message.msgTime;
            BTPromptEntity *prompt = [[BTPromptEntity alloc] init];
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:message.msgTime];
            prompt.message = [date promptDateString];
            [self.showingMessages addObject:prompt];
        }
        NSArray *array = [[self class] p_spliteMessage:message];
        [array enumerateObjectsUsingBlock:^(BTMessageEntity* obj, NSUInteger idx, BOOL *stop) {
            [[self mutableArrayValueForKeyPath:@"showingMessages"] addObject:obj];
        }];
    }
}

-(void)addShowMessages:(NSArray *)messages
{
    [[self mutableArrayValueForKeyPath:@"showingMessages"] addObjectsFromArray:messages];
}

-(void)getCurrentUser:(void(^)(BTUserEntity *))completion
{
    // TODO: why is sessionId
    [[BTUserModule shareInstance] getUserByUserId:self.sessionEntity.sessionId completion:^(BTUserEntity *user) {
        completion(user);
    }];
}

-(void)updateSessionUpdateTime:(NSUInteger)time
{
    [self.sessionEntity updateUpdateTime:time];
    _lastestDate = time;
}


#pragma mark -
#pragma mark PrivateAPI
-(NSUInteger)p_getMessageCount
{
    __block NSUInteger count = 0;
    [self.showingMessages enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([obj isKindOfClass:NSClassFromString(@"BTMessageEntity")])
        {
            count++;
        }
    }];
    return count;
}

-(void)p_addHistoryMessages:(NSArray *)messages completion:(BTChatLoadMoreHistoryCompletion)completion
{
    // 消息中最早的一条的时间
    __block NSUInteger tempEarliestDate = [[messages valueForKeyPath:@"@min.msgTime"] integerValue];
    // 消息中最晚的一条的时间
    __block NSUInteger tempLastestDate = 0;
    NSUInteger itemCount = [self.showingMessages count];
    NSMutableArray *tmp = [NSMutableArray arrayWithArray:messages];
    NSMutableArray *tempMessages = [[NSMutableArray alloc] init];
    for (NSInteger index = [tmp count] - 1; index >= 0; index--)
    {
        BTMessageEntity *message = tmp[index];
            
        if ([self.ids containsObject:@(message.msgId)])
        {
            // 内存中已经存在了这条消息
            continue;
        }
        
        // 只有当两条消息间隔达到某一数值时才会显示时间信息
        if (message.msgTime - tempLastestDate > showPromptGapSec)
        {
            BTPromptEntity *prompt = [[BTPromptEntity alloc] init];
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:message.msgTime];
            prompt.message = [date promptDateString];
            [tempMessages addObject:prompt];
        }
        
        // 标记最近遍历的一条消息的时间信息
        tempLastestDate = message.msgTime;
        NSArray *array = [[self class] p_spliteMessage:message];
        [array enumerateObjectsUsingBlock:^(BTMessageEntity *obj, NSUInteger idx, BOOL *stop) {
            [self.ids addObject:@(message.msgId)];
            [tempMessages addObject:obj];
        }];
    }
        
    if ([self.showingMessages count] == 0)
    {
        [[self mutableArrayValueForKeyPath:@"showingMessages"] addObjectsFromArray:tempMessages];
        _earliestDate = tempEarliestDate;
        _lastestDate = tempLastestDate;
    }
    else
    {
        [self.showingMessages insertObjects:tempMessages atIndexes:[NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, [tempMessages count])]];
        _earliestDate = tempEarliestDate;
    }
    NSUInteger newItemCount = [self.showingMessages count];
    completion(newItemCount - itemCount, nil);
}

+(NSArray *)p_spliteMessage:(BTMessageEntity *)message
{
    NSMutableArray *messageContentArray = [[NSMutableArray alloc] init];

    // 含有图片消息
    if ([message.msgContent rangeOfString:kBTImageMessagePrefix].length > 0)
    {
        NSString *messageContent = [message msgContent];
        // https://developer.apple.com/documentation/foundation/nsrange?language=objc
        // https://www.jianshu.com/p/3e4220b2f51b
        // 此种情况说明只有一张图片
        if (
            [messageContent rangeOfString:kBTImageMessagePrefix].length > 0
            &&
            [messageContent rangeOfString:kBTImageLocal].length > 0
            &&
            [messageContent rangeOfString:kBTImageUrl].length > 0
        )
        {
            BTMessageEntity *messageEntity = [[BTMessageEntity alloc] initWithMsgId:[BTMessageModule generateMessageId]
                                                                            msgType:message.msgType
                                                                            msgTime:message.msgTime
                                                                          sessionId:message.sessionId
                                                                           senderId:message.senderId
                                                                         msgContent:messageContent
                                                                           toUserId:message.toUserId];
            messageEntity.msgContentType = MSG_TYPE_IMAGE;
            messageEntity.state = MSG_SEND_SUCCESS;
        }
        else
        {
            // 这种情况说明是图文混排
            // 首先将消息按照图片前缀拆分，
            NSArray *tempMessageContent = [messageContent componentsSeparatedByString:kBTImageMessagePrefix];
            [tempMessageContent enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                NSString *content = (NSString *)obj;
                // 如果图片在原始消息中位于开头，则上述拆分后的数组元素的第一个元素会是一个空串
                // 这里可以跳过空串
                if ([content length] > 0)
                {
                    // 在这部分消息中找到图片结尾
                    NSRange suffixRange = [content rangeOfString:kBTImageMessageSuffix];
                    if (suffixRange.length > 0)
                    {
                        // 该部分至少含有一张图片
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
                        
                        
                        NSString* secondComponent = [content substringFromIndex:suffixRange.location + suffixRange.length];
                        // 图片之后还有内容
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
                        
                        BTMessageEntity *messageEntity = [[BTMessageEntity alloc] initWithMsgId:[BTMessageModule generateMessageId]
                                                                                        msgType:message.msgType
                                                                                        msgTime:message.msgTime
                                                                                      sessionId:message.sessionId
                                                                                       senderId:message.senderId
                                                                                     msgContent:content
                                                                                       toUserId:message.toUserId];
                        messageEntity.state = MSG_SEND_SUCCESS;
                        [messageContentArray addObject:messageEntity];
                    }
                }
            }];
        }
    }
   
    if ([messageContentArray count] == 0)
    {
        [messageContentArray addObject:message];
    }
    
    return messageContentArray;
        
}

-(NSInteger)getMaxMsgId:(NSArray *)array
{
    __block NSInteger maxMsgId = 0;
    [array enumerateObjectsUsingBlock:^(BTMessageEntity *obj, NSUInteger idx, BOOL *stop) {
        if ([obj isKindOfClass:[BTMessageEntity class]])
        {
            // 只关注100万以内的数据
            if (obj.msgId > maxMsgId && obj.msgId < LOCAL_MSG_BEGIN_ID)
            {
                maxMsgId = obj.msgId;
            }
        }
    }];
    return maxMsgId;
}

-(BOOL)p_isHaveMissMsg:(NSArray *)messages
{
    // 最大值和最小值都用最大值初始化
    // 在遍历的过程中一步步调整 minMsgId 得到真实值
    // 注意只关注 msgId 在100万以内的消息
    __block NSInteger maxMsgId = [self getMaxMsgId:messages];
    __block NSInteger minMsgId = [self getMaxMsgId:messages];;
    [messages enumerateObjectsUsingBlock:^(BTMessageEntity *obj, NSUInteger idx, BOOL *stop) {
        if (obj.msgId > maxMsgId && obj.msgId < LOCAL_MSG_BEGIN_ID)
        {
            // maxMsgId = obj.msgId;
        }
        else if (obj.msgId < minMsgId && obj.msgId < LOCAL_MSG_BEGIN_ID)
        {
            minMsgId = obj.msgId;
        }
    }];
    
    // NSUInteger maxMsgId = [msgIds valueForKeyPath:@"@max"];
    // NSUInteger minMsgId = [msgIds valueForKeyPath:@"@min"];
 
    NSUInteger diff = maxMsgId - minMsgId;
    if (diff + 1 != [messages count])
    {
        return YES;
    }
    return NO;
}

-(void)checkMsgList:(BTChatLoadMoreHistoryCompletion)completion
{
    // 为了显示而加载到内存中的消息
    NSMutableArray *tmp = [NSMutableArray arrayWithArray:self.showingMessages];
    [tmp enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        
        if ([obj isKindOfClass:[BTPromptEntity class]])
        {
            // 移除”临时消息“，这里的“临时消息”是指时间提示、系统提示等非真实消息内容
            [tmp removeObject:obj];
        }
        else
        {
            // TODO: 这里仅仅显示了 msgId 为 LOCAL_MSG_BEGIN_ID（目前为100万） 以内的消息
            BTMessageEntity *msg = obj;
            if (msg.msgId >= LOCAL_MSG_BEGIN_ID)
            {
                [tmp removeObject:obj];
            }
        }
    }];
    
    [tmp enumerateObjectsUsingBlock:^(BTMessageEntity *obj, NSUInteger idx, BOOL *stop) {
        // 也就是说最后一条消息是不会遍历到的
        if (idx + 1 < [tmp count])
        {
            BTMessageEntity *msg = [tmp objectAtIndex:idx + 1];
            // 比较当前消息的 id 和下一条消息的 id 是否相差1
            // 如果不满足的话需要从服务端拉取缺少的消息
            if (abs(obj.msgId - msg.msgId) != 1)
            {
                [self loadHistoryMessageFromServer:MIN(obj.msgId, msg.msgId) loadCount:abs(obj.msgId - msg.msgId) completion:^(NSUInteger addCount, NSError *error) {
                    completion(addCount, error);
                }];
            }
        }
    }];
}

@end


@implementation BTPromptEntity

@end
