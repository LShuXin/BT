//
//  BTPersonEditCollectionCell.h
//

#import <UIKit/UIKit.h>

@interface BTPersonEditCollectionCell : UICollectionViewCell
@property(strong)UIImageView *personIcon;
@property(strong)UIButton *delImg;
@property(strong)UILabel *name;
@property(strong)UIButton *button;
-(void)setName:(NSString *)name avatarImage:(NSString *)urlString;
@end
