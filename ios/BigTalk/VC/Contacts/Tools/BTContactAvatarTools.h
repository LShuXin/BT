//
//  BTContactAvatarTools.h
//

#import <UIKit/UIKit.h>


typedef void(^ButtonSelectBlock)(int buttonIndex);

@class BTUserEntity;

@interface BTContactAvatarTools : UIView

@property(strong)UIButton *item1;
@property(strong)UIButton *item2;
@property(strong)UIButton *item3;
@property(strong)BTUserEntity *user;
@property(copy)ButtonSelectBlock block;
@property(assign)BOOL isShow;

-(void)hiddenSelf;

@end
