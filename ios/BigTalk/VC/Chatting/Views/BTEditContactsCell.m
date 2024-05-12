//
//  BTEditContactsCell.m
//

#import "BTEditContactsCell.h"


@interface BTEditContactsCell()
@property(strong)UIImageView *selectView;
@end


@implementation BTEditContactsCell
-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        self.selectView = [[UIImageView alloc] initWithFrame:CGRectMake(10, 20, 22, 22)];
        [self.selectView setImage:[UIImage imageNamed:@"unselected"]];
        [self.selectView setHighlightedImage:[UIImage imageNamed:@"select"]];
        [self addSubview:self.selectView];
        self.avatar.frame = CGRectMake(45, 10, 35, 35);
        self.nameLabel.frame = CGRectMake(90, 20, 100, 15);
        self.cnameLabel.frame = CGRectMake(130, 23, 150, 15);
    }
    return self;
}

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {

    }
    return self;
}

-(void)setCellToSelected:(BOOL)select
{
    [self.selectView setHighlighted:select];
}

@end
