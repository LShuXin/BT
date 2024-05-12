//
//  BTSuperAPI.h
//

#import <Foundation/Foundation.h>
#import "BTAPIScheduleProtocol.h"
#import "BTTcpClientManager.h"
#import "BTDataOutputStream.h"
#import "BTDataInputStream.h"
#import "BTDataOutputStream+Addition.h"
#import "BTTcpProtocolHeader.h"


#define TimeOutTimeInterval 10


typedef void(^RequestCompletion)(id response, NSError *error);


static uint32_t strLen(NSString *aString)
{
    return (uint32_t)[[aString dataUsingEncoding:NSUTF8StringEncoding] length];
}


@interface BTSuperAPI : NSObject
@property(nonatomic,copy)RequestCompletion completion;
@property(nonatomic,readonly)uint16_t seqNo;
// 子类调用该方法实现网络请求
-(void)requestWithObject:(id)object completion:(RequestCompletion)completion;
@end
