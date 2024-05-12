//
//  BTSendPhotoMessageAPI.m
//

#import "BTSendPhotoMessageAPI.h"
#import "AFHTTPRequestOperationManager.h"
#import "BTPhoto.h"
#import "BTPublicDefine.h"
#import "BTMessageEntity.h"
#import "BTPhotosCache.h"
#import "NSDictionary+BTSafe.h"


static int max_try_upload_times = 5;

@interface BTSendPhotoMessageAPI()
@property(nonatomic,strong)AFHTTPRequestOperationManager *manager;
@property(nonatomic,strong)NSOperationQueue *queue;
@property(assign)bool isSending;
@end


@implementation BTSendPhotoMessageAPI

+(BTSendPhotoMessageAPI *)sharedPhotoCache
{
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [self new];
    });
    return instance;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        self.manager = [AFHTTPRequestOperationManager manager];
        self.manager.responseSerializer.acceptableContentTypes = [NSSet setWithObject:@"text/html"];
        self.queue = [NSOperationQueue new];
        self.queue.maxConcurrentOperationCount = 1;
    }
    return self;
}

-(void)uploadImage:(NSString *)imageKey
           success:(void(^)(NSString *imageURL))success
           failure:(void(^)(id error))failure
{
    
    NSBlockOperation *operation = [NSBlockOperation blockOperationWithBlock:^() {
        NSURL *url = [NSURL URLWithString:BTRuntime.msfs];
        NSString *urlString =  [url.absoluteString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        @autoreleasepool
        {
            __block NSData *imageData = [[BTPhotosCache sharedPhotoCache] photoCacheForKey:imageKey];
            if (imageData == nil)
            {
                failure(@"data is emplty");
                return;
            }
            __block UIImage *image = [UIImage imageWithData:imageData];
            NSString *imageName = [NSString stringWithFormat:@"image.png_%dx%d.png",
                                   image.size.width,
                                   image.size.height];
            NSDictionary *params = [NSDictionary dictionaryWithObjectsAndKeys:@"im_image", @"type", nil];
            [self.manager POST:urlString parameters:params constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
                [formData appendPartWithFileData:imageData name:@"image" fileName:imageName mimeType:@"image/jpeg"];
            } success:^(AFHTTPRequestOperation *operation, id responseObject) {
                imageData = nil;
                image = nil;
                NSInteger statusCode = [operation.response statusCode];
                if (statusCode == 200)
                {
                    NSString *imageURL = nil;
                    if ([responseObject isKindOfClass:[NSDictionary class]])
                    {
                        if ([[responseObject safeObjectForKey:@"error_code"] intValue] == 0)
                        {
                            imageURL = [responseObject safeObjectForKey:@"url"];
                        }
                        else
                        {
                            failure([responseObject safeObjectForKey:@"error_msg"]);
                        }
                    }
                   
                    NSMutableString *url = [NSMutableString stringWithFormat:@"%@", kBTImageMessagePrefix];
                    if (!imageURL)
                    {
                        max_try_upload_times--;
                        if (max_try_upload_times > 0)
                        {
                            
                            [self uploadImage:imageKey success:^(NSString *imageURL) {
                                success(imageURL);
                            } failure:^(id error) {
                                failure(error);
                            }];
                        }
                        else
                        {
                            failure(nil);
                        }
                        
                    }
                    if (imageURL)
                    {
                        [url appendString:imageURL];
                        [url appendString:kBTImageMessageSuffix];
                        success(url);
                    }
                }
                else
                {
                    self.isSending = NO;
                    failure(nil);
                }
                
            } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
                self.isSending = NO;
                NSDictionary *userInfo = error.userInfo;
                NSHTTPURLResponse *response = userInfo[AFNetworkingOperationFailingURLResponseErrorKey];
                NSInteger stateCode = response.statusCode;
                if (!(stateCode >= 300 && stateCode <= 307))
                {
                    failure(@"断网");
                }
            }];
        }
    }];
    [self.queue addOperation:operation];
    
}
+(NSString *)imageUrl:(NSString *)content
{
    NSRange range = [content rangeOfString:@"path="];
    NSString *url = nil;
    if ([content length] > range.location + range.length)
    {
        url = [content substringFromIndex:range.location + range.length];
    }
    url = [(NSString *)url stringByReplacingOccurrencesOfString:@"+" withString:@" "];
    url = [url stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    return url;
}

@end
