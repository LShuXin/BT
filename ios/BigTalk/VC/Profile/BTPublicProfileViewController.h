//
//  BTPublicProfileViewController.h
//

#import <UIKit/UIKit.h>
#import "BTPublicDefine.h"
#import "BTRootViewController.h"


@class BTUserEntity;

@interface BTPublicProfileViewController : BTRootViewController<UITableViewDataSource, UITableViewDelegate>
@property(strong)BTUserEntity *user;
@end
