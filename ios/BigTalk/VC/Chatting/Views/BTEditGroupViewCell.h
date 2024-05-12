//
//  BTEditGroupViewCell.h
//

#import <UIKit/UIKit.h>
#import "BTUserEntity.h"

@interface BTEditGroupViewCell : UICollectionViewCell
@property(strong)UILabel *name;
@property(strong)UIImageView *personIcon;
@property(strong)UIButton *button;
-(void)showDeleteActionView;
@end
