//
//  BTAddMemberToGroupAPI.m
//

#import "BTAddMemberToGroupAPI.h"
#import "BTGroupModule.h"
#import "BTGroupEntity.h"
#import "BTRuntimeStatus.h"
#import "IMGroup.pbobjc.h"
#import "GPBProtocolBuffers.h"


@implementation BTAddMemberToGroupAPI

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
    return CID_CHANGE_GROUP_REQ;
}

-(int)responseServiceID
{
    return SID_GROUP;
}

-(int)responseCommandID
{
    return CID_CHANGE_GROUP_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        
        IMGroupChangeMemberRsp *rsp = [IMGroupChangeMemberRsp parseFromData:data error:nil];
        uint32_t result = rsp.resultCode;
        NSMutableArray *array = [NSMutableArray new];
        
        // 出错
        if (result != 0)
        {
            return array;
        }
    
        NSUInteger userCnt = [rsp.curUserIdListArray count];

        for (NSUInteger i = 0; i < userCnt; i++)
        {
            NSString *userId = [BTRuntime convertPbIdToLocalId:[[rsp curUserIdListArray] valueAtIndex:i]
                                                   sessionType:SessionType_SessionTypeSingle];
            [array addObject:userId];
        }
        return array;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        NSString *groupId = array[0];
        NSArray *userList = array[1];
        NSMutableArray *users = [NSMutableArray new];
        for (NSString *user in userList)
        {
            [users addObject:@([BTRuntime convertLocalIdToPbId:user])];
        }
        IMGroupChangeMemberReq *memberChange = [[IMGroupChangeMemberReq alloc] init];
        [memberChange setUserId:0];
        [memberChange setChangeType:GroupModifyType_GroupModifyTypeAdd];
        [memberChange setGroupId:[BTRuntime convertLocalIdToPbId:groupId]];
        
        GPBUInt32Array *tempUsers = [[GPBUInt32Array alloc] init];
        for (NSNumber *number in users)
        {
            [tempUsers addValue:number.unsignedIntValue];
        }
        [memberChange setMemberIdListArray:tempUsers];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_GROUP
                                               commandID:CID_CHANGE_GROUP_REQ
                                                   seqNo:seqNo];
        
        [outputStream directWriteBytes:[memberChange data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
