//
//  BTSendPhotoMessageAPI.h
//

#import "BTSuperAPI.h"

@interface BTSendPhotoMessageAPI : NSObject
+(BTSendPhotoMessageAPI *)sharedPhotoCache;
-(void)uploadImage:(NSString*)imagePath
           success:(void(^)(NSString *imageURL))success
           failure:(void(^)(id error))failure;
@end
