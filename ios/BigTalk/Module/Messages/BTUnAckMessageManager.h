//
//  BTUnAckMessageManager.h
//

#import <Foundation/Foundation.h>
#import "BTMessageEntity.h"


@interface BTUnAckMessageManager : NSObject

+(instancetype)instance;
-(void)removeMessageFromUnAckQueue:(BTMessageEntity *)message;
-(void)addMessageToUnAckQueue:(BTMessageEntity *)message;
-(BOOL)isInUnAckQueue:(BTMessageEntity *)message;
@end
