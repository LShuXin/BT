//
//  BTFixedGroupAPI.m
//

#import "BTFixedGroupAPI.h"
#import "BTGroupEntity.h"
#import "IMGroup.pbobjc.h"


@implementation BTFixedGroupAPI

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
    return CMD_ID_GROUP_LIST_REQ;
}

-(int)responseCommandID
{
    return CMD_ID_GROUP_LIST_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *object) {
     
        IMNormalGroupListRsp *imNormalRsp = [IMNormalGroupListRsp parseFromData:object error:nil];
        NSMutableArray *array = [NSMutableArray new];
        for (GroupVersionInfo *info in imNormalRsp.groupVersionListArray)
        {
            NSDictionary *dic = @{@"groupId":@(info.groupId), @"version":@(info.version)};
            [array addObject:dic];
        }
        return  array;
//        NSMutableArray* recentlyGroup = [[NSMutableArray alloc] init];
//        uint32_t groupCnt = [bodyData readInt];
//        for (uint32_t i = 0; i < groupCnt; i++)
//        {
//            NSString* groupId = [bodyData readUTF];
//            NSString* groupName = [bodyData readUTF];
//            NSString* groupAvatar = [bodyData readUTF];
//            NSString* groupCreator = [bodyData readUTF];
//            
//            int groupType = [bodyData readInt];
//            GroupEntity* group = [[GroupEntity alloc] init];
//            group.objId = groupId;
//            group.name = groupName;
//            group.avatar = groupAvatar;
//            group.groupCreatorId = groupCreator;
//            group.groupType = groupType;
//            group.isShield=[bodyData readInt];
//            uint32_t groupMemberCnt = [bodyData readInt];
//            if(groupMemberCnt > 0)
//                group.groupUserIds = [[NSMutableArray alloc] init];
//            for (uint32_t i = 0; i < groupMemberCnt; i++)
//            {
//                NSString *userId = [bodyData readUTF];
//                [group.groupUserIds addObject:userId];
//                [group addFixOrderGroupUserIDS:userId];
//            }
//            
//            [recentlyGroup addObject:group];
//        }
//        //log4CInfo(@"get recently group count:%i",[recentlyGroup count]);
        
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo) {
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        IMNormalGroupListReq *imNormalGroupListReq = [[IMNormalGroupListReq alloc] init];
        [imNormalGroupListReq setUserId:0];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SERVICE_GROUP
                                               commandID:CMD_ID_GROUP_LIST_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[imNormalGroupListReq data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
