//
//  BTContactsViewController.h
//

#import <UIKit/UIKit.h>
#import "BTSessionEntity.h"


@interface BTContactsViewController : UIViewController<UITableViewDataSource,
                                                       UITableViewDelegate,
                                                       UISearchBarDelegate,
                                                       UISearchResultsUpdating,
                                                       UIScrollViewDelegate>

@property(strong)NSString *sectionTitle;
@property(assign)BOOL isSearchResult;

@end
