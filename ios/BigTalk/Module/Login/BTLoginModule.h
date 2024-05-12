//
//  BTLoginModule.h
//

#import <Foundation/Foundation.h>


@class BTLoginAPI, BTMsgServerModule, BTTcpServerModule, BTUserEntity;


@interface BTLoginModule : NSObject
{
    BTLoginAPI *_loginAPI;
    BTTcpServerModule *_tcpServerModule;
    BTMsgServerModule *_msgServerModule;
}

@property(nonatomic, readonly)NSString *token;

+(instancetype)instance;

-(void)loginWithUsername:(NSString *)name
                password:(NSString *)password
                 success:(void(^)(BTUserEntity *user))success
                 failure:(void(^)(NSString *error))failure;

-(void)offlineCompletion:(void(^)())completion;
-(void)reloginSuccess:(void(^)())success
              failure:(void(^)(NSString *error))failure;

@end
