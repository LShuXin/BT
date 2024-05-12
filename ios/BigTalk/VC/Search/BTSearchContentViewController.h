//
//  BTSearchContentViewController.h
//

#import <UIKit/UIKit.h>

@interface BTSearchContentViewController : UITableView<UISearchBarDelegate, UITableViewDelegate, UITableViewDataSource>
-(void)searchTextDidChanged:(NSString *)searchText block:(void(^)(bool done))block;
@property(strong)UIViewController *viewController;
@end
