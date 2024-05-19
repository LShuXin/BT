//
//  BTSuperAPI.m
//

#import "BTSuperAPI.h"
#import "BTAPISchedule.h"


static uint16_t theSeqNo = 0;


@implementation BTSuperAPI

-(void)requestWithObject:(id)object completion:(RequestCompletion)completion
{
    theSeqNo++;
    _seqNo = theSeqNo;
    
    BOOL registerSuccess = [[BTAPISchedule instance] registerApi:(id<BTAPIScheduleProtocol>)self];
    
    if (!registerSuccess)
    {
        return;
    }
    
    if ([(id<BTAPIScheduleProtocol>)self requestTimeOutTimeInterval] > 0)
    {
        [[BTAPISchedule instance] registerTimeoutApi:(id<BTAPIScheduleProtocol>)self];
    }
    
    // 保存完成块
    self.completion = completion;

    // 数据打包
    Package package = [(id<BTAPIScheduleProtocol>)self packageRequestObject];
    NSMutableData *requestData = package(object, _seqNo);
    
    // 发送
    if (requestData)
    {
        [[BTAPISchedule instance] sendData:requestData];
    }
}

@end
