//
//  BTGroupAvatarImage.h
//

#import <UIKit/UIKit.h>


@class BTGroupEntity;


@interface BTGroupAvatarImage : UIImageView
-(BTGroupAvatarImage *)getGroupImage:(BTGroupEntity *)group completion:(void(^)(UIImage *))completion;
@end
