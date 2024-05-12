//
//  BTTcpServerModule.h
//

#import <Foundation/Foundation.h>


typedef void(^ConnectTcpServerSuccess)();


@interface BTTcpServerModule : NSObject
{
    
}

// connect to tcp server
-(void)connectTcpServerWithIp:(NSString *)ip
                         port:(NSInteger)port
                      success:(void(^)())success
                      failure:(void(^)())failure;

@end
