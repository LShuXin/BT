//
//  BTGetUnreadMessagesAPI.m
//

#import "BTGetUnreadMessagesAPI.h"
#import "Encapsulator.h"
#import "BTUserModule.h"
#import "BTMessageModule.h"
#import "BTRuntimeStatus.h"
#import "BTSessionEntity.h"
#import "BTGroupEntity.h"
#import "BTMessageEntity.h"
#import "IMMessage.pbobjc.h"
#import "IMBaseDefine.pbobjc.h"


@implementation BTGetUnreadMessagesAPI

-(int)requestTimeOutTimeInterval
{
    return 20;
}

-(int)requestServiceID
{
    return SID_MESSAGE;
}

-(int)requestCommandID
{
    return CID_MSG_UNREAD_CNT_REQ;
}

-(int)responseServiceID
{
    return SID_MESSAGE;
}

-(int)responseCommandID
{
    return CID_MSG_UNREAD_CNT_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMUnreadMsgCntRsp *unreadRsp = [IMUnreadMsgCntRsp parseFromData:data error:nil];
        NSMutableDictionary *dic = [NSMutableDictionary new];
        NSInteger totalCnt = unreadRsp.totalCnt;
        [dic setObject:@(totalCnt) forKey:@"m_total_cnt"];
        NSMutableArray *array = [NSMutableArray new];
        
        for (UnreadInfo *unreadInfo in unreadRsp.unreadinfoListArray)
        {
            NSString *userId = @"";
            NSInteger sessionType = unreadInfo.sessionType;
            if (sessionType == SessionType_SessionTypeSingle)
            {
                userId = [BTUserEntity pbUserIdToLocalUserId:unreadInfo.sessionId];
            }
            else
            {
                userId = [BTGroupEntity pbGroupIdToLocalGroupId:unreadInfo.sessionId];
            }
            NSInteger unreadCnt = unreadInfo.unreadCnt;
            NSInteger latestMsgId = unreadInfo.latestMsgId;
            NSString *latestMsgContent = [[NSString alloc] initWithData:unreadInfo.latestMsgData encoding:NSUTF8StringEncoding];
            
            BTSessionEntity *session = [[BTSessionEntity alloc] initWithSessionId:userId sessionType:sessionType];
            session.unReadMsgCount = unreadCnt;
            session.lastMsg = latestMsgContent;
            session.lastMsgId = latestMsgId;
            [array addObject:session];
        }
       
        [dic setObject:array forKey:@"sessions"];
        return dic;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint32_t seqNo)
    {
        IMUnreadMsgCntReq *unreadReq = [[IMUnreadMsgCntReq alloc] init];
        [unreadReq setUserId:0];
        BTDataOutputStream *outputStream  = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:SID_MESSAGE
                                               commandID:CID_MSG_UNREAD_CNT_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[unreadReq data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
