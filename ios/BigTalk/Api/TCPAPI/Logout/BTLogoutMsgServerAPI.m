//
//  BTLogoutMsgServerAPI.m
//

#import "BTLogoutMsgServerAPI.h"
#import "IMLogin.pbobjc.h"


@implementation BTLogoutMsgServerAPI

-(int)requestTimeOutTimeInterval
{
    return 5;
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
    return CID_LOGIN_REQ_LOGINOUT;
}

-(int)responseCommandID
{
    return CID_LOGIN_RES_LOGINOUT;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMLogoutRsp* rsp = [IMLogoutRsp parseFromData:data error:nil];
        int isok = rsp.resultCode;
        return isok;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo)
    {
        IMLogoutReq *logoutReq = [[IMLogoutReq alloc] init];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:DDSERVICE_LOGIN
                                               commandID:CID_LOGIN_REQ_LOGINOUT
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[logoutReq data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
