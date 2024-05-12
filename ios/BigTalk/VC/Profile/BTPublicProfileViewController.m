//
//  BTPublicProfileViewController.m
//

#import "BTPublicProfileViewController.h"
#import "BTUserEntity.h"
#import "BTSessionEntity.h"
#import "UIImageView+WebCache.h"
#import "BTContactsModule.h"
#import "UIImageView+WebCache.h"
#import "BTChattingMainViewController.h"
#import "BTRuntimeStatus.h"
#import "BTUserDetailInfoAPI.h"
#import "BTDatabaseUtil.h"
#import "BTAppDelegate.h"
#import "BTUserModule.h"
#import "BTPublicProfileCell.h"


@interface BTPublicProfileViewController()
@property(nonatomic, retain)UILabel *nickName;
@property(nonatomic, retain)UILabel *realName;
@property(nonatomic, retain)UIImageView *avatar;
@property(nonatomic, retain)UITableView *tableView;
@property(nonatomic, retain)UIButton *conversationBtn;
@end


@implementation BTPublicProfileViewController

-(instancetype)init
{
    self = [super init];
    if (self)
    {

    }
    return self;
}


-(void)viewDidLoad
{
    [super viewDidLoad];
    [self initView];
}

-(void)initView
{
    [self setTitle:@"详细资料"];
    self.view.backgroundColor = [UIColor whiteColor];
    
    UIImage *placeholder = [UIImage imageNamed:@"user_placeholder"];
    _avatar = [[UIImageView alloc] initWithFrame:CGRectMake(40,
                                                            80,
                                                            80,
                                                            80)];
    [_avatar sd_setImageWithURL:[NSURL URLWithString:[self.user getAvatarUrl]]
               placeholderImage:placeholder];
    [self.avatar setClipsToBounds:YES];
    [self.avatar.layer setCornerRadius:7.5];
    [self.avatar setUserInteractionEnabled:YES];
    [self.view addSubview:_avatar];
    
    _nickName = [[UILabel alloc] initWithFrame:CGRectMake(40 + 80 + 6,
                                                          80 + 80 - 48,
                                                          100.0,
                                                          20.0)];
    _nickName.text = self.user.nick;
    [self.view addSubview:_nickName];
    
    
    _realName = [[UILabel alloc] initWithFrame:CGRectMake(40 + 80 + 16,
                                                          80 + 80 - 20.0,
                                                          100.0,
                                                          20.0)];
    _realName.text = self.user.name;
    [self.view addSubview:_realName];
    
    
    _conversationBtn = [[UIButton alloc] initWithFrame:CGRectMake(BTScreenWidth / 2 - 20,
                                                                  BTScreenHeight - 50,
                                                                  40,
                                                                  30)];
    [_conversationBtn setTitle: @"发消息" forState:UIControlStateNormal];
    // A Boolean indicating whether sublayers are clipped to the layer’s bounds. Animatable.
    _conversationBtn.layer.masksToBounds = YES;
    _conversationBtn.layer.cornerRadius = 4;
    if ([self.user.objId isEqualToString:BTRuntime.user.objId])
    {
        [_conversationBtn setHidden:YES];
    }
    else
    {
        [_conversationBtn setHidden:NO];
    }
    [self.view addSubview:_conversationBtn];
    
    
    _tableView = [[UITableView alloc] init];
    [self.view addSubview:_tableView];
    _tableView.translatesAutoresizingMaskIntoConstraints = NO;
    NSLayoutConstraint *topConstraint = [_tableView.topAnchor constraintEqualToAnchor:self.view.topAnchor constant:80 + 80 + 12];
    NSLayoutConstraint *bottomConstraint = [_tableView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor constant:100]; // 让tableView延伸至底部
    NSLayoutConstraint *leadingConstraint = [_tableView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor];
    NSLayoutConstraint *trailingConstraint = [_tableView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor];
    [topConstraint setActive:YES];
    [bottomConstraint setActive:YES];
    [leadingConstraint setActive:YES];
    [trailingConstraint setActive:YES];
    [_tableView setContentInset:UIEdgeInsetsMake(0, 0, 0, 0)];
    UIView *view = [UIView new];
    view.backgroundColor = [UIColor clearColor];
    [self.tableView setTableFooterView:view];
    [self.tableView setTableHeaderView:view];
    [_tableView setDelegate:self];
    [_tableView setDataSource:self];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.tabBarController.tabBar setHidden:YES];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.tabBarController.tabBar setHidden:NO];
}

-(void)favThisContact
{
    [BTContactsModule favContact:self.user];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 3;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *cellIdentifier = @"PublicProfileCell";
    BTPublicProfileCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier ];
    if (cell == nil)
    {
        cell = [[BTPublicProfileCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
    switch (indexPath.row)
    {
        case 0:
            {
                [[BTDatabaseUtil instance] getDepartmentTitleByDepartmentId:self.user.departId completion:^(NSString *title) {
                     [cell setDesc:@"部门" detail:title];
                }];
                cell.userInteractionEnabled = NO;
                [cell hidePhone:YES];
            }
            break;
        case 1:
            {
                [cell setDesc:@"手机" detail:self.user.telphone];
                [cell hidePhone:NO];
            }
            break;
        case 2:
            {
                [cell setDesc:@"邮箱" detail:self.user.email];
                [cell hidePhone:YES];
            }
            break;
        default:
            break;
    }
    
    return cell;
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == 100)
    {
        if (buttonIndex == 1)
        {
            [self callPhoneNum:self.user.telphone];
        }
    }
    else
    {
        if (buttonIndex == 1)
        {
            [self sendEmail:self.user.email];
        }
    }
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    switch (indexPath.row)
    {
        case 1:
            {
                NSString *alertMsg;
                alertMsg = [NSString stringWithFormat:@"呼叫%@?", self.user.telphone];
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"提示"
                                                                message:alertMsg
                                                               delegate:self
                                                      cancelButtonTitle:@"取消"
                                                      otherButtonTitles:@"确定", nil];
                alert.tag = 100;
                [alert show];
            }
            break;
        case 2:
            {
//                NSString *alertMsg;
//                alertMsg = [NSString stringWithFormat:@"发送邮件%@?", self.user.email];
//                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"提示"
//                                                                message:alertMsg
//                                                               delegate:self
//                                                      cancelButtonTitle:@"取消"
//                                                      otherButtonTitles:@"确定", nil];
//                alert.tag = 101;
//                [alert show];
                
                [self startConversation];
            }
            break;
        default:
            break;
    }
}

-(void)callPhoneNum:(NSString *)phoneNum
{
    if (!phoneNum)
    {
        return;
    }
    NSString *stringURL = [NSString stringWithFormat:@"tel:%@", phoneNum];
    NSURL *url = [NSURL URLWithString:stringURL];
    [[UIApplication sharedApplication] openURL:url];
}

-(void)sendEmail:(NSString *)address
{
    if (!address.length)
    {
        return;
    }
    NSString *stringURL = [NSString stringWithFormat:@"mailto:%@", address];
    NSURL *url = [NSURL URLWithString:stringURL];
    [[UIApplication sharedApplication] openURL:url];
}

-(void)startConversation
{
    BTSessionEntity *session = [[BTSessionEntity alloc] initWithSessionId:self.user.objId sessionType:SessionType_SessionTypeSingle];
    [[BTChattingMainViewController shareInstance] showChattingContentForSession:session];
    if ([[self.navigationController viewControllers] containsObject:[BTChattingMainViewController shareInstance]])
    {
        [self.navigationController popToViewController:[BTChattingMainViewController shareInstance] animated:YES];
    }
    else
    {
        [self.navigationController pushViewController:[BTChattingMainViewController shareInstance] animated:YES];
    }
}

/*设置标题头的宽度*/
-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 0;
}

/*设置标题尾的宽度*/
-(CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return 0;
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
