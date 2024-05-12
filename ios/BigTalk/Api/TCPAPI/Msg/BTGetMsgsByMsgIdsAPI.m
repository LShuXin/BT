//
//  BTGetMsgsByMsgIdsAPI.m
//

#import "BTGetMsgsByMsgIdsAPI.h"
#import "IMMessage.pbobjc.h"


@implementation BTGetMsgsByMsgIdsAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
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
    return GET_MSG_BY_IDS_REQ;
}

-(int)responseCommandID
{
    return GET_MSG_BY_IDS_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMGetMsgByIdRsp *msgsRsp = [IMGetMsgByIdRsp parseFromData:data error:nil];
        return msgsRsp.msgListArray;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        SessionType sesstionType = (SessionType)[array[0] integerValue];
        NSInteger sessionId = [BTRuntime convertLocalIdToPbId:array[1]];
        NSArray *ids = array[2];
        IMGetMsgByIdReq *req = [[IMGetMsgByIdReq alloc] init];
        [req setUserId:0];
        [req setSessionType:sesstionType];
        [req setSessionId:sessionId];
        [req setMsgIdListArray:ids];
        
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:DDSERVICE_MESSAGE
                                               commandID:GET_MSG_BY_IDS_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[req data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
