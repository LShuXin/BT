//
//  BTUnrequestSuperAPI.h
//

#import <Foundation/Foundation.h>
#import "BTDataInputStream.h"
#import "BTAPIUnrequestScheduleProtocol.h"
#import "BTTcpClientManager.h"
#import "BTDataOutputStream.h"
#import "BTDataOutputStream+Addition.h"
#import "BTTcpProtocolHeader.h"


typedef void(^ReceiveData)(id object, NSError *error);


@interface BTUnrequestSuperAPI : NSObject
// 收到数据的回调
@property(nonatomic,copy)ReceiveData receivedData;

-(BOOL)registerAPIInAPIScheduleReceiveData:(ReceiveData)received;

@end
