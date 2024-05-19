//
//  BTMsgReadNotify.m
//

#import "BTMsgReadNotify.h"
#import "IMMessage.pbobjc.h"


@implementation BTMsgReadNotify
-(int)responseServiceID
{
    return SID_MESSAGE;
}

-(int)responseCommandID
{
    return CID_MSG_READ_NOTIFY;
}

-(UnrequestAPIAnalysis)unrequestAnalysis
{
    UnrequestAPIAnalysis analysis = (id)^(NSData *data) {
        IMMsgDataReadNotify *notify = [IMMsgDataReadNotify parseFromData:data error:nil];
        NSMutableDictionary *dic = [NSMutableDictionary new];
        UInt32 sessionType = notify.sessionType;
        NSString *fromId = [BTRuntime convertPbIdToLocalId:notify.sessionId sessionType:sessionType];
        UInt32 msgId = notify.msgId;
        [dic setObject:fromId forKey:@"from_id"];
        [dic setObject:@(msgId) forKey:@"msgId"];
        [dic setObject:@(sessionType) forKey:@"type"];
        return dic;
    };
    return analysis;
}
@end
