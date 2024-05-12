/*
 * BTImageGridViewCell.m
 */

#import "BTImageGridViewCell.h"


@implementation BTImageGridViewCell

-(id)initWithFrame:(CGRect)frame reuseIdentifier:(NSString *)aReuseIdentifier
{
	self = [super initWithFrame: frame reuseIdentifier: aReuseIdentifier];
	if (self == nil)
    {
        return nil;
    }

	_imageView = [[UIImageView alloc] initWithFrame: CGRectZero];
	[self.contentView addSubview: _imageView];
    
	self.selectImage = [[UIImageView alloc] initWithFrame:CGRectMake(50, 5, 20, 20)];
    [self.selectImage setImage:[UIImage imageNamed:@"unselected"]];
    [self.selectImage setHighlightedImage:[UIImage imageNamed:@"select"]];
    [self.contentView addSubview:self.selectImage];
    
    self.title = [[UILabel alloc] initWithFrame:CGRectMake(0, self.frame.size.height + 10, self.frame.size.width, 15)];
    [self.title setFont:[UIFont systemFontOfSize:14.0]];
    [self.title setTextAlignment:NSTextAlignmentCenter];
    [self.contentView addSubview:self.title];

	return self ;
}

-(void)setCellIsToHighlight:(BOOL)isHighlight
{
    [self.selectImage setHighlighted:isHighlight];
}

-(CALayer *)glowSelectionLayer
{
    return _imageView.layer;
}

-(UIImage *)image
{
    return _imageView.image ;
}

-(void)setImage:(UIImage *)anImage
{
    _imageView.image = anImage;
    [self setNeedsLayout];
}

-(void)layoutSubviews
{
    [super layoutSubviews];
    if (!self.isShowSelect)
    {
        [self.selectImage setHidden:YES];
    }
    else
    {
        [self.selectImage setHidden:NO];
    }
    CGSize imageSize = _imageView.image.size;
    CGRect frame = _imageView.frame;
    CGRect bounds = self.contentView.bounds;
    
    if (
        (imageSize.width <= bounds.size.width)
        &&
		(imageSize.height <= bounds.size.height)
       )
    {
        return;
    }
    
    // scale it down to fit
    CGFloat hRatio = bounds.size.width / imageSize.width;
    CGFloat vRatio = bounds.size.height / imageSize.height;
    CGFloat ratio = MAX(hRatio, vRatio);
    
    frame.size.width = floorf(imageSize.width * ratio);
    frame.size.height = floorf(imageSize.height * ratio);
    frame.origin.x = floorf((bounds.size.width - frame.size.width) * 0.5);
    frame.origin.y = floorf((bounds.size.height - frame.size.height) * 0.5);
    _imageView.frame = frame;
    self.title.frame = CGRectMake(0, self.frame.size.height + 10, self.frame.size.width, 15);
}

@end
