//
//  BTRecentUsersViewController.h
//

#import <UIKit/UIKit.h>
#import "BTSessionModule.h"


@class BTRecentUserVCModule;

@interface BTRecentUsersViewController : UIViewController<UITableViewDataSource,
                                                         UITableViewDelegate,
                                                         UIAlertViewDelegate,
                                                         UISearchBarDelegate,
                                                         UISearchResultsUpdating,
                                                         BTSessionModuelDelegate>

@property(nonatomic, weak)IBOutlet UITableView *tableView;
@property(nonatomic, strong)BTRecentUserVCModule *module;
@property(strong)NSMutableArray *items;

+(instancetype)shareInstance;
-(void)moveSessionToTop:(NSString *)sesstionId;
-(void)showLinking;

@end
