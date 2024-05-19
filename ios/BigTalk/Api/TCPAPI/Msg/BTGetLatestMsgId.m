//
//  BTGetLatestMsgId.m
//

#import "BTGetLatestMsgId.h"
#import "IMMessage.pbobjc.h"


@implementation BTGetLatestMsgId

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return SID_MESSAGE;
}

-(int)requestCommandID
{
    return CID_LASTEST_MSG_ID_REQ;
}

-(int)responseServiceID
{
    return SID_MESSAGE;
}

-(int)responseCommandID
{
    return CID_LASTEST_MSG_ID_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMGetLatestMsgIdRsp *latestMsgIdRsp = [IMGetLatestMsgIdRsp parseFromData:data error:nil];
        return latestMsgIdRsp.latestMsgId;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        SessionType sessionType = (SessionType)[array[0] integerValue];
        NSInteger sessionId = [BTRuntime convertLocalIdToPbId:array[1]];
        
        IMGetLatestMsgIdReq *req = [[IMGetLatestMsgIdReq alloc] init];
        [req setUserId:0];
        [req setSessionType:sessionType];
        [req setSessionId:sessionId];
        
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_MESSAGE
                                               commandID:CID_LASTEST_MSG_ID_REQ
                                  seqNo:seqNo];
        [outputStream directWriteBytes:[req data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
