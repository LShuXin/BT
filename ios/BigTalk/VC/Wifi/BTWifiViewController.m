//
//  BTWifiViewController.m
//

#import "BTWifiViewController.h"


@interface BTWifiViewController()

@end

@implementation BTWifiViewController
{
    UIWebView *_webView;
    UIActivityIndicatorView *_activityIndicatorView;
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"";
    _webView = [[UIWebView alloc] initWithFrame:self.view.bounds];
    [self.view addSubview:_webView];
    NSURL *url = [NSURL URLWithString:@"https://ant.design/docs/spec/colors-cn"];
    NSURLRequest *urlRequest = [NSURLRequest requestWithURL:url];
    [_webView loadRequest:urlRequest];
    [_webView setDelegate:self];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)webViewDidFinishLoad:(UIWebView*)webView
{
    [_activityIndicatorView stopAnimating];
}


@end
