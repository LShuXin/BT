//
//  BTReceiveMessageAPI.m
//

#import "BTReceiveMessageAPI.h"
#import "BTMessageEntity.h"
#import "BTMessageModule.h"
#import "BTRuntimeStatus.h"
#import "Encapsulator.h"
#import "IMMessage.pbobjc.h"


@implementation BTReceiveMessageAPI

-(int)responseServiceID
{
    return DDSERVICE_MESSAGE;
}

-(int)responseCommandID
{
    return DDCMD_MSG_DATA;
}

-(UnrequestAPIAnalysis)unrequestAnalysis
{
    UnrequestAPIAnalysis analysis = (id)^(NSData *data) {
        IMMsgData *msgdata = [IMMsgData parseFromData:data error:nil];
        BTMessageEntity *msg = [BTMessageEntity makeMessageFromPbData:msgdata];
        msg.state= MSG_SEND_SUCCESS;
        return msg;
    };
    return analysis;
}
@end
