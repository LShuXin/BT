//
//  BTRecentUsersViewController.m
//

#import "BTRecentUsersViewController.h"
#import "BTRecentUserCell.h"
#import "BTUserModule.h"
#import "BTMessageModule.h"
#import "BTChattingMainViewController.h"
#import "BTSessionEntity.h"
#import "BTPublicDefine.h"
#import "BTDatabaseUtil.h"
#import "BTLoginModule.h"
#import "BTClientState.h"
#import "BTRuntimeStatus.h"
#import "BTGroupModule.h"
#import "BTFixedGroupAPI.h"
#import "BTSearchContentViewController.h"
#import "MBProgressHUD.h"
#import "BTSessionModule.h"
#import "BTBlurView.h"
#import "BTLoginViewController.h"
#import "BTNotificationHelper.h"
#import "BTConsts.h"


@interface BTRecentUsersViewController()
@property(strong)UISearchController *searchController;
@property(strong)MBProgressHUD *hud;
@property(strong)NSMutableDictionary *lastMsgs;
@property(weak)IBOutlet UISearchBar *bar;
@property(strong)BTSearchContentViewController *searchContent;
@property(assign)NSInteger fixedCount;
-(void)n_receiveStartLoginNotification:(NSNotification *)notification;
-(void)n_receiveLoginFailureNotification:(NSNotification *)notification;
-(void)n_receiveRecentContactsUpdateNotification:(NSNotification *)notification;
@end


@implementation BTRecentUsersViewController

+(instancetype)shareInstance
{
    static BTRecentUsersViewController *g_recentUsersViewController;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_recentUsersViewController = [BTRecentUsersViewController new];
    });
    return g_recentUsersViewController;
}

-(id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(n_receiveLoginFailureNotification:)
                                                     name:BTNotificationUserLoginFailure
                                                   object:nil];
    }
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(n_receiveLoginNotification:)
                                                 name:BTNotificationUserLoginSuccess
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(kickOffUser:)
                                                 name:@"KickOffUser"
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(logout)
                                                 name:BTNotificationUserLogout
                                               object:nil];
    return self;
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = @"消息";
    self.navigationItem.title = @"消息";
    [self.navigationController.navigationBar setBarStyle:UIBarStyleDefault];
    // 全屏
    self.edgesForExtendedLayout = UIRectEdgeAll;
    self.extendedLayoutIncludesOpaqueBars = YES;
    
    self.items = [NSMutableArray new];
    [_tableView setFrame:self.view.frame];
    self.tableView.contentOffset = CGPointMake(0, CGRectGetHeight(self.bar.bounds));
    [self.tableView setContentInset:UIEdgeInsetsMake(0, 0, 0, 0)];
    [self.tableView setBackgroundColor:RGB(239,239,244)];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(refreshData)
                                                 name:@"RefreshRecentData"
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(n_receiveReLoginSuccessNotification)
                                                 name:BTNotificationUserReloginSuccess
                                               object:nil];
    self.lastMsgs = [NSMutableDictionary new];
    [[BTSessionModule sharedInstance] loadLocalSession:^(bool isok) {
        if (isok)
        {
            // 加载本地会话
            [self.items addObjectsFromArray:[[BTSessionModule sharedInstance] getAllSessions]];
            [self.tableView reloadData];
            
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                // 更新最近会话
                [[BTSessionModule sharedInstance] getRecentSession:^(NSUInteger count) {
                    [self.items removeAllObjects];
                    [self.items addObjectsFromArray:[[BTSessionModule sharedInstance] getAllSessions]];
                    [self sortItems];
                    NSUInteger unreadcount = [[self.items valueForKeyPath:@"@sum.unReadMsgCount"] integerValue];
                    // 更新未读消息数
                    [self setToolbarBadge:unreadcount];
                }];
            });
        }
    }];
    [BTSessionModule sharedInstance].delegate = self;
    [self addCustomSearchController];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    NSUInteger count = [[self.items valueForKeyPath:@"@sum.unReadMsgCount"] integerValue];
    [self setToolbarBadge:count];
    [self.tableView reloadData];
    self.fixedCount = [BTRuntime getFixedTopCount];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.tabBarController.tabBar setHidden:NO];
    [BTChattingMainViewController shareInstance].module.sessionEntity = nil;
   
    if (!self.items)
    {
        self.items = [NSMutableArray new];
        [_tableView setFrame:self.view.frame];
        self.lastMsgs = [NSMutableDictionary new];
        [[BTSessionModule sharedInstance] loadLocalSession:^(bool isok) {
            if (isok)
            {
                [self.items addObjectsFromArray:[[BTSessionModule sharedInstance] getAllSessions]];
                [self.tableView reloadData];
                
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    [[BTSessionModule sharedInstance] getRecentSession:^(NSUInteger count) {
                        [self.items removeAllObjects];
                        [self.items addObjectsFromArray:[[BTSessionModule sharedInstance] getAllSessions]];
                        [self sortItems];
                        NSUInteger unreadcount = [[self.items valueForKeyPath:@"@sum.unReadMsgCount"] integerValue];
                        [self setToolbarBadge:unreadcount];
                    }];
                });
            }
        }];
    }
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}


#pragma mark - UISearchResultsUpdating
// 这个方法通常在你希望在用户输入搜索文本时实时更新搜索结果的场景中使用
-(void)updateSearchResultsForSearchController:(UISearchController *)searchController {
    NSString *searchString = searchController.searchBar.text;
    BTLog(@"search content change");

//    [self.searchContent searchTextDidChanged:searchString Block:^(BOOL done) {
//        BTLog(@"search content changed: %d", done);
//    }];
}





#pragma mark - UISearchBarDelegate
-(BOOL)searchBarShouldBeginEditing:(UISearchBar*)searchBar
{
    [self.searchController setActive:YES];
    return YES;
}

-(void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)searchText
{
//    [self.searchContent searchTextDidChanged:searchText Block:^(bool done) {
//        [self.searchContent reloadData];
//    }];
}

-(void)searchBar:(UISearchBar *)searchBar selectedScopeButtonIndexDidChange:(NSInteger)selectedScope {
    [self updateSearchResultsForSearchController:self.searchController];
}




#pragma mark - UITableView DataSource
-(NSInteger)numberOfSectionsInTableView:(UITableView*)tableView
{
    return 1;
}

-(NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.items count];
}

-(CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(NSIndexPath*)indexPath
{
    return 72;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *cellIdentifier = [NSString stringWithFormat:@"BTRecentUserCellIdentifier_%ld", indexPath.row];
    BTRecentUserCell *cell = (BTRecentUserCell *)[tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (!cell)
    {
        NSArray *topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"BTRecentUserCell" owner:self options:nil];
        cell = [topLevelObjects objectAtIndex:0];
    }
   
    UIView *view = [[UIView alloc] initWithFrame:cell.bounds];
    view.backgroundColor = RGB(229, 229, 229);
    cell.selectedBackgroundView = view;
    NSInteger row = [indexPath row];
    [cell setShowSession:self.items[row]];
    [self preLoadMessage:self.items[row]];
 
    return cell;
}


#pragma mark - UITableView Delegate
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    NSInteger row = [indexPath row];
    BTSessionEntity *session = self.items[row];
    [BTChattingMainViewController shareInstance].title = session.name;
    [[BTChattingMainViewController shareInstance] showChattingContentForSession:session];
    [self.navigationController pushViewController:[BTChattingMainViewController shareInstance] animated:YES];
}

-(void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    BTSessionEntity *session = self.items[row];
    [[BTSessionModule sharedInstance] removeSessionByServer:session];
    [self.items removeObjectAtIndex:row];
    [self setToolbarBadge:[[self.items valueForKeyPath:@"@sum.unReadMsgCount"] integerValue]];
    [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationRight];
}

-(BOOL)tableView:(UITableView*)tableView canEditRowAtIndexPath:(NSIndexPath*)indexPath
{
    return YES;
}

-(NSString *)tableView:(UITableView *)tableView titleForDeleteConfirmationButtonForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return @"删除";
}


#pragma mark - BTSessionModuelDelegate
-(void)sessionUpdate:(BTSessionEntity *)session action:(BTSessionAction)action
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.items containsObject:session])
        {
            // 会话在第一个位置，此时先删除后添加
            if ([self.items indexOfObject:session] == 0)
            {
                [self.items removeObjectAtIndex:0];
                [self.items insertObject:session atIndex:0];
                [self.tableView reloadData];
            }
            else
            {
                // 会话在中间位置，此时从中间删除，然后添加到第一个
                NSUInteger index = [self.items indexOfObject:session];
                [self.items removeObjectAtIndex:index];
                [self.items insertObject:session atIndex:0];
                [self.tableView reloadData];
            }
        }
        else
        {
            // 会话不存在，直接插在第一个
            [self.items insertObject:session atIndex:0];
            @try
            {
                [self.tableView reloadData];
            }
            @catch (NSException *exception)
            {
                BTLog(@"插入cell 动画失败");
            }
        }
        
        NSUInteger count = [[self.items valueForKeyPath:@"@sum.unReadMsgCount"] integerValue];
        [self setToolbarBadge:count];
    });
}


#pragma mark - Custom Methods
-(void)n_receiveLoginFailureNotification:(NSNotification *)notification
{
    self.title = @"未连接";
}

-(void)n_receiveStartLoginNotification:(NSNotification*)notification
{
     self.title = @"正在登录...";
}

-(void)n_receiveLoginNotification:(NSNotification *)notification
{
    self.title = @"消息";
}

-(void)logout
{
    self.items = nil;
}

// 被踢下线的通知
-(void)kickOffUser:(NSNotification *)notification
{
    [[NSUserDefaults standardUserDefaults] setObject:@(false) forKey:@"autoLogin"];
    BTLoginViewController *login = [BTLoginViewController new];
    login.isRelogin = YES;
    
    [self presentViewController:login animated:YES completion:^{
        BTRuntime.user = nil;
        BTRuntime.userId = nil;
        [[BTTcpClientManager instance] disconnect];
        [BTClientState shareInstance].userState = USER_OFF_LINE_INITIATIVE;
        SCLAlertView *alert = [SCLAlertView new];
        [alert showInfo:self title:@"注意" subTitle:@"你的账号在其他设备登陆了" closeButtonTitle:@"确定" duration:0];
    }];
}

-(void)moveSessionToTop:(NSString*)sesstionId
{

}

-(void)n_receiveReLoginSuccessNotification
{
    self.title = @"BigTalk";
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[BTSessionModule sharedInstance] getRecentSession:^(NSUInteger count) {
            [self.items removeAllObjects];
            [self.items addObjectsFromArray:[[BTSessionModule sharedInstance] getAllSessions]];
            [self sortItems];
            [self setToolbarBadge:count];
        }];
    });
}

-(void)n_receiveRecentContactsUpdateNotification:(NSNotification*)notification
{

}

-(void)preLoadMessage:(BTSessionEntity *)session
{
    [[BTDatabaseUtil instance] getLastestMessageBySessionId:session.sessionId completion:^(BTMessageEntity *message, NSError *error) {
        if (message)
        {
            if (message.msgId != session.lastMsgId)
            {
                [[BTMessageModule shareInstance] getMessageFromServerFromMsgId:session.lastMsgId session:session count:20 completion:^(NSMutableArray *array, NSError *error) {
                    [[BTDatabaseUtil instance] insertMessages:array success:^{
                        
                    } failure:^(NSString *error) {
                        
                    }];
                }];
            }
        }
        else
        {
            if (session.lastMsgId != 0)
            {
                [[BTMessageModule shareInstance] getMessageFromServerFromMsgId:session.lastMsgId session:session count:20 completion:^(NSMutableArray *array, NSError *error) {
                    [[BTDatabaseUtil instance] insertMessages:array success:^{
                        
                    } failure:^(NSString *error) {
                        
                    }];
                }];
            }
        }
    }];
}

-(void)addCustomSearchController
{
    self.searchContent = [BTSearchContentViewController new];
    self.searchContent.viewController = self;
    self.searchController = [[UISearchController alloc] initWithSearchResultsController:self];
    self.searchController.searchResultsUpdater = self;
    self.searchController.searchBar.delegate = self;
    [self.searchController.searchBar sizeToFit];
    self.searchController.dimsBackgroundDuringPresentation = NO;
    self.definesPresentationContext = YES;
    self.tableView.tableHeaderView = self.searchController.searchBar;
}

-(void)sortItems
{
    [self.items removeAllObjects];
    [self.items addObjectsFromArray:[[BTSessionModule sharedInstance] getAllSessions]];
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"timeInterval" ascending:NO];
    [self.items sortUsingDescriptors:[NSArray arrayWithObject:sortDescriptor]];
    [self.tableView reloadData];
}

-(void)refreshData
{
    [self.tableView reloadData];
    [self setToolbarBadge:0];
    [self sortItems];
}

-(void)setToolbarBadge:(NSUInteger)count
{
    if (count != 0)
    {
        if (count > 99)
        {
            [self.parentViewController.tabBarItem setBadgeValue:@"99+"];
        }
        else
        {
            [self.parentViewController.tabBarItem setBadgeValue:[NSString stringWithFormat:@"%ld", count]];
        }
    }
    else
    {
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
        [self.parentViewController.tabBarItem setBadgeValue:nil];
    }
}

-(void)searchContact
{
    
}

// #pragma mark public
-(void)showLinking
{
    self.title = @"正在连接...";
//    UIView* titleView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 200, 44)];
//
//    UIActivityIndicatorView* activity = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
//    [activity setFrame:CGRectMake(30, 0, 44, 44)];
//
//    UILabel* linkLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 200, 44)];
//    [linkLabel setTextAlignment:NSTextAlignmentCenter];
//    [linkLabel setText:@"正在连接"];
//
//    [activity startAnimating];
//    [titleView addSubview:activity];
//    [titleView addSubview:linkLabel];
//
//    [self.navigationItem setTitleView:titleView];
}

@end
