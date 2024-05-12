//
//  BTHeartbeatAPI.m
//

#import "BTHeartbeatAPI.h"
#import "IMOther.pbobjc.h"


@implementation BTHeartbeatAPI

-(int)requestTimeOutTimeInterval
{
    return 0;
}

-(int)requestServiceID
{
    return DDHEARTBEAT_SID;
}

-(int)responseServiceID
{
    return DDHEARTBEAT_SID;
}

-(int)requestCommandID
{
    return REQ_CID;
}

-(int)responseCommandID
{
    return RES_CID;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, UInt32 seqNo) {
        IMHeartBeat *heartBeat = [[IMHeartBeat alloc] init];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:DDHEARTBEAT_SID
                                               commandID:REQ_CID
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[heartBeat data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
