//
//  BTRecordingView.m
//

#import "BTRecordingView.h"
#import "UIView+BTAddition.h"
#import "BTPublicDefine.h"


#define RECORDING_VIEW_WIDTH                          196
#define RECORDING_VIEW_HEIGHT                         196
#define VOLUMN_VIEW_TAG                               10


@interface BTRecordingView(PrivateAPI)
-(void)setupCancelSendView;
-(void)setupShowVolumnState;
-(void)setupShowRecordingTooShort;
-(void)showCancelSendState;
-(void)showVolumnState;
-(void)showRecordingTooShort;
-(float)heightForVolumn:(float)vomlun;
@end

@implementation BTRecordingView

-(instancetype)initWithState:(BTRecordingState)state
{
    self = [super init];
    if (self)
    {
        self.frame = CGRectMake(0, 0, RECORDING_VIEW_WIDTH, RECORDING_VIEW_HEIGHT);
        [self setClipsToBounds:YES];
        [self.layer setCornerRadius:10];
        [self setBackgroundColor:[UIColor clearColor]];
        UIView *backgroundView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, RECORDING_VIEW_WIDTH, RECORDING_VIEW_HEIGHT)];
        [backgroundView setBackgroundColor:[UIColor blackColor]];
        [backgroundView setAlpha:0.7];
        backgroundView.tag = 100;
        [self addSubview:backgroundView];
        _recordingState = SHOW_VOLUMN;
        [self setupShowVolumnState];
    }
    return self;
}

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        // Initialization code
    }
    return self;
}

-(void)setRecordingState:(BTRecordingState)recordingState
{
    switch (recordingState)
    {
        case SHOW_CANCEL_SEND:
            [self showCancelSendState];
            break;
        case SHOW_VOLUMN:
            [self showVolumnState];
            break;
        case SHOW_RECORD_TIME_TOO_SHORT:
            [self showRecordingTooShort];
            break;
    }
}

-(void)setVolume:(float)volume
{
    if (_recordingState != SHOW_VOLUMN)
    {
        return;
    }
    
    UIImageView *volumnImageView = [self subviewWithTag:VOLUMN_VIEW_TAG];
    float height = [self heightForVolumn:volume];
    [volumnImageView setHeight:height];
    volumnImageView.bottom = 126;
}

#pragma mark privateAPI
-(void)setupCancelSendView
{
    [self.subviews enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([(UIView *)obj tag] != 100)
        {
            [(UIView *)obj removeFromSuperview];
        }
    }];
    UIImage *image = [UIImage imageNamed:@"dd_cancel_send_record"];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
    [imageView setFrame:CGRectMake(74, 53, 45, 59)];
    [self addSubview:imageView];
    
    UIView* backgrounView = [[UIView alloc] initWithFrame:CGRectMake(28, 152, 140, 23)];
    [backgrounView setBackgroundColor:RGB(176, 34, 33)];
    [backgrounView setAlpha:0.8];
    [backgrounView.layer setCornerRadius:2];
    [backgrounView setClipsToBounds:YES];
    [self addSubview:backgrounView];
    
    UILabel *prompt = [[UILabel alloc] initWithFrame:CGRectMake(28, 152, 140, 23)];
    [prompt setBackgroundColor:[UIColor clearColor]];
    [prompt setTextColor:[UIColor whiteColor]];
    [prompt setText:@"松开手指，取消发送"];
    [prompt setFont:[UIFont systemFontOfSize:15]];
    [prompt setTextAlignment:NSTextAlignmentCenter];
    [self addSubview:prompt];
}

-(void)setupShowVolumnState
{
    [self.subviews enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([(UIView *)obj tag] != 100)
        {
            [(UIView *)obj removeFromSuperview];
        }
    }];
    UIImage *image = [UIImage imageNamed:@"dd_recording"];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
    [imageView setFrame:CGRectMake(60, 42, 53, 83)];
    [self addSubview:imageView];
    
    UILabel *prompt = [[UILabel alloc] initWithFrame:CGRectMake(0, 152, RECORDING_VIEW_WIDTH, 23)];
    [prompt setBackgroundColor:[UIColor clearColor]];
    [prompt setTextColor:[UIColor whiteColor]];
    [prompt.layer setCornerRadius:2];
    [prompt setTextAlignment:NSTextAlignmentCenter];
    [prompt setFont:[UIFont systemFontOfSize:15]];
    [prompt setText:@"手指上滑,取消发送"];
    [self addSubview:prompt];

    UIImage *volumnImage = [UIImage imageNamed:@"dd_volumn"];
    UIImageView *volumnImageView = [[UIImageView alloc] initWithImage:volumnImage];
    [volumnImageView setFrame:CGRectMake(119, 83, 20, 43)];
    [volumnImageView setContentMode:UIViewContentModeBottom];
    [volumnImageView setClipsToBounds:YES];
    [volumnImageView setTag:VOLUMN_VIEW_TAG];
    [self addSubview:volumnImageView];
}

-(void)setupShowRecordingTooShort
{
    [self.subviews enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if ([(UIView *)obj tag] != 100)
        {
            [(UIView *)obj removeFromSuperview];
        }
    }];
    UIImage *image = [UIImage imageNamed:@"dd_record_too_short"];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
    [imageView setFrame:CGRectMake(85, 42, 22, 83)];
    [self addSubview:imageView];
    
    UILabel *prompt = [[UILabel alloc] initWithFrame:CGRectMake(0, 152, RECORDING_VIEW_WIDTH, 23)];
    [prompt setBackgroundColor:[UIColor clearColor]];
    [prompt setTextColor:[UIColor whiteColor]];
    [prompt.layer setCornerRadius:2];
    [prompt setTextAlignment:NSTextAlignmentCenter];
    [prompt setFont:[UIFont systemFontOfSize:15]];
    [prompt setText:@"说话时间太短"];
    [self addSubview:prompt];
}

-(void)showCancelSendState
{
    if (self.recordingState != SHOW_CANCEL_SEND)
    {
        [self setupCancelSendView];
    }
    _recordingState = SHOW_CANCEL_SEND;
}

-(void)showVolumnState
{
    if (self.recordingState != SHOW_VOLUMN)
    {
        [self setupShowVolumnState];
    }
    _recordingState = SHOW_VOLUMN;
}

-(void)showRecordingTooShort
{
    if (self.recordingState != SHOW_RECORD_TIME_TOO_SHORT)
    {
        [self setupShowRecordingTooShort];
    }
    _recordingState = SHOW_RECORD_TIME_TOO_SHORT;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self.recordingState == SHOW_RECORD_TIME_TOO_SHORT)
        {
            [self setHidden:YES];
        }
    });
}

-(float)heightForVolumn:(float)vomlun
{
    // 0-1.6 volumn
    float height = 43.0 / 1.6 * vomlun;
    return height;
}
@end
