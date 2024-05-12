//
//  BTPromptCell.m
//

#import "BTPromptCell.h"
#import "UIView+BTAddition.h"
@implementation BTPromptCell

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
        [_promptLabel setBackgroundColor:[UIColor grayColor]];
        [_promptLabel setTextColor:[UIColor whiteColor]];
        [_promptLabel setFont:[UIFont systemFontOfSize:12]];
        [_promptLabel setTextAlignment:NSTextAlignmentCenter];
        [_promptLabel.layer setCornerRadius:5];
        [_promptLabel setClipsToBounds:YES];
        [self.contentView addSubview:_promptLabel];
    }
    return self;
}

-(void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

-(void)setprompt:(NSString *)prompt
{
    UIFont *font = [UIFont systemFontOfSize:12];
    CGSize size = [prompt sizeWithFont:font
                     constrainedToSize:CGSizeMake(320, 1000000)
                         lineBreakMode:NSLineBreakByWordWrapping];
    [_promptLabel setSize:CGSizeMake(size.width + 30, size.height + 6)];
    CGPoint tmpCenter = self.contentView.center;
    tmpCenter.x = BTFullWidth / 2 + 6;
    [_promptLabel setCenter:tmpCenter];
    [_promptLabel setText:prompt];
}

@end
