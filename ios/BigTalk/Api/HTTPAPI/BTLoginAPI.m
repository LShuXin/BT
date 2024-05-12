//
//  BTLoginAPI.m
//

#import "BTLoginAPI.h"
#import "BTAFClient.h"


@implementation BTLoginAPI

-(void)loginWithUserName:(NSString *)userName
                password:(NSString *)password
                 success:(void(^)(id respone))success
                 failure:(void(^)(id error))failure
{
    NSMutableDictionary *dictParams = [NSMutableDictionary dictionary];
    [dictParams setObject:userName forKey:@"user_email"];
    [dictParams setObject:password forKey:@"user_pass"];
    [dictParams setObject:@"ooxx" forKey:@"macim"];
    [dictParams setObject:@"1.0" forKey:@"imclient"];
    [dictParams setObject:@"1" forKey:@"remember"];
    [BTAFClient jsonFormPOSTRequest:@"user/zlogin/" param:dictParams success:^(id result) {
        success(result);
    } failure:^(NSError *error) {
        failure(error);
    }];
}

@end
