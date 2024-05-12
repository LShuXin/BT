//
//  BTTouchDownGestureRecognizer.h
//

#import <UIKit/UIKit.h>
typedef void(^BTTouchDown)();
typedef void(^BTTouchMoveOutside)();
typedef void(^BTTouchMoveInside)();
typedef void(^BTTouchEnd)(BOOL inside);

@interface BTTouchDownGestureRecognizer : UIGestureRecognizer
@property(nonatomic, copy)BTTouchDown touchDown;
@property(nonatomic, copy)BTTouchMoveOutside moveOutside;
@property(nonatomic, copy)BTTouchMoveInside moveInside;
@property(nonatomic, copy)BTTouchEnd touchEnd;
@end
