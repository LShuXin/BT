//
//  BTLoginAPI.h
//

#import <Foundation/Foundation.h>


@interface BTLoginAPI : NSObject

// http 短链接实现登录验证，获取token
-(void)loginWithUserName:(NSString *)userName
                password:(NSString *)password
                 success:(void(^)(id respone))success
                 failure:(void(^)(id error))failure;

@end
