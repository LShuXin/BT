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
    return SID_HEARTBEAT;
}

-(int)requestCommandID
{
    return CID_HEARTBEAT_REQ;
}

-(int)responseServiceID
{
    return SID_HEARTBEAT;
}

-(int)responseCommandID
{
    return CID_HEARTBEAT_RES;
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
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_HEARTBEAT
                                               commandID:CID_HEARTBEAT_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[heartBeat data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
