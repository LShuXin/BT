//
//  BTChatEditTableViewCell.m
//

#import "BTChatEditTableViewCell.h"

@implementation BTChatEditTableViewCell

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        self.title = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 100, 15)];
        [self.contentView addSubview:self.title];
    }
    return self;
}

-(void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

@end
