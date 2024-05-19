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
    return SID_LOGIN;
}

-(int)requestCommandID
{
    return CID_LOGOUT_REQ;
}

-(int)responseServiceID
{
    return SID_LOGIN;
}

-(int)responseCommandID
{
    return CID_LOGOUT_RES;
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
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_LOGIN
                                               commandID:CID_LOGOUT_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[logoutReq data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
