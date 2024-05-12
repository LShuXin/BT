//
//  BTMyProfileViewController.h
//

#import <UIKit/UIKit.h>


@class BTUserEntity;


@interface BTMyProfileViewController : UIViewController<UITableViewDataSource, UITableViewDelegate>

@property(nonatomic, retain)UILabel *nickName;
@property(nonatomic, retain)UILabel *realName;
@property(nonatomic, retain)UIImageView *avatar;
@property(nonatomic, retain)UITableView *tableView;
@property(nonatomic, retain)UILabel *versionLabel;
@property(nonatomic, retain)BTUserEntity *user;

-(void)goPersonalProfile;
-(void)clearCache;
-(void)logout;

@end
