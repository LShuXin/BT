/*
 * BTImageGridViewCell.h
 */

#import <Foundation/Foundation.h>
// https://github.com/AlanQuatermain/AQGridView
#import "AQGridViewCell.h"

@interface BTImageGridViewCell : AQGridViewCell
{
	
}
@property(nonatomic, retain)UIImage *image;
@property(nonatomic, strong)UIImageView *imageView;
@property(nonatomic, strong)UIImageView *selectImage;
@property(strong)UILabel *title;
@property(assign)BOOL isShowSelect;
-(void)setCellIsToHighlight:(BOOL)isHighlight;
@end
