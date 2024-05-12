//
//  BTAlbumDetailsBottomBar.h
//

#import <UIKit/UIKit.h>


typedef void(^ButtonSelectBlock)(int buttonIndex);


@interface BTAlbumDetailsBottomBar : UIView
@property(nonatomic, strong)UIButton *send;
@property(nonatomic, copy)ButtonSelectBlock block;
-(void)setSendButtonTitle:(int)num;
@end
