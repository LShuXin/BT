//
//  BTUserDetailInfoAPI.m
//

#import "BTUserDetailInfoAPI.h"
#import "BTRuntimeStatus.h"
#import "BTUserEntity.h"
#import "IMBuddy.pbobjc.h"


@implementation BTUserDetailInfoAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return SID_MESSAGE;
}

-(int)requestCommandID
{
    return 18;
}

-(int)responseServiceID
{
    return SID_MESSAGE;
}

-(int)responseCommandID
{
    return 19;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMUsersInfoRsp *rsp = [IMUsersInfoRsp parseFromData:data error:nil];
        NSMutableArray *userList = [[NSMutableArray alloc] init];
        for (UserInfo *userInfo in rsp.userInfoListArray)
        {
            BTUserEntity *user = [[BTUserEntity alloc] initWithPbData:userInfo];
            [userList addObject:user];
        }
        return userList;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *users = (NSArray *)object;
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        IMUsersInfoReq *userInfoReq = [[IMUsersInfoReq alloc] init];
        [userInfoReq setUserId:0];
        [userInfoReq setUserIdListArray:users];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_MESSAGE
                                               commandID:18
                                                   seqNo:seqNo];
        [outputStream writeBytes:[userInfoReq data]];
        return [outputStream toByteArray];
    };
    return package;
}
@end
