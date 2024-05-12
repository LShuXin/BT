//
//  BTPublicProfileCell.m
//

#import "BTPublicProfileCell.h"
#import "BTPublicDefine.h"

@implementation BTPublicProfileCell

-(instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        _descLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 15, 40, 15)];
        [_descLabel setFont:systemFont(15)];
        [_descLabel setTextColor:RGB(164, 165, 169)];
        [self.contentView addSubview:_descLabel];
        
        _detailLabel = [[UILabel alloc]initWithFrame:CGRectMake(70, 12, 100, 20)];
        [_descLabel setFont:systemFont(15)];
        [self.contentView addSubview:_detailLabel];
        
        _phone = [[UIImageView alloc] initWithFrame:CGRectMake(BTScreenWidth - 15 - 12, 40, 12, 13)];
        [_phone setImage:[UIImage imageNamed:@"phone"]];
        [_phone setHidden:YES];
        _phone.backgroundColor = [UIColor redColor];
        [self.contentView addSubview:_phone];
    }
    return self;
}

-(void)setDesc:(NSString *)desc detail:(NSString *)detail
{
    [_descLabel setText:desc];
    [_detailLabel setText:detail];
}

-(void)hidePhone:(BOOL)hide
{
    [_phone setHidden:hide];
}

-(void)awakeFromNib
{
    [super awakeFromNib];
}

-(void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];
}

@end
