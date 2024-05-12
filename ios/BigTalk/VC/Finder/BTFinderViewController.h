//
//  BTFinderViewController.h
//

#import <UIKit/UIKit.h>


@interface BTFinderViewController : UIViewController<UITableViewDataSource, UITableViewDelegate, NSURLConnectionDelegate>

@property(nonatomic, weak)IBOutlet UITableView *tableView;

@end
