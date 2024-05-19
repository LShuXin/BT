//
//  BTCreateGroupAPI.m
//

#import "BTCreateGroupAPI.h"
#import "BTTcpProtocolHeader.h"
#import "BTGroupEntity.h"
#import "IMGroup.pbobjc.h"


@implementation BTCreateGroupAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return SID_GROUP;
}

-(int)requestCommandID
{
    return CID_CREATE_TMP_GROUP_REQ;
}

-(int)responseServiceID
{
    return SID_GROUP;
}

-(int)responseCommandID
{
    return CID_CREATE_TMP_GROUP_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMGroupCreateRsp *rsp = [IMGroupCreateRsp parseFromData:data error:nil];
        uint32_t result = rsp.resultCode;
        BTGroupEntity *group = nil;
        if (result != 0)
        {
            return group;
        }
        else
        {
            NSString *groupId = [BTGroupEntity pbGroupIdToLocalGroupId:rsp.groupId];
            NSString *groupName = rsp.groupName;
            uint32_t userCnt = [rsp.userIdListArray count];
            group = [[BTGroupEntity alloc] init];
            group.objId = groupId;
            group.name = groupName;
            group.groupUserIds = [[NSMutableArray alloc] init];
            
            for (uint32_t i = 0; i < userCnt; i++)
            {
                NSString *userId = [BTUserEntity pbUserIdToLocalUserId:[[rsp userIdListArray] valueAtIndex:i]];
                [group.groupUserIds addObject:userId];
                [group addFixOrderGroupUserIds:userId];
            }
           
            return group;
        }
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        NSString *groupName = array[0];
        NSString *groupAvatar = array[1];
        NSArray *groupUserList = array[2];
        
        IMGroupCreateReq *req = [[IMGroupCreateReq alloc] init];
        [req setUserId:0];
        [req setGroupName:groupName];
        [req setGroupAvatar:groupAvatar];
        [req setGroupType:GroupType_GroupTypeTmp];
        GPBUInt32Array *pbUserIds = [[GPBUInt32Array alloc] init];
        for (NSString *localUserId in groupUserList)
        {
            [pbUserIds addValue:[BTRuntime convertLocalIdToPbId:localUserId]];
        }
        [req setMemberIdListArray:pbUserIds];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_GROUP
                                               commandID:CID_CREATE_TMP_GROUP_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[req data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
