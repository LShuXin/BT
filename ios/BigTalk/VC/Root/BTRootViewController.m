//
//  BTRootViewController.m
//

#import "BTRootViewController.h"


@interface BTRootViewController()

@end


@implementation BTRootViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    UIButton *back = [UIButton buttonWithType:UIButtonTypeCustom];
    back.frame = CGRectMake(0, 0, 60, 40);
    UIImage *image = [UIImage imageNamed:@"top_back"];
    [back setImage:image forState:UIControlStateNormal];
    [back setImageEdgeInsets:UIEdgeInsetsMake(0, 0, 0, 10)];
    [back setTitle:@"返回" forState:UIControlStateNormal];
    [back setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [back addTarget:self action:@selector(p_popViewController) forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithCustomView:back];
    self.navigationItem.backBarButtonItem = backButton;
}

-(void)p_popViewController
{
    [self.navigationController popViewControllerAnimated:YES];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
