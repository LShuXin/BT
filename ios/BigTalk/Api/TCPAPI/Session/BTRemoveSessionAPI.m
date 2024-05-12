//
//  BTRemoveSessionAPI.m
//

#import "BTRemoveSessionAPI.h"
#import "IMBuddy.pbobjc.h"


@implementation BTRemoveSessionAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return MODULE_ID_SESSION;
}

-(int)responseServiceID
{
    return MODULE_ID_SESSION;
}

-(int)requestCommandID
{
    return MODULE_ID_SESSION;
}

-(int)responseCommandID
{
    return REMOVE_SESSION_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData* data)
    {
        return nil;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo)
    {
        NSArray *array = (NSArray *)object;
        NSString *sessionId = array[0];
        SessionType sessionType = [array[1] intValue];
        IMRemoveSessionReq *removeSession = [[IMRemoveSessionReq alloc] init];
        [removeSession setUserId:0];
        [removeSession setSessionId:[BTRuntime convertLocalIdToPbId:sessionId]];
        [removeSession setSessionType:sessionType];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:MODULE_ID_SESSION
                                               commandID:REMOVE_SESSION_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[removeSession data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
