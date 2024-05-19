//
//  BTGetGroupInfoAPI.m
//

#import "BTGetGroupInfoAPI.h"
#import "BTGroupEntity.h"
#import "IMGroup.pbobjc.h"


@implementation BTGetGroupInfoAPI

-(int)requestTimeOutTimeInterval
{
    return 0;
}

-(int)requestServiceID
{
    return SID_GROUP;
}

-(int)requestCommandID
{
    return CID_GROUP_USER_LIST_REQ;
}

-(int)responseServiceID
{
    return SID_GROUP;
}

-(int)responseCommandID
{
    return CID_GROUP_USER_LIST_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMGroupInfoListRsp *rsp = [IMGroupInfoListRsp parseFromData:data error:nil];
        NSMutableArray *array = [NSMutableArray new];
        for (GroupInfo *info in rsp.groupInfoListArray)
        {
            BTGroupEntity *group = [BTGroupEntity initGroupEntityWithPbData:info];
            [array addObject:group];
        }
        
        return array;
       
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo) {
        NSArray *array = (NSArray *)object;
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        IMGroupInfoListReq *groupInfoListReq = [[IMGroupInfoListReq alloc] init];
        GroupVersionInfo *groupInfo = [[GroupVersionInfo alloc] init];
        [groupInfo setGroupId:[array[0] integerValue]];
        [groupInfo setVersion:[array[1] integerValue]];
        [groupInfoListReq setUserId:0];
        [groupInfoListReq setGroupVersionListArray:[NSMutableArray arrayWithObject:groupInfo]];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_GROUP
                                               commandID:CID_GROUP_USER_LIST_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[groupInfoListReq data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];

    };
    return package;
}
@end
