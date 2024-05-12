//
//  BTMessageEntity.h
//

#import <Foundation/Foundation.h>
#import "IMBaseDefine.pbobjc.h"


@class BTChattingModule;
@class BTDataInputStream;
@class IMMsgData;

typedef NS_ENUM(NSUInteger, BTMessageType)
{
    MSG_TYPE_SINGLE     = 1,             // 单个人会话消息
    MSG_TYPE_TEMP_GROUP = 2,             // 临时群消息
};

typedef NS_ENUM(NSUInteger, BTMessageContentType)
{
    MSG_TYPE_TEXT        = 0,
    MSG_TYPE_IMAGE       = 1,
    MSG_TYPE_VOICE       = 2,
    MSG_TYPE_EMOTION     = 3,
    MSG_TYPE_AUDIO	     = 100,
    MSG_TYPE_GROUP_AUDIO = 101,
};

typedef NS_ENUM(NSUInteger, BTMessageState)
{
    MSG_SENDING      = 0,
    MSG_SEND_FAILURE = 1,
    MSG_SEND_SUCCESS = 2
};


#define kBTImageMessagePrefix             @"&$#@~^@[{:"
#define kBTImageMessageSuffix             @":}]&$~@#@"

#define kBTVoiceLength                    @"voiceLength"
#define kBTVoicePlayed                    @"voicePlayed"

#define kBTImageLocal                     @"local"
#define kBTImageUrl                       @"url"


@interface BTMessageEntity : NSObject

@property(assign)NSUInteger  msgId;                        // messageId
@property(nonatomic,assign)MsgType msgType;                // 消息类型
@property(nonatomic,assign)NSTimeInterval msgTime;         // 消息收发时间
@property(nonatomic,strong)NSString *sessionId;            // 会话id
@property(assign)NSUInteger seqNo;
@property(nonatomic,strong)NSString *senderId;             // 发送者的Id, 群聊天表示发送者id
@property(nonatomic,strong)NSString *msgContent;           // 消息内容, 若为非文本消息则是json
@property(nonatomic,strong)NSString *toUserId;             // 发消息的用户ID
@property(nonatomic,strong)NSMutableDictionary *info;      // 一些附属的属性，包括语音时长
@property(assign)BTMessageContentType msgContentType;
@property(nonatomic,strong)NSString *attach;
@property(assign)SessionType sessionType;
@property(nonatomic,assign)BTMessageState state;           // 消息发送状态

-(BTMessageEntity *)initWithMsgId:(NSUInteger)idUInt
                          msgType:(MsgType)msgType
                          msgTime:(NSTimeInterval)msgTime
                        sessionId:(NSString *)sessionId
                         senderId:(NSString *)senderId
                       msgContent:(NSString *)msgContent
                         toUserId:(NSString *)toUserId;

+(BTMessageEntity *)makeMessage:(NSString *)content
                 withChatModule:(BTChattingModule *)chatModule
                        msgType:(BTMessageContentType)type;

+(BTMessageEntity *)makeMessageFromStream:(BTDataInputStream *)bodyData;
-(BOOL)isGroupMessage;
-(SessionType)getMessageSessionType;
-(BOOL)isImageMessage;
-(BOOL)isVoiceMessage;
-(BOOL)isSendBySelf;
+(BTMessageEntity *)makeMessageFromPb:(MsgInfo *)info sessionType:(SessionType)sessionType;
+(BTMessageEntity *)makeMessageFromPbData:(IMMsgData *)data;

@end
