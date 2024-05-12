//
//  BTReceiveGroupAddMemberAPI.m
//

#import "BTReceiveGroupAddMemberAPI.h"
#import "BTGroupModule.h"
#import "BTRuntimeStatus.h"
#import "BTGroupEntity.h"


@implementation BTReceiveGroupAddMemberAPI

-(int)responseServiceID
{
    return SERVICE_GROUP;
}

-(int)responseCommandID
{
    return CMD_ID_GROUP_CHANGE_GROUP_REQ;
}

-(UnrequestAPIAnalysis)unrequestAnalysis
{
    UnrequestAPIAnalysis analysis = (id)^(NSData *data) {
        BTDataInputStream *inputStream = [BTDataInputStream dataInputStreamWithData:data];
        uint32_t result = [inputStream readInt];
        BTGroupEntity *groupEntity = nil;
        if (result != 0)
        {
            return groupEntity;
        }
        NSString *groupId = [inputStream readUTF];
        uint32_t userCnt = [inputStream readInt];
        groupEntity = [[BTGroupModule instance] getGroupByGroupId:groupId];
//        if (!groupEntity)
//        {
//            [groupModule tcpGetUnkownGroupInfo:groupId];
//        }
        if (groupEntity)
        {
            for (uint32_t i = 0; i < userCnt; i++)
            {
                NSString *userId = [inputStream readUTF];
                if (![groupEntity.groupUserIds containsObject:userId])
                {
                    [groupEntity.groupUserIds addObject:userId];
                    [groupEntity addFixOrderGroupUserIds:userId];
                }
            }
        }
        
        BTLog(@"Server Push: New Group Member");
        return groupEntity;
    };
    return analysis;
}
@end
