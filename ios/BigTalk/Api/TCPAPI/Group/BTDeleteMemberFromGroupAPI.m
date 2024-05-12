//
//  BTDeleteMemberFromGroupAPI.m
//

#import "BTDeleteMemberFromGroupAPI.h"
#import "BTGroupEntity.h"
#import "BTGroupModule.h"
#import "IMGroup.pbobjc.h"


@implementation BTDeleteMemberFromGroupAPI

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return SERVICE_GROUP;
}

-(int)responseServiceID
{
    return SERVICE_GROUP;
}

-(int)requestCommandID
{
    return CMD_ID_GROUP_CHANGE_GROUP_REQ;
}

-(int)responseCommandID
{
    return CMD_ID_GROUP_CHANGE_GROUP_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData* data) {
    
        IMGroupChangeMemberRsp *rsp = [IMGroupChangeMemberRsp parseFromData:data error:nil];
        uint32_t result = rsp.resultCode;
        BTGroupEntity *groupEntity = nil;
        if (result != 0)
        {
            return groupEntity;
        }
        NSString *groupId = [BTGroupEntity pbGroupIdToLocalGroupId:rsp.groupId];
        groupEntity = [[BTGroupModule instance] getGroupByGroupId:groupId];
        NSMutableArray *array = [NSMutableArray new];
        for (uint32_t i = 0; i < [rsp.curUserIdListArray count]; i++)
        {
            NSString *userId = [BTRuntime convertPbIdToLocalId:[[rsp curUserIdListArray] valueAtIndex:i]
                                                   sessionType:SessionType_SessionTypeSingle];
            [array addObject:userId];
        }
        groupEntity.groupUserIds = array;
        return groupEntity;
        
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        NSString *groupId = array[0];
        NSUInteger userId = [BTRuntime convertLocalIdToPbId:array[1]];
        IMGroupChangeMemberReq *memberChange = [[IMGroupChangeMemberReq alloc] init];
        [memberChange setUserId:[BTRuntime convertLocalIdToPbId:BTRuntime.user.objId]];
        [memberChange setChangeType:GroupModifyType_GroupModifyTypeDel];
        [memberChange setGroupId:[BTRuntime convertLocalIdToPbId:groupId]];
        [memberChange setMemberIdListArray:@[@(userId)]];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SERVICE_GROUP
                                                     commandID:CMD_ID_GROUP_CHANGE_GROUP_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[memberChange data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
