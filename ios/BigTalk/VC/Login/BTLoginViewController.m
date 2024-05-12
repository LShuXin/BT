//
//  BTLoginViewController.m
//

#import "BTLoginViewController.h"
#import "BTPublicDefine.h"
#import "BTChattingMainViewController.h"
#import "BTRecentUsersViewController.h"
#import "BTClientStateMaintenanceManager.h"
#import "BTUserModule.h"
#import "BTMessageModule.h"
#import "BTLoginModule.h"
#import "BTSendPushTokenAPI.h"
#import "BTNotificationHelper.h"
#import "BTPublicDefine.h"
#import "BTAppDelegate.h"
#import "BTContactsModule.h"
#import "BTRuntimeStatus.h"
#import "BTMainViewController.h"
#import "BTDatabaseUtil.h"
#import "BTGroupModule.h"
#import "MBProgressHUD.h"


@interface BTLoginViewController()
@property(assign)CGPoint defaultCenter;
@end

@implementation BTLoginViewController

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
    [super viewDidLoad];
    [self initView];
    [self initData];
}

-(void)initView
{
    [self.view setBackgroundColor:[UIColor whiteColor]];
    
    
    _landspace = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"logo.png"]];
    _landspace.frame = CGRectMake(BTScreenWidth / 2 - 60.0,
                                  BTNavBarAndStatusBarHeight + 30.0,
                                  120.0,
                                  120.0);
    _landspace.clipsToBounds = YES;
    _landspace.layer.cornerRadius = 20.0;
    [self.view addSubview:_landspace];
    
    
    _userNameTextField = [[UITextField alloc] initWithFrame:CGRectZero];
    _userNameTextField.font = [UIFont systemFontOfSize: 20.0];
    _userNameTextField.borderStyle = UITextBorderStyleRoundedRect;
    UIView *usernameLeftView = [[UIView alloc] init];
    usernameLeftView.contentMode = UIViewContentModeCenter;
    usernameLeftView.frame = CGRectMake(0, 0, 4, 26);
    _userNameTextField.leftView = usernameLeftView;
    _userNameTextField.leftViewMode = UITextFieldViewModeAlways;
    [_userNameTextField.layer setBorderColor:RGB(211, 211, 211).CGColor];
    [_userNameTextField.layer setBorderWidth:0.5];
    [_userNameTextField.layer setCornerRadius:4];
    [self.view addSubview:_userNameTextField];
    _userNameTextField.translatesAutoresizingMaskIntoConstraints = NO;
    [NSLayoutConstraint activateConstraints:@[
        [_userNameTextField.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [_userNameTextField.topAnchor constraintEqualToAnchor:_landspace.bottomAnchor constant:60.0],
        [_userNameTextField.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:50.0],
        [_userNameTextField.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-50.0],
    ]];
    _userNameTextField.autocapitalizationType = UITextAutocapitalizationTypeNone;

    
    _userPassTextField = [[UITextField alloc] initWithFrame:CGRectZero];
    _userPassTextField.font = [UIFont systemFontOfSize:20.0];
    _userPassTextField.borderStyle = UITextBorderStyleRoundedRect;
    UIView *pwdLeftView = [[UIView alloc] init];
    pwdLeftView.contentMode = UIViewContentModeCenter;
    pwdLeftView.frame = CGRectMake(0, 0, 4, 26);
    _userPassTextField.leftView = pwdLeftView;
    _userPassTextField.leftViewMode = UITextFieldViewModeAlways;
    [_userPassTextField.layer setBorderColor:RGB(211, 211, 211).CGColor];
    [_userPassTextField.layer setBorderWidth:0.5];
    [_userPassTextField.layer setCornerRadius:4];
    [self.view addSubview:_userPassTextField];
    _userPassTextField.translatesAutoresizingMaskIntoConstraints = NO;
    [NSLayoutConstraint activateConstraints:@[
        [_userPassTextField.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [_userPassTextField.topAnchor constraintEqualToAnchor:_userNameTextField.bottomAnchor constant:8.0],
        [_userPassTextField.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor constant:50.0],
        [_userPassTextField.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor constant:-50.0],
    ]];
    _userPassTextField.autocapitalizationType = UITextAutocapitalizationTypeNone;

    
    _userLoginBtn = [[UIButton alloc] initWithFrame:CGRectZero];
    [_userLoginBtn setBackgroundColor:BTColorCyan6];
    [_userLoginBtn setTitle:@"登录" forState:UIControlStateNormal];
    _userLoginBtn.translatesAutoresizingMaskIntoConstraints = NO;
    _userLoginBtn.contentEdgeInsets = UIEdgeInsetsMake(10.0, 28.0, 10.0, 28.0);
    [self.view addSubview:_userLoginBtn];
    [NSLayoutConstraint activateConstraints:@[
        [_userLoginBtn.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor],
        [_userLoginBtn.centerYAnchor constraintEqualToAnchor:_userPassTextField.bottomAnchor constant:60.0],
    ]];
    [_userLoginBtn.layer setCornerRadius:4];
    [_userLoginBtn addTarget:self action:@selector(login) forControlEvents:UIControlEventTouchUpInside];
    
    self.defaultCenter = self.view.center;
    
    
    UITapGestureRecognizer *pageTapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hiddenKeyboard)];
    pageTapGestureRecognizer.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:pageTapGestureRecognizer];
}

-(void)initData
{
    if ([[NSUserDefaults standardUserDefaults] objectForKey:@"username"] != nil)
    {
        _userNameTextField.text = [[NSUserDefaults standardUserDefaults] objectForKey:@"username"];
    }
    
    if ([[NSUserDefaults standardUserDefaults] objectForKey:@"password"] != nil)
    {
        _userPassTextField.text = [[NSUserDefaults standardUserDefaults] objectForKey:@"password"];
    }
    
    if (!self.isRelogin)
    {
        if (
            [[NSUserDefaults standardUserDefaults] objectForKey:@"username"]
            &&
            [[NSUserDefaults standardUserDefaults] objectForKey:@"password"]
            )
        {
            if ([[[NSUserDefaults standardUserDefaults] objectForKey:@"autoLogin"] boolValue] == YES)
            {
                [self login];
            }
        }
    }
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleWillShowKeyboard)
                                                 name:UIKeyboardWillShowNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleWillHideKeyboard)
                                                 name:UIKeyboardWillHideNotification
                                               object:nil];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

-(void)handleWillShowKeyboard
{
    [UIView animateWithDuration:0.2 animations:^{
        self.view.center = CGPointMake(
                                       self.view.center.x,
                                       self.defaultCenter.y - (BTIsIPhone4 ? 120 : 40));
    }];
}

-(void)handleWillHideKeyboard
{
    [UIView animateWithDuration:0.2 animations:^{
        self.view.center = self.defaultCenter;
    }];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)hiddenKeyboard
{
    [_userNameTextField resignFirstResponder];
    [_userPassTextField resignFirstResponder];
}

-(void)login
{
    [self.userLoginBtn setEnabled:NO];
    NSString *userName = [_userNameTextField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    NSString *password = [_userPassTextField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    if (userName.length == 0 || password.length == 0)
    {
        [self.userLoginBtn setEnabled:YES];
        return;
    }
    
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:self.view];
    [self.view addSubview:hud];
    [hud show:YES];
    hud.dimBackground = YES;
    hud.labelText = @"正在登录";
    SCLAlertView *alert = [SCLAlertView new];
    
    [[BTLoginModule instance] loginWithUsername:userName password:password success:^(BTUserEntity *user) {
        [self.userLoginBtn setEnabled:YES];
        if (user)
        {
            BTRuntime.user = user ;
            [BTRuntime updateData];
            
            if (BTRuntime.pushToken)
            {
                BTSendPushTokenAPI *pushToken = [[BTSendPushTokenAPI alloc] init];
                [pushToken requestWithObject:BTRuntime.pushToken completion:^(id response, NSError *error) {
                    
                }];
            }
            if (self.isRelogin)
            {
                BTAppDel.mainViewController = nil;
                BTAppDel.window.rootViewController = [BTMainViewController new];
                [self dismissViewControllerAnimated:YES completion:^{
                    
                }];
            }
            else
            {
                [BTGroupModule instance];
                [self.navigationController setViewControllers:@[BTAppDel.mainViewController] animated:YES];
            }
        }
    } failure:^(NSString *error) {
        [self.userLoginBtn setEnabled:YES];
        [hud removeFromSuperview];
        
        [alert showError:self title:@"错误" subTitle:error closeButtonTitle:@"确定" duration:0];
    }];
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    [self login];
    return YES;
}

-(void)showEditServerAddress
{
    SCLAlertView *alert = [SCLAlertView new];
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    UITextField *addressField = [alert addTextField:@"请填写服务器地址"];
    addressField.text = [defaults objectForKey:@"ipAddress"];
    [alert addButton:@"确定" actionBlock:^{
        [defaults setObject:addressField.text forKey:@"ipAddress"];
    }];
    [alert showEdit:self
              title:@"编辑服务器地址"
           subTitle:@"请填写你的服务器地址，或使用我们的测试服务器"
   closeButtonTitle:@"关闭"
           duration:0];
}

@end
