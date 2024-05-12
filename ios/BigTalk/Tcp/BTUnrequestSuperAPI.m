//
//  BTUnrequestSuperAPI.m
//

#import "BTUnrequestSuperAPI.h"
#import "BTAPISchedule.h"


@implementation BTUnrequestSuperAPI

-(BOOL)registerAPIInAPIScheduleReceiveData:(ReceiveData)received
{
    BOOL registerSuccess = [[BTAPISchedule instance] registerUnrequestAPI:(id<BTAPIUnrequestScheduleProtocol>)self];
    if (registerSuccess)
    {
        self.receivedData = received;
        return YES;
    }
    else
    {
        return NO;
    }
}

@end
