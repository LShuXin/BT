//
//  BTMsgServer.h
//

#import <Foundation/Foundation.h>


@interface BTMsgServerModule : NSObject

// tcp connection authentication
-(void)checkUserId:(NSString *)userId
               pwd:(NSString *)password
             token:(NSString *)token
           success:(void(^)(id object))success
           failure:(void(^)(id object))failure;

@end
