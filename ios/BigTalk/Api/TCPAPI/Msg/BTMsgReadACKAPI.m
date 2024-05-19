//
//  BTMsgReadACKAPI.m
//

#import "BTMsgReadACKAPI.h"
#import "IMMessage.pbobjc.h"


@implementation BTMsgReadACKAPI

-(int)requestTimeOutTimeInterval
{
    return 0;
}

-(int)requestServiceID
{
    return SID_MESSAGE;
}

-(int)requestCommandID
{
    return CID_MSG_READ_ACK;
}

-(int)responseServiceID
{
    return 0;
}

-(int)responseCommandID
{
    return 0;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo)
    {
        NSArray *array = (NSArray *)object;
        IMMsgDataReadAck *readAck = [[IMMsgDataReadAck alloc] init];
        [readAck setUserId:0];
        [readAck setSessionId:[BTRuntime convertLocalIdToPbId:array[0]]];
        [readAck setMsgId:[array[1] integerValue]];
        [readAck setSessionType:[array[2] integerValue]];
        
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_MESSAGE
                                               commandID:CID_MSG_READ_ACK
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[readAck data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
