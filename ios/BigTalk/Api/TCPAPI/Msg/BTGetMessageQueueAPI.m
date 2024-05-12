//
//  BTGetMessageQueueAPI.m
//

#import "BTGetMessageQueueAPI.h"
#import "BTMessageEntity.h"
#import "BTRuntimeStatus.h"
#import "Encapsulator.h"
#import "IMMessage.pbobjc.h"


@implementation BTGetMessageQueueAPI

-(int)requestTimeOutTimeInterval
{
    return 20;
}

-(int)requestServiceID
{
    return DDSERVICE_MESSAGE;
}

-(int)responseServiceID
{
    return DDSERVICE_MESSAGE;
}

-(int)requestCommandID
{
    return CID_MSG_LIST_REQUEST;
}

-(int)responseCommandID
{
    return CID_MSG_LIST_RESPONSE;
}

-(Analysis)analysisReturnData
{
   
    Analysis analysis = (id)^(NSData *data) {
        IMGetMsgListRsp *rsp = [IMGetMsgListRsp parseFromData:data error:nil];
        SessionType sessionType = rsp.sessionType;

        NSString *sessionId = [BTRuntime convertPbIdToLocalId:rsp.sessionId sessionType:sessionType];
        NSMutableArray *msgArray = [NSMutableArray new];
        for (MsgInfo *msgInfo in rsp.msgListArray)
        {
            BTMessageEntity *msg = [BTMessageEntity makeMessageFromPb:msgInfo sessionType:sessionType];
            msg.sessionId = sessionId;
            msg.state = MSG_SEND_SUCCESS;
            [msgArray addObject:msg];
        }
        
        return msgArray;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        IMGetMsgListReq *getMsgListReq = [[IMGetMsgListReq alloc] init];
        [getMsgListReq setMsgIdBegin:[array[0] integerValue]];
        [getMsgListReq setUserId:0];
        [getMsgListReq setMsgCnt:[array[1] integerValue]];
        [getMsgListReq setSessionType: [array[2] integerValue]];
        [getMsgListReq setSessionId:[BTRuntime convertLocalIdToPbId:array[3]]];
       
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:DDSERVICE_MESSAGE
                                               commandID:CID_MSG_LIST_REQUEST
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[getMsgListReq data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}

@end
