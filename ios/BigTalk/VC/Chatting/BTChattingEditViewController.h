//
//  BTChattingEditViewController.h
//

#import <UIKit/UIKit.h>
#import "BTSessionEntity.h"
#import "BTGroupEntity.h"


@interface BTChattingEditViewController : BTRootViewController<UITableViewDataSource,
                                                               UITableViewDelegate,
                                                               UICollectionViewDataSource,
                                                               UICollectionViewDelegate>
@property(assign)BOOL isGroup;
@property(strong)NSString *groupName;
@property(nonatomic, strong)NSMutableArray *items;
@property(strong)BTSessionEntity *session;
@property(strong)BTGroupEntity *group;
-(void)refreshUsers:(NSMutableArray *)array;
@end
