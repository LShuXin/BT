//
//  BTGetRecentSession.m
//

#import "BTGetRecentSession.h"
#import "BTSessionEntity.h"
#import "BTRuntimeStatus.h"
#import "BTGroupEntity.h"
#import "security.h"
#import "IMBuddy.pbobjc.h"


@implementation BTGetRecentSession

-(int)requestTimeOutTimeInterval
{
    return TimeOutTimeInterval;
}

-(int)requestServiceID
{
    return MODULE_ID_SESSION;
}

-(int)responseServiceID
{
    return MODULE_ID_SESSION;
}

-(int)requestCommandID
{
    return RECENT_SESSION_REQ;
}

-(int)responseCommandID
{
    return RECENT_SESSION_RES;
}

-(Analysis)analysisReturnData
{
    Analysis analysis = (id)^(NSData *data) {
        IMRecentContactSessionRsp *rsp = [IMRecentContactSessionRsp parseFromData:data error:nil];
        NSMutableArray *array = [NSMutableArray new];
        
        for (ContactSessionInfo *sessionInfo in rsp.contactSessionListArray)
        {
            NSString *sessionId = @"";
            SessionType sessionType = sessionInfo.sessionType;
            if (sessionType == SessionType_SessionTypeSingle)
            {
                sessionId = [BTUserEntity pbUserIdToLocalUserId:sessionInfo.sessionId];
            }
            else
            {
                sessionId = [BTGroupEntity pbGroupIdToLocalGroupId:sessionInfo.sessionId];
            }
            NSInteger updated_time = sessionInfo.updatedTime;
            BTSessionEntity *session =[[BTSessionEntity alloc] initWithSessionId:sessionId sessionType:sessionType];
            NSString *lastMsgData = [[NSString alloc] initWithData:sessionInfo.latestMsgData encoding:NSUTF8StringEncoding];

            char *pOut;
            uint32_t nOutLen;
            uint32_t nInLen = strlen([lastMsgData cStringUsingEncoding:NSUTF8StringEncoding]);
            int nRet = DecryptMsg([lastMsgData cStringUsingEncoding:NSUTF8StringEncoding], nInLen, &pOut, nOutLen);
            if (nRet == 0)
            {
                session.lastMsg = [NSString stringWithCString:pOut encoding:NSUTF8StringEncoding];
                Free(pOut);
            }
            else
            {
                session.lastMsg = @" ";
            }
          
            session.lastMsgId = sessionInfo.latestMsgId;
            session.timeInterval = updated_time;
            [array addObject:session];
        }
        return array;
    };
    return analysis;
}

-(Package)packageRequestObject
{
    Package package = (id)^(id object, uint16_t seqNo) {
        NSArray *array = (NSArray *)object;
        IMRecentContactSessionReq *req = [[IMRecentContactSessionReq alloc] init];
        [req setUserId:0];
        [req setLatestUpdateTime:[array[0] integerValue]];
        BTDataOutputStream *outputStream = [[BTDataOutputStream alloc] init];
        [outputStream writeInt:0];
        [outputStream writeTcpProtocolHeaderUseServiceID:MODULE_ID_SESSION
                                               commandID:RECENT_SESSION_REQ
                                                   seqNo:seqNo];
        [outputStream directWriteBytes:[req data]];
        [outputStream writeDataCount];
        return [outputStream toByteArray];
    };
    return package;
}
@end
