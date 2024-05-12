//
//  BTMainViewController.h
//

#import <UIKit/UIKit.h>
#import "BTContactsViewController.h"


@interface BTMainViewController : UITabBarController<UITabBarControllerDelegate, UITabBarDelegate>

@property(strong)UINavigationController *nvc1;
@property(strong)BTContactsViewController *contacts;

-(void)setSelectIndex:(int)index;

@end
