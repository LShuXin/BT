//
//  BTEmojiFaceView.h
//

#import <UIKit/UIKit.h>


@protocol BTFacialViewDelegate
-(void)selectedFacialView:(NSString *)str;
@end


@interface BTEmojiFaceView : UIView
@property(nonatomic, assign)id<BTFacialViewDelegate> delegate;
-(void)loadFacialView:(int)offset limit:(CGSize)limit;
@end
