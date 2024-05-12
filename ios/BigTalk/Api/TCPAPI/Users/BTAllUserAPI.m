//
//  BTAllUserAPI.m
//

#import "BTAllUserAPI.h"
#import "BTUserEntity.h"
#import "IMBuddy.pbobjc.h"


@implementation BTAllUserAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return MODULE_ID_SESSION;
}

-(int)responseServiceID
{
    return MODULE_ID_SESSION;
}

-(int)requestCommandID
{
    return CMD_FRI_ALL_USER_REQ;
}

-(int)responseCommandID
{
    return CMD_FRI_ALL_USER_RES;
}

-(Analysis)analysisReturnData
{
    
    Analysis analysis = (id)^(NSData *data) {
        IMAllUserRsp *allUserRsp = [IMAllUserRsp parseFromData:data error:nil];
        uint32_t alllastupdatetime = allUserRsp.latestUpdateTime;
        NSMutableDictionary *userAndVersion = [NSMutableDictionary new];
        [userAndVersion setObject:@(alllastupdatetime) forKey:@"alllastupdatetime"];
        NSMutableArray *userList = [[NSMutableArray alloc] init];
        for (UserInfo *userInfo in allUserRsp.userListArray)
        {
            BTUserEntity *user = [[BTUserEntity alloc] initWithPbData:userInfo];
            [userList addObject:user];
        }

        [userAndVersion setObject:userList forKey:@"userlist"];
        return userAndVersion;
    };
    return analysis;
}

- (Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo) {
        NSArray *array = (NSArray *)object;
        IMAllUserReq *reqBuilder = [[IMAllUserReq alloc] init];
        NSInteger version = [array[0] integerValue];
        [reqBuilder setUserId:0];
        [reqBuilder setLatestUpdateTime:version];

        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:MODULE_ID_SESSION
                                               commandID:CMD_FRI_ALL_USER_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[reqBuilder data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
