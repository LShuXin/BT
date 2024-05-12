//
//  BTEmotionsViewController.h
//

#import "BTEmojiFaceView.h"


@protocol BTEmotionsViewControllerDelegate<NSObject>
-(void)emotionViewClickSendButton;
@end


@interface BTEmotionsViewController : UIViewController<BTFacialViewDelegate, UIScrollViewDelegate>
@property(nonatomic, strong)UIScrollView *scrollView;
@property(nonatomic, strong)UIPageControl *pageControl;
@property(strong)NSArray *emotions;
@property(assign)BOOL isOpen;
@property(nonatomic, assign)id<BTEmotionsViewControllerDelegate> delegate;
@end
