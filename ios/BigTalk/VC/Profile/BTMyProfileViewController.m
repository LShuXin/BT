//
//  BTMyProfileViewController.m
//

#import "BTMyProfileViewController.h"
#import "BTPublicProfileViewController.h"
#import "BTRuntimeStatus.h"
#import "UIImageView+WebCache.h"
#import "BTUserDetailInfoAPI.h"
#import "BTPhotosCache.h"
#import "BTLogoutMsgServerAPI.h"
#import "BTLoginViewController.h"
#import "BTClientState.h"
#import "BTUserModule.h"
#import "BTDatabaseUtil.h"
#import "NSString+BTAdditions.h"
#import "BTConsts.h"


@interface BTMyProfileViewController()

@end

@implementation BTMyProfileViewController

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        // Custom initialization
    }
    return self;
}

-(void)viewDidLoad
{
    [self initView];
}

-(void)initView
{
    [super viewDidLoad];
    self.title = @"我";
    
    UIImage *placeholder = [UIImage imageNamed:@"user_placeholder"];
    _avatar = [[UIImageView alloc] initWithFrame:CGRectMake(15,
                                                            BTNavBarAndStatusBarHeight + 22,
                                                            80,
                                                            80)];
    [self.avatar sd_setImageWithURL:[NSURL URLWithString:[[BTRuntimeStatus instance].user getAvatarUrl]]
                   placeholderImage:placeholder];
    // https://developer.apple.com/documentation/quartzcore/calayer/1410896-maskstobounds?language=objc
    [_avatar.layer setMasksToBounds:YES];
    [_avatar.layer setCornerRadius:4];
    // https://developer.apple.com/documentation/uikit/uiview/1622577-userinteractionenabled
    _avatar.userInteractionEnabled = true;
    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                action:@selector(goPersonalProfile)];
    [_avatar addGestureRecognizer:singleTap];
    [self.view addSubview:_avatar];

    
    _nickName = [[UILabel alloc] initWithFrame:CGRectZero];
    _nickName.translatesAutoresizingMaskIntoConstraints = NO;
    [_nickName setFont: [UIFont systemFontOfSize: 20]];
    [self.view addSubview:_nickName];
    [[_nickName.topAnchor constraintEqualToAnchor:_avatar.topAnchor constant:30] setActive:YES];
    [[_nickName.leadingAnchor constraintEqualToAnchor:_avatar.trailingAnchor constant:8] setActive:YES];
    
    
    _realName = [[UILabel alloc] initWithFrame:CGRectZero];
    _realName.translatesAutoresizingMaskIntoConstraints = NO;
    _realName.textColor = BTColorGray5;
    [_realName setFont: [UIFont systemFontOfSize: 14]];
    [self.view addSubview:_realName];
    [[_realName.bottomAnchor constraintEqualToAnchor:_avatar.bottomAnchor] setActive:YES];
    [[_realName.leadingAnchor constraintEqualToAnchor:_avatar.trailingAnchor constant:8] setActive:YES];
    
    
    [[BTUserModule shareInstance] getUserByUserId:[BTRuntimeStatus instance].user.objId completion:^(BTUserEntity *user) {
        self.user = user;
        self.realName.text = user.name;
        self.nickName.text = user.nick;
    }];
    

    _tableView = [[UITableView alloc] initWithFrame:CGRectZero];
    _tableView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:_tableView];
    [[_tableView.topAnchor constraintEqualToAnchor:_avatar.bottomAnchor constant: 28] setActive:YES];
    [[_tableView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:0] setActive:YES];
    [[_tableView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-16] setActive:YES];
    [[_tableView.bottomAnchor constraintEqualToAnchor:_tableView.topAnchor constant:150] setActive:YES];
    [_tableView setDelegate:self];
    [_tableView setDataSource:self];
    [self.view addSubview:_tableView];
    
    
    _versionLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _versionLabel.textAlignment = UITextAlignmentCenter;
    [_versionLabel setText:BTConcatStringWith(@"version: ", [NSString formatCurDayForVersion]]);
    [self.view addSubview:_versionLabel];
    _versionLabel.translatesAutoresizingMaskIntoConstraints = false;
    [[_versionLabel.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor] setActive:YES];
    [[_versionLabel.centerYAnchor constraintEqualToAnchor:_tableView.bottomAnchor constant: 20] setActive:YES];
    _versionLabel.font = [UIFont italicSystemFontOfSize:14];
    _versionLabel.textColor = BTColorGray5;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.tabBarController.tabBar setHidden:NO];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 2;
}

// Row display. Implementers should *always* try to reuse cells by setting each cell's reuseIdentifier and querying for available reusable cells with dequeueReusableCellWithIdentifier:
// Cell gets various attributes set automatically based on table (separators) and data source (accessory views, editing controls)
-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *identifier = @"MyProfileCellIdentifier";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell)
    {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:identifier];
    }
    cell.selectedBackgroundView = [[UIView alloc] initWithFrame:cell.frame];
    cell.selectedBackgroundView.backgroundColor = RGB(244, 245, 246);
    NSInteger row = [indexPath row];
    if (row == 0)
    {
        [cell.textLabel setText:@"清理缓存图片"];
    }
    else if (row == 1)
    {
        [cell.textLabel setText:@"退出"];
    }
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (indexPath.row == 0)
    {
        [self clearCache];
    }
    else
    {
        [self logout];
    }
}

-(void)clearCache
{
    __block SCLAlertView *alert = [SCLAlertView new];
    [alert addButton:@"确定" actionBlock:^{
        SCLAlertView *cleaning = [SCLAlertView new];
        [cleaning showWaiting:self title:nil subTitle:@"正在清理" closeButtonTitle:nil duration:0];
        [[BTPhotosCache sharedPhotoCache] clearPhotoCacheCompletion:^(bool ok) {
            if (ok)
            {
                [cleaning hideView];
                SCLAlertView *notice = [SCLAlertView new];
                [notice showSuccess:self title:nil subTitle:@"清理完成" closeButtonTitle:nil duration:2.0];
            }
        }];
    }];
    [alert showNotice:self title:@"提示" subTitle:@"是否清理图片缓存" closeButtonTitle:@"取消" duration:0];
}

-(void)logout
{
    SCLAlertView *alert = [SCLAlertView new];
    [alert addButton:@"确定" actionBlock:^{
        BTLogoutMsgServerAPI *logout = [BTLogoutMsgServerAPI new];
        [logout requestWithObject:nil completion:^(id response, NSError *error) {
            
        }];
        [BTNotificationHelper postNotification:BTNotificationUserLogout userInfo:nil object:nil];
        BTLoginViewController *loginVC = [BTLoginViewController new];
        loginVC.isRelogin = YES;
        [self presentViewController:loginVC animated:YES completion:^{
            BTRuntime.user = nil;
            BTRuntime.userId = nil;
            [BTClientState shareInstance].userState = USER_OFF_LINE_INITIATIVE;
            [[BTTcpClientManager instance] disconnect];
            [[NSUserDefaults standardUserDefaults] setBool:NO forKey:@"autoLogin"];
        }];
    }];
    [alert showNotice:self title:@"提示" subTitle:@"是否确认退出?" closeButtonTitle:@"取消" duration:0];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)goPersonalProfile
{
    BTPublicProfileViewController *publicPrifileVC = [BTPublicProfileViewController new] ;
    publicPrifileVC.user = self.user;
    [self.navigationController pushViewController:publicPrifileVC animated:YES];
}

@end
