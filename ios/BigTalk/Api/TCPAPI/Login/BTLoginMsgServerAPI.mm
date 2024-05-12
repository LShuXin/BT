//
//  BTLoginAPI.m
//

#import "BTLoginMsgServerAPI.h"
#import "BTUserEntity.h"
#import "IMLogin.pbobjc.h"
#import "IMBaseDefine.pbobjc.h"
#import "NSString+BTAdditions.h"
#import "security.h"


@implementation BTLoginMsgServerAPI

-(int)requestTimeOutTimeInterval
{
    return 15;
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
    return DDCMD_LOGIN_REQ_USERLOGIN;
}

-(int)responseCommandID
{
    return DDCMD_LOGIN_RES_USERLOGIN;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMLoginRes *res = [IMLoginRes parseFromData:data error:nil];
        NSInteger serverTime = res.serverTime;
        NSInteger loginResult = res.resultCode;
        NSString *resultString = nil;
        resultString = res.resultString;
        NSDictionary *result = nil;
        if (loginResult != 0)
        {
            return result;
        }
        else
        {
            BTUserEntity *user = [[BTUserEntity alloc] initWithPbData:res.userInfo];
            result = @{
                @"serverTime": @(serverTime),
                @"result": resultString,
                @"user": user,
            };
            return result;
        }
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo) {
        NSArray *array = (NSArray *)object;
        NSString *clientVersion = [NSString stringWithFormat:@"MAC/%@-%@",
                                   [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleShortVersionString"],
                                   [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"]];
       
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:DDSERVICE_LOGIN
                                               commandID:DDCMD_LOGIN_REQ_USERLOGIN
                                                   seqNo:seqNo];

        IMLoginReq *login = [[IMLoginReq alloc] init];
        [login setUserName:array[0]];
        [login setPassword:[array[1] MD5]];
        [login setClientType:ClientType_ClientTypeIos];
        [login setClientVersion:clientVersion];
        [login setOnlineStatus:UserStatType_UserStatusOnline];
        [outputStream directWriteBytes:[login data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
