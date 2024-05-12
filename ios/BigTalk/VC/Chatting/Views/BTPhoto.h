//
//  BTPhoto.h
//

#import <Foundation/Foundation.h>


@interface BTPhoto : NSObject
@property(nonatomic, strong)NSString *localPath;
@property(nonatomic, strong)NSString *resultUrl;
@property(nonatomic, assign)CGImageRef imageRef;
@property(nonatomic, strong)UIImage *image;
@end
