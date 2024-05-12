//
//  BTContactsCell.h
//

#import <UIKit/UIKit.h>
#import "BTGroupEntity.h"


@interface BTContactsCell : UITableViewCell

@property(strong)UIButton *button;
@property(strong)UIImageView *avatar;
@property(strong)UILabel *nameLabel;
@property(strong)UILabel *cnameLabel;

-(void)setCellContent:(NSString *)avater
                 name:(NSString *)name
                cname:(NSString *)cname;
-(void)setGroupAvatar:(BTGroupEntity *)group;

@end
