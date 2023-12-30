//
//  GetGroupInfoAPi.m
//  TeamTalk
//
//  Created by Michael Scofield on 2014-09-18.
//  Copyright (c) 2014 dujia. All rights reserved.
//

#import "GetGroupInfoAPI.h"
#import "GroupEntity.h"
#import "IMGroup.pbobjc.h"


@implementation GetGroupInfoAPI

-(int)requestTimeOutTimeInterval
{
    return 0;
}

-(int)requestServiceID
{
    return SERVICE_GROUP;
}

-(int)responseServiceID
{
    return SERVICE_GROUP;
}

-(int)requestCommendID
{
    return CMD_ID_GROUP_USER_LIST_REQ;
}

-(int)responseCommendID
{
    return CMD_ID_GROUP_USER_LIST_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData* data)
    {
        IMGroupInfoListRsp* rsp = [IMGroupInfoListRsp parseFromData:data error:nil];
        // NSUInteger userid = rsp.userId;
        NSMutableArray* array = [NSMutableArray new];
        for (GroupInfo* info in rsp.groupInfoListArray)
        {
            GroupEntity* group = [GroupEntity initGroupEntityFromPBData:info];
            [array addObject:group];
        }
        
        return array;
       
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo)
    {
        DDDataOutputStream* dataout = [[DDDataOutputStream alloc] init];
        IMGroupInfoListReq* info = [[IMGroupInfoListReq alloc] init];
        GroupVersionInfo* groupInfo = [[GroupVersionInfo alloc] init];
        [groupInfo setGroupId:[object[0] integerValue]];
        [groupInfo setVersion:[object[1] integerValue]];
        [info setUserId:0];
        [info setGroupVersionListArray:[NSMutableArray arrayWithObject:groupInfo]];
        [dataout writeInt:0];
        [dataout writeTcpProtocolHeader:SERVICE_GROUP
                                    cId:CMD_ID_GROUP_USER_LIST_REQ
                                  seqNo:seqNo];
        [dataout directWriteBytes:[info data]];
        [dataout writeDataCount];
        return [dataout toByteArray];

    };
    return package;
}
@end
