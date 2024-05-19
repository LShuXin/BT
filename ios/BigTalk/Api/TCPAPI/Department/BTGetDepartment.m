//
//  BTGetDepartment.m
//

#import "BTGetDepartment.h"
#import "IMBuddy.pbobjc.h"


@implementation BTGetDepartment

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return SID_SESSION;
}

-(int)requestCommandID
{
    return CID_DEPARTINFO_REQ;
}

-(int)responseServiceID
{
    return SID_SESSION;
}

-(int)responseCommandID
{
    return CID_DEPARTINFO_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMDepartmentRsp *rsp =[IMDepartmentRsp parseFromData:data error:nil];
        NSDictionary *dic = nil;
        if (rsp.deptListArray)
        {
            dic = @{@"allDeplastupdatetime": @(rsp.latestUpdateTime),
                    @"deplist": rsp.deptListArray};
            return dic;
        }
        return dic;
        
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        IMDepartmentReq *req = [[IMDepartmentReq alloc] init];
        [req setUserId:0];
        [req setLatestUpdateTime:[array[0] integerValue]];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_SESSION
                                               commandID:CID_DEPARTINFO_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[req data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}

@end
