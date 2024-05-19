//
//  BTEditGroupViewController.h
//

#import <UIKit/UIKit.h>
#import "BTGroupEntity.h"
#import "BTChattingEditViewController.h"


@class BTSessionEntity;

@interface BTEditGroupViewController : BTRootViewController<UISearchBarDelegate,
                                                            UISearchDisplayDelegate,
                                                            UITableViewDataSource,
                                                            UITableViewDelegate,
                                                            UIAlertViewDelegate>

@property(strong)NSMutableArray *users;
@property(copy)NSString *sessionId;
@property(strong)BTSessionEntity *session;
@property(assign)BOOL isGroupCreator;
@property(assign)BOOL isCreate;
@property(weak)BTGroupEntity *group;
@property(strong)BTChattingEditViewController *editController;
@end
