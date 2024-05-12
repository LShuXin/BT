//
//  BTMainViewController.m
//

#import "BTMainViewController.h"
#import "BTRecentUsersViewController.h"
#import "BTContactsViewController.h"
#import "BTMyProfileViewController.h"
#import "BTClientStateMaintenanceManager.h"
#import "BTGroupModule.h"
#import "BTFinderViewController.h"
#import "BTLoginViewController.h"
#import "BTPublicDefine.h"
#import "BTSessionEntity.h"


@interface BTMainViewController()
@property(assign)NSUInteger clickCount;
@end


@implementation BTMainViewController

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
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController.navigationBar setHidden:YES];
    [self.tabBarController.tabBar setHidden:YES];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)initView
{
    UIImage *conversationSelected = [[UIImage imageNamed:@"conversation_selected"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    self.nvc1 = [[UINavigationController alloc] initWithRootViewController:[BTRecentUsersViewController shareInstance]];
    self.nvc1.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"消息"
                                                        image:[UIImage imageNamed:@"conversation"]
                                                selectedImage:conversationSelected];
    self.nvc1.tabBarItem.tag = 0;
    [self.nvc1.tabBarItem setTitleTextAttributes:[NSDictionary dictionaryWithObject:RGB(26, 140, 242) forKey:UITextAttributeTextColor]
                                       forState:UIControlStateSelected];
    
    
    UIImage *contactSelected = [[UIImage imageNamed:@"contact_selected"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    UINavigationController *nvc2 = [[UINavigationController alloc] initWithRootViewController:[BTContactsViewController new]];
    nvc2.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"通讯录"
                                                   image:[UIImage imageNamed:@"contact"]
                                           selectedImage:contactSelected];
    nvc2.tabBarItem.tag = 1;
    [nvc2.tabBarItem setTitleTextAttributes:[NSDictionary dictionaryWithObject:RGB(26, 140, 242) forKey:UITextAttributeTextColor]
                                  forState:UIControlStateSelected];
    

    UIImage *findSelected = [[UIImage imageNamed:@"tab_nav_selected"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    UINavigationController *nvc3 = [[UINavigationController alloc] initWithRootViewController:[[BTFinderViewController alloc] init]];
    nvc3.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"发现"
                                                   image:[UIImage imageNamed:@"tab_nav"]
                                           selectedImage:findSelected];
    nvc3.tabBarItem.tag = 2;
    [nvc3.tabBarItem setTitleTextAttributes:[NSDictionary dictionaryWithObject:RGB(26, 140, 242) forKey:UITextAttributeTextColor]
                                  forState:UIControlStateSelected];

    
    UIImage *myProfileSelected = [[UIImage imageNamed:@"myprofile_selected"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    UINavigationController *nvc4 = [[UINavigationController alloc] initWithRootViewController:[BTMyProfileViewController new]];
    nvc4.tabBarItem = [[UITabBarItem alloc] initWithTitle:@"我的"
                                                   image:[UIImage imageNamed:@"myprofile"]
                                           selectedImage:myProfileSelected];
    nvc4.tabBarItem.tag = 3;
    [nvc4.tabBarItem setTitleTextAttributes:[NSDictionary dictionaryWithObject:RGB(26, 140, 242) forKey:UITextAttributeTextColor]
                                  forState:UIControlStateSelected];
    
    self.viewControllers = @[self.nvc1, nvc2, nvc3, nvc4];
    self.delegate = self;
    self.tabBar.translucent = NO;
}

-(UIStatusBarStyle)preferredStatusBarStyle
{
    return UIStatusBarStyleDefault;
}

-(void)tabBar:(UITabBar *)tabBar didSelectItem:(UITabBarItem *)item
{
    self.navigationController.navigationBarHidden = YES;
    if ([self.nvc1.tabBarItem isEqual:item])
    {
        self.clickCount = self.clickCount + 1;
        if (self.clickCount == 2)
        {
            if ([[[BTRecentUsersViewController shareInstance].tableView visibleCells] count] > 0)
            {
                [[BTRecentUsersViewController shareInstance].items enumerateObjectsUsingBlock:^(BTSessionEntity *obj, NSUInteger idx, BOOL *stop) {
                    if (obj.unReadMsgCount)
                    {
                        [[BTRecentUsersViewController shareInstance].tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:idx inSection:0]
                                                                                   atScrollPosition:UITableViewScrollPositionTop
                                                                                           animated:YES];
                        return;
                    }
                }];
                
            }
            self.clickCount = 0;
        }
    }
    else
    {
        self.clickCount = 0;
    }
}

-(void)setSelectIndex:(int)index
{
}

@end
