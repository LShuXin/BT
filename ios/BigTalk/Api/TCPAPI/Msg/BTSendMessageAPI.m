//
//  BTSendMessageAPI.m
//

#import "BTSendMessageAPI.h"
#import "IMMessage.pbobjc.h"


@implementation BTSendMessageAPI

-(int)requestTimeOutTimeInterval
{
    return 10;
}

-(int)requestServiceID
{
    return SID_MESSAGE;
}

-(int)requestCommandID
{
    return CID_MSG_DATA;
}

-(int)responseServiceID
{
    return SID_MESSAGE;
}

-(int)responseCommandID
{
    return CID_MSG_DATA_ACK;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMMsgDataAck *msgDataAck = [IMMsgDataAck parseFromData:data error:nil];
        return @[@(msgDataAck.msgId), @(msgDataAck.sessionId)];
    };
    return analysis;
}

- (Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        /**
         index0: fromId
         index1: toId,
         index2: messageSeqNo,
         index3: messageType,
         index4: messageSenderType,
         index5: messageContent,
         index6: messageAttachContent
         */
        NSArray *array = (NSArray *)object;
        NSString *fromId = array[0];
        NSString *toId = array[1];
        NSData *content = array[2];
        MsgType type = [array[3] intValue];
        
        IMMsgData *msgdata = [[IMMsgData alloc] init];
        [msgdata setFromUserId:0];
        [msgdata setToSessionId:[BTRuntime convertLocalIdToPbId:toId]];
        [msgdata setMsgData:content];
        [msgdata setMsgType:type];
        [msgdata setMsgId:0];
        [msgdata setCreateTime:0];
        
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_MESSAGE
                                               commandID:CID_MSG_DATA
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:msgdata.data];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}

@end
