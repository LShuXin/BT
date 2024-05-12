//
//  BTAFClient.m
//

#import "BTConsts.h"
#import "BTAFClient.h"
#import "BTPublicDefine.h"
#import "NSDictionary+BTSafe.h"


@implementation BTAFClient

+(void)handleRequest:(id)result
          success:(void(^)(id))success
          failure:(void(^)(NSError *))failure
{
    
    if (![result isKindOfClass:[NSDictionary class]])
    {
        // TODO: code
        NSError *error = [NSError errorWithDomain:@"data format is invalid"
                                             code:-1000
                                         userInfo:nil];
        BLOCK_SAFE_RUN(failure, error);
        return;
    }
    
    int code = [result[@"status"][@"code"] integerValue];
    NSString *msg = result[@"status"][@"msg"];
    // TODO: code
    if (1001 == code)
    {
        id object = [result valueForKey:@"result"];
        object = BTIsNull(object) ? result : object;
        BLOCK_SAFE_RUN(success, object);
    }
    else
    {
        if (msg)
        {
            NSError *error = [NSError errorWithDomain:msg code:code userInfo:nil];
            failure(error);
        }
        else
        {
            failure(nil);
        }
    }
}

+(void)jsonFormRequest:(NSString *)url
                 param:(NSDictionary *)param
             fromBlock:(void(^)(id<AFMultipartFormData> formData))block
               success:(void(^)(id))success
               failure:(void(^)(NSError *))failure
{
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    [manager GET:url parameters:param success:^(AFHTTPRequestOperation *operation, id responseObject) {
        if ([responseObject respondsToSelector:@selector(objectForKey:)])
        {
            [BTAFClient handleRequest:(NSDictionary *)responseObject success:success failure:failure];
        }
        else
        {
            NSDictionary *responseDictionary = [NSJSONSerialization JSONObjectWithData:responseObject options:0 error:nil];
            [BTAFClient handleRequest:responseDictionary success:success failure:failure];
        }
       
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
       BLOCK_SAFE_RUN(failure, error);
    }];
}

+(void)jsonFormPOSTRequest:(NSString *)url
                     param:(NSDictionary *)param
                   success:(void(^)(id))success
                   failure:(void(^)(NSError *))failure
{
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    manager.responseSerializer = [AFHTTPResponseSerializer serializer];
    NSString *fullPath = [NSString stringWithFormat:@"%@%@", BT_BASE_URL, url];

    [manager POST:fullPath parameters:param success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSDictionary *responseDictionary = [NSJSONSerialization JSONObjectWithData:responseObject options:0 error:nil];
        NSString *string = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
        BTLog(@"post result：%@", string);
        [BTAFClient handleRequest:responseDictionary success:success failure:failure];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            if ([error.domain isEqualToString:NSURLErrorDomain])
            {
                // TODO: code
                error = [NSError errorWithDomain:@"请检查网络连接" code:-100 userInfo:nil];
            }
            BLOCK_SAFE_RUN(failure, error);
    }];
}

@end
