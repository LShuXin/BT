//
//  BTMessageEntity.m
//

#import "BTMessageEntity.h"
#import "BTUserModule.h"
#import "BTEmotionsModule.h"
#import "BTChattingModule.h"
#import "BTEmotionsModule.h"
#import "BTMessageModule.h"
#import "Encapsulator.h"
#import "BTDataInputStream.h"
#import "BTRuntimeStatus.h"
#import "IMMessage.pbobjc.h"
#import "security.h"


@implementation BTMessageEntity

-(BTMessageEntity *)initWithMsgId:(NSUInteger)idUInt
                          msgType:(MsgType)msgType
                          msgTime:(double)msgTime
                        sessionId:(NSString *)sessionId
                         senderId:(NSString *)senderId
                       msgContent:(NSString *)msgContent
                         toUserId:(NSString *)toUserId
{
    self = [super init];
    if (self)
    {
        _msgId = idUInt;
        _msgType = msgType;
        _msgTime = msgTime;
        _sessionId = [sessionId copy];
        _senderId = [senderId copy];
        _msgContent = msgContent;
        _toUserId = [toUserId copy];
        _info = [[NSMutableDictionary alloc] init];
    }
    return self;
}

// copyWithZone 方法用于创建对象的副本。它是一种遵循 NSCopying 协议的类的必备方法。
-(id)copyWithZone:(NSZone *)zone
{
    BTMessageEntity *msgEntity = [[[self class] allocWithZone:zone] initWithMsgId:_msgId
                                                                          msgType:_msgType
                                                                          msgTime:_msgTime
                                                                        sessionId:_sessionId
                                                                         senderId:_senderId
                                                                       msgContent:_msgContent
                                                                         toUserId:_toUserId];
    return msgEntity;
}


#pragma mark - privateAPI
// 删除文本消息中的表情字符串
-(NSString *)getNewMessageContentFromContent:(NSString *)content
{
    NSMutableString *msgContent = [NSMutableString stringWithString: content ? content : @""];
    NSMutableString *resultContent = [NSMutableString string];
    NSRange startRange;
//    NSDictionary *emotionDic = [BTEmotionsModule shareInstance].emotionUnicodeDic;
    while ((startRange = [msgContent rangeOfString:@"["]).location != NSNotFound)
    {
        if (startRange.location > 0)
        {
            NSString *str = [msgContent substringWithRange:NSMakeRange(0, startRange.location)];
            BTLog(@"[前文本内容:%@", str);
            [msgContent deleteCharactersInRange:NSMakeRange(0, startRange.location)];
            startRange.location = 0;
            [resultContent appendString: str];
        }
        
        NSRange endRange = [msgContent rangeOfString:@"]"];
        if (endRange.location != NSNotFound)
        {
            NSRange range;
            range.location = 0;
            range.length = endRange.location + endRange.length;
            NSString *emotionText = [msgContent substringWithRange:range];
            [msgContent deleteCharactersInRange: NSMakeRange(0, endRange.location + endRange.length)];
            
            BTLog(@"类似表情字串:%@", emotionText);
//            NSString *emotion = emotionDic[emotionText];
//            if (emotion) {
//                // 表情
//                [resultContent appendString:emotion];
//            } else
//            {
//                [resultContent appendString:emotionText];
//            }
        }
        else
        {
            BTLog(@"没有[匹配的后缀");
            break;
        }
    }
    
    if ([msgContent length] > 0)
    {
        [resultContent appendString:msgContent];
    }
    return resultContent;
}

+(BTMessageEntity *)makeMessage:(NSString *)content
                 withChatModule:(BTChattingModule *)chatModule
                        msgType:(BTMessageContentType)type
{
    double msgTime = [[NSDate date] timeIntervalSince1970];
    NSString *senderId = [BTRuntimeStatus instance].user.objId;
    
    MsgType msgType;
    if (chatModule.sessionEntity.sessionType == SessionType_SessionTypeSingle)
    {
        msgType = MsgType_MsgTypeSingleText;
    }
    else
    {
        msgType = MsgType_MsgTypeGroupText;
    }
    
    BTMessageEntity *message = [[BTMessageEntity alloc] initWithMsgId:[BTMessageModule generateMessageId]
                                                              msgType:msgType
                                                              msgTime:msgTime
                                                            sessionId:chatModule.sessionEntity.sessionId
                                                             senderId:senderId
                                                           msgContent:content
                                                             toUserId:chatModule.sessionEntity.sessionId];
    message.state = MSG_SENDING;
    message.msgContentType = type;
    
    [chatModule addShowMessage:message];
    [chatModule updateSessionUpdateTime:message.msgTime];
    return message;
}

-(BOOL)isGroupMessage
{
    if (self.msgType == MsgType_MsgTypeGroupAudio || self.msgType == MsgType_MsgTypeGroupText)
    {
        return YES;
    }
    return NO;
}

-(SessionType)getMessageSessionType
{
    return ( ![self isGroupMessage] ? SessionType_SessionTypeSingle : SessionType_SessionTypeGroup );
}

-(BOOL)isGroupVoiceMessage
{
    if (self.msgType == MsgType_MsgTypeGroupAudio)
    {
        return YES;
    }
    return NO;
}

-(BOOL)isVoiceMessage
{
    if (self.msgType == MsgType_MsgTypeGroupAudio || self.msgType == MsgType_MsgTypeSingleAudio)
    {
        return YES;
    }
    return NO;
}

-(BOOL)isImageMessage
{
    if (self.msgContentType == MSG_TYPE_IMAGE)
    {
        return YES;
    }
    return NO;
}

-(BOOL)isSendBySelf
{
    if ([self.senderId isEqualToString:BTRuntime.user.objId])
    {
        return YES;
    }
    return NO;
}

+(BTMessageEntity *)makeMessageFromPb:(MsgInfo *)info sessionType:(SessionType)sessionType
{
    BTMessageEntity *msg = [BTMessageEntity new];
    msg.msgType = info.msgType;
    NSMutableDictionary *msgInfo = [[NSMutableDictionary alloc] init];
    
    if ([msg isVoiceMessage])
    {
        msg.msgContentType = MSG_TYPE_VOICE;
        
        NSData *data = info.msgData;
        NSData *voiceData;
        
        // 大于4是才可能是有效的语音数据
        // 前4个字节是音频长度数据
        if (data.length > 4)
        {
            voiceData = [data subdataWithRange:NSMakeRange(4, [data length] - 4)];
            NSString *filename = [NSString stringWithString:[Encapsulator defaultFileName]];
            if ([voiceData writeToFile:filename atomically:YES])
            {
                msg.msgContent = filename;
            }
            else
            {
                msg.msgContent = @"语音存储出错";
            }
            
            NSData *voiceLengthData = [data subdataWithRange:NSMakeRange(0, 4)];
            
            // 将二进制的音频长度数据装换为10进制
            int8_t ch1;
            [voiceLengthData getBytes:&ch1 range:NSMakeRange(0, 1)];
            ch1 = ch1 & 0x0ff;
            
            int8_t ch2;
            [voiceLengthData getBytes:&ch2 range:NSMakeRange(1, 1)];
            ch2 = ch2 & 0x0ff;
            
            int32_t ch3;
            [voiceLengthData getBytes:&ch3 range:NSMakeRange(2, 1)];
            ch3 = ch3 & 0x0ff;
            
            int32_t ch4;
            [voiceLengthData getBytes:&ch4 range:NSMakeRange(3, 1)];
            ch4 = ch4 & 0x0ff;
            
            if ((ch1 | ch2 | ch3 | ch4) < 0)
            {
                @throw [NSException exceptionWithName:@"Exception" reason:@"EOFException" userInfo:nil];
            }
            
            int voiceLength = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            [msgInfo setObject:@(voiceLength) forKey:kBTVoiceLength];
            // TODO: why 1
            [msgInfo setObject:@(1) forKey:kBTVoicePlayed];
        }
    }
    else
    {
        // 非音频消息，文本消息（包括表情文本和正常文本）
        NSString *tmpStr = [[NSString alloc] initWithData:info.msgData encoding:NSUTF8StringEncoding];
        
        char* pOut;
        uint32_t nOutLen;
        uint32_t nInLen = strlen([tmpStr cStringUsingEncoding:NSUTF8StringEncoding]);
        int nRet = DecryptMsg([tmpStr cStringUsingEncoding:NSUTF8StringEncoding], nInLen, &pOut, nOutLen);
        if (nRet == 0)
        {
            msg.msgContent = [NSString stringWithCString:pOut encoding:NSUTF8StringEncoding];
            Free(pOut);
        }
        else
        {
            msg.msgContent = @"";
        }
    }
    
    if ([[self class] isEmotionMsg:msg.msgContent])
    {
        msg.msgContentType = MSG_TYPE_EMOTION;
    }
    
    msg.sessionId = [BTRuntime convertPbIdToLocalId:info.fromSessionId sessionType:sessionType];
    msg.msgId = info.msgId;
    msg.toUserId = BTRuntime.user.objId;
    msg.senderId = [BTRuntime convertPbIdToLocalId:info.fromSessionId sessionType:SessionType_SessionTypeSingle];
    msg.msgTime = info.createTime;
    msg.info = msgInfo;
    return msg;
}

// TODO: why
// converts a hexadecimal string representation into an NSData object
+(NSData *)hexStringToData:(NSString *)string
{
    NSString *command = string;
    command = [command stringByReplacingOccurrencesOfString:@" " withString:@""];
    NSMutableData *commandToSend = [[NSMutableData alloc] init];
    unsigned char whole_byte;
    char byte_chars[3] = {'\0', '\0', '\0'};
    int i;
    for (i = 0; i < [command length] / 2; i++)
    {
        byte_chars[0] = [command characterAtIndex:i * 2];
        byte_chars[1] = [command characterAtIndex:i * 2 + 1];
        whole_byte = strtol(byte_chars, NULL, 16);
        [commandToSend appendBytes:&whole_byte length:1];
    }
    return commandToSend;
}

+(BTMessageEntity *)makeMessageFromPbData:(IMMsgData *)data
{
    BTMessageEntity *msg = [BTMessageEntity new];
    msg.msgType = data.msgType;
    SessionType type = [msg isGroupMessage] ? SessionType_SessionTypeGroup : SessionType_SessionTypeSingle;
    msg.sessionType = type;
    NSMutableDictionary *msgInfo = [[NSMutableDictionary alloc] init];
    if ([msg isVoiceMessage])
    {
        msg.msgContentType = MSG_TYPE_VOICE;
        NSData *tempdata = data.msgData;
        NSData *voiceData = [tempdata subdataWithRange:NSMakeRange(4, [tempdata length] - 4)];
        NSString *filename = [NSString stringWithString:[Encapsulator defaultFileName]];
        if ([voiceData writeToFile:filename atomically:YES])
        {
            msg.msgContent = filename;
        }
        else
        {
            msg.msgContent = @"语音存储出错";
        }
        NSData *voiceLengthData = [tempdata subdataWithRange:NSMakeRange(0, 4)];
        
        int8_t ch1;
        [voiceLengthData getBytes:&ch1 range:NSMakeRange(0, 1)];
        ch1 = ch1 & 0x0ff;
        
        int8_t ch2;
        [voiceLengthData getBytes:&ch2 range:NSMakeRange(1, 1)];
        ch2 = ch2 & 0x0ff;
        
        int32_t ch3;
        [voiceLengthData getBytes:&ch3 range:NSMakeRange(2, 1)];
        ch3 = ch3 & 0x0ff;
        
        int32_t ch4;
        [voiceLengthData getBytes:&ch4 range:NSMakeRange(3, 1)];
        ch4 = ch4 & 0x0ff;
        
        if ((ch1 | ch2 | ch3 | ch4) < 0)
        {
            @throw [NSException exceptionWithName:@"Exception" reason:@"EOFException" userInfo:nil];
        }
        int voiceLength = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        [msgInfo setObject:@(voiceLength) forKey:kBTVoiceLength];
        // TODO: sometimes was set to 1, why
        [msgInfo setObject:@(0) forKey:kBTVoicePlayed];
    }
    else
    {
        // 文本消息（表情文本或正常文本）
        NSString *tmpStr = [[NSString alloc] initWithData:data.msgData encoding:NSUTF8StringEncoding];

        char *pOut;
        uint32_t nOutLen;
        uint32_t nInLen = strlen([tmpStr cStringUsingEncoding:NSUTF8StringEncoding]);
        DecryptMsg([tmpStr cStringUsingEncoding:NSUTF8StringEncoding], nInLen, &pOut, nOutLen);
        msg.msgContent = [NSString stringWithCString:pOut encoding:NSUTF8StringEncoding];
        Free(pOut);
    }
    
    if (msg.sessionType == SessionType_SessionTypeSingle)
    {
        msg.sessionId = [BTRuntime convertPbIdToLocalId:data.fromUserId sessionType:type];
    }
    else
    {
        msg.sessionId = [BTRuntime convertPbIdToLocalId:data.toSessionId sessionType:type];
    }
    
    if ([[self class] isEmotionMsg:msg.msgContent])
    {
        msg.msgContentType = MSG_TYPE_EMOTION;
    }
    
    msg.msgId = data.msgId;
    msg.toUserId = msg.sessionId;
    msg.senderId = [BTRuntime convertPbIdToLocalId:data.fromUserId sessionType:SessionType_SessionTypeSingle];
    if ([msg.senderId isEqual:BTRuntime.user.objId])
    {
        msg.sessionId = [BTRuntime convertPbIdToLocalId:data.toSessionId sessionType:type];
    }
    msg.msgTime = data.createTime;
    msg.info = msgInfo;
    return msg;
}

+(BOOL)isEmotionMsg:(NSString *)content
{
    return [[[BTEmotionsModule shareInstance].emotionUnicodeDic allKeys] containsObject:content];
}

+(BTMessageEntity *)makeMessageFromStream:(BTDataInputStream *)bodyData
{
    return nil;
}

@end
