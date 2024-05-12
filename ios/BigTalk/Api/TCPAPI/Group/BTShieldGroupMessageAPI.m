//
//  ShieldGroupMessageAPI.m
//

#import "BTShieldGroupMessageAPI.h"
#import "IMGroup.pbobjc.h"


@implementation BTShieldGroupMessageAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return SERVICE_GROUP;
}

-(int)responseServiceID
{
    return SERVICE_GROUP;
}

-(int)requestCommandID
{
    return CID_GROUP_SHIELD_GROUP_REQUEST;
}

-(int)responseCommandID
{
    return CID_GROUP_SHIELD_GROUP_RESPONSE;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMGroupShieldRsp *groupShieldRsp = [IMGroupShieldRsp parseFromData:data error:nil];
        return groupShieldRsp.resultCode;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        UInt32 groupID = [BTRuntime convertLocalIdToPbId:array[0]];
        uint32_t isShield = [array[1] intValue];
        IMGroupShieldReq *groupShield = [[IMGroupShieldReq alloc] init];
        [groupShield setUserId:0];
        [groupShield setGroupId:groupID];
        [groupShield setShieldStatus:isShield];
        
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SERVICE_GROUP
                                               commandID:CID_GROUP_SHIELD_GROUP_REQUEST
                                  seqNo:seqNo];
        [outputStream directWriteBytes:[groupShield data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
