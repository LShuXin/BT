//
//  BTReceiveKickoffAPI.m
//

#import "BTReceiveKickoffAPI.h"
#import "IMLogin.pbobjc.h"


@implementation BTReceiveKickoffAPI
-(int)responseServiceID
{
    return DDSERVICE_LOGIN;
}

-(int)responseCommandID
{
    return DDCMD_LOGIN_KICK_USER;
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
