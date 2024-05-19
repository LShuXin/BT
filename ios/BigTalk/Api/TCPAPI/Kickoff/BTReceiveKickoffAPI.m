//
//  BTReceiveKickoffAPI.m
//

#import "BTReceiveKickoffAPI.h"
#import "IMLogin.pbobjc.h"


@implementation BTReceiveKickoffAPI
-(int)responseServiceID
{
    return SID_LOGIN;
}

-(int)responseCommandID
{
    return CID_KICK_USER_RES;
}

-(UnrequestAPIAnalysis)unrequestAnalysis
{
    UnrequestAPIAnalysis analysis = (id)^(NSData *data) {
        IMKickUser *res = [IMKickUser parseFromData:data error:nil];
        return @(res.kickReason);
    };
    return analysis;
}

@end
