//
//  BTReceiveMessageACKAPI.m
//

#import "BTReceiveMessageACKAPI.h"
#import "IMMessage.pbobjc.h"


@implementation BTReceiveMessageACKAPI

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
    return CID_MSG_DATA_ACK;
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
      
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo) {
        NSArray *array = (NSArray *)object;
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        IMMsgDataAck *dataAck = [[IMMsgDataAck alloc] init];
        [dataAck setUserId:0];
        [dataAck setMsgId:[array[1] intValue]];
        [dataAck setSessionId:[BTRuntime convertLocalIdToPbId:array[2]]];
        [dataAck setSessionType:[array[3] integerValue]];

        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_MESSAGE
                                               commandID:CID_MSG_DATA_ACK
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[dataAck data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
