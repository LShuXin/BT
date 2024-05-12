//
//  BTAFClient.h
//

#import <Foundation/Foundation.h>
#import "AFNetworking.h"


@interface BTAFClient : NSObject


// 发送 get 请求
+(void)jsonFormRequest:(NSString *)url
                 param:(NSDictionary *)param
             fromBlock:(void(^)(id<AFMultipartFormData> formData))block
               success:(void(^)(id))success
               failure:(void(^)(NSError *))failure;

// 发送 post 请求
+(void)jsonFormPOSTRequest:(NSString *)url
                     param:(NSDictionary *)param
                   success:(void(^)(id))success
                   failure:(void(^)(NSError *))failure;

@end

// 参数不为空时才会运行相关的block
#define BLOCK_SAFE_RUN(block, ...) block ? block(__VA_ARGS__) : nil;
