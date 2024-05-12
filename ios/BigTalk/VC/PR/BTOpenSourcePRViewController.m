//
//  BTOpenSourcePRViewController.m
//

#import "BTOpenSourcePRViewController.h"


@interface BTOpenSourcePRViewController()

@end

@implementation BTOpenSourcePRViewController
{
    UIWebView *_webView;
    UIActivityIndicatorView *_activityIndicatorView;
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    _webView = [[UIWebView alloc] initWithFrame:self.view.frame];
    [_webView setClipsToBounds:YES];
    [self.view addSubview:_webView];
    NSURL *url = [NSURL URLWithString:self.urlString];
    NSURLRequest *urlRequest = [NSURLRequest requestWithURL:url];
    [_webView loadRequest:urlRequest];
    [_webView setDelegate:self];
    _activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    [_activityIndicatorView setCenter:_webView.center];
    [_activityIndicatorView setHidesWhenStopped:YES];
    [_activityIndicatorView startAnimating];
    [self.view addSubview:_activityIndicatorView];
    _webView.backgroundColor = [UIColor clearColor];
    for (UIView *_aView in [_webView subviews])
    {
        if ([_aView isKindOfClass:[UIScrollView class]])
        {
            [(UIScrollView *)_aView setShowsHorizontalScrollIndicator:NO];
        }
    }
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

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)webViewDidFinishLoad:(UIWebView *)webView
{
    [_activityIndicatorView stopAnimating];
}

@end
