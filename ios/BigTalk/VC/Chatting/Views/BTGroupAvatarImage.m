//
//  BTGroupAvatarImage.m
//

#import "BTGroupAvatarImage.h"
#import "BTGroupEntity.h"
#import "BTDatabaseUtil.h"
#import "BTPhotosCache.h"
#import "BTUserEntity.h"
#import "BTUserModule.h"
#import <SDWebImage/UIImageView+WebCache.h>


#define BIGSIZE   CGSizeMake(22, 22)
#define SMALLSIZE CGSizeMake(12, 12)


@implementation BTGroupAvatarImage

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {

    }
    return self;
}

-(BTGroupAvatarImage *)getGroupImage:(BTGroupEntity *)group completion:(void(^)(UIImage *))completion
{
    BTGroupAvatarImage *groupAvatar = [[BTGroupAvatarImage alloc] initWithFrame:CGRectMake(0, 0, 45, 45)];
    NSData *data = [[BTPhotosCache sharedPhotoCache] photoCacheForKey:group.objId];
    if (data)
    {
        UIImage *image = [UIImage imageWithData:data];
        completion(image);
        return nil;
    }
    else
    {
        [group.fixGroupUserIds enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            __block UIImageView *imagev;
            [[BTUserModule shareInstance] getUserByUserId:obj completion:^(BTUserEntity *user) {
                if (idx <= 4)
                {
                    imagev = [[UIImageView alloc] initWithFrame:CGRectMake((idx % 2) * BIGSIZE.width, (idx / 2) * BIGSIZE.height, BIGSIZE.width, BIGSIZE.height)];
                    [imagev sd_setImageWithURL:[NSURL URLWithString:[user getAvatarUrl]] placeholderImage:[UIImage imageNamed:@"user_placeholder"]];
                    [groupAvatar addSubview:imagev];
                }
                else
                {
                    *stop = YES;
                    UIGraphicsBeginImageContext(groupAvatar.bounds.size);
                    [groupAvatar.layer renderInContext:UIGraphicsGetCurrentContext()];
                    UIImage *viewImage = UIGraphicsGetImageFromCurrentImageContext();
                    UIGraphicsEndImageContext();
                    [[BTPhotosCache sharedPhotoCache] storePhoto:UIImagePNGRepresentation(viewImage) forKey:group.objId toDisk:YES];
                    completion(viewImage);
                }
            }];
        }];
    }
    return groupAvatar;
}

-(UIImage *)makeUserAvaterImage:(NSString *)userId
{
    __block UIImage *newImage;
    
    [[BTUserModule shareInstance] getUserByUserId:userId completion:^(BTUserEntity *user) {
        NSData *data = [[BTPhotosCache sharedPhotoCache] photoCacheForKey:[user getAvatarUrl]];
        UIImage *image = [UIImage imageWithData:data];
        newImage = image;
    }];
    
    return newImage;
}

-(UIImageView *)makeAvater:(UIImage *)image
{
    UIImageView *bigImageView = [[UIImageView alloc] initWithImage:image];
    return bigImageView;
}

@end
