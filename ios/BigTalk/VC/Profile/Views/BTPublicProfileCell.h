//
//  BTPublicProfileCell.h
//

#import <UIKit/UIKit.h>
#import "BTPublicDefine.h"


@interface BTPublicProfileCell : UITableViewCell

@property(nonatomic, retain)UILabel *descLabel;
@property(nonatomic, retain)UILabel *detailLabel;
@property(nonatomic, retain)UIImageView *phone;

-(void)setDesc:(NSString *)desc detail:(NSString *)detail;
-(void)hidePhone:(BOOL)hide;

@end
