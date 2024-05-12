//
//  BTSendPushTokenAPI.m
//

#import "BTSendPushTokenAPI.h"
#import "BTRuntimeStatus.h"
#import "IMLogin.pbobjc.h"


@implementation BTSendPushTokenAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return DDSERVICE_LOGIN;
}

-(int)responseServiceID
{
    return DDSERVICE_LOGIN;
}

-(int)requestCommandID
{
    return CMD_PUSH_TOKEN_REQ;
}

-(int)responseCommandID
{
    return CMD_PUSH_TOKEN_RES;
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
        NSString *token = (NSString *)object;
        IMDeviceTokenReq *deviceToken = [[IMDeviceTokenReq alloc] init];
        [deviceToken setUserId:[BTUserEntity localUserIdToPbUserId:BTRuntime.user.objId]];
        [deviceToken setDeviceToken:token];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:DDSERVICE_LOGIN
                                               commandID:CMD_PUSH_TOKEN_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[deviceToken data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}

@end
