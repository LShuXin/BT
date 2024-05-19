//
//  BTChatImageCell.m
//

#import "BTChatImageCell.h"
#import "UIImageView+WebCache.h"
#import "BTChatImagePreviewViewController.h"
#import "UIView+BTAddition.h"
#import "NSDictionary+BTJSON.h"
#import "BTPhotosCache.h"
#import "BTAppDelegate.h"
#import "BTDatabaseUtil.h"
#import "BTMessageSendManager.h"
#import "BTChattingMainViewController.h"
#import "BTSendPhotoMessageAPI.h"
#import "BTSessionModule.h"
#import "UIImage+BTUIImageAddition.h"


@implementation BTChatImageCell
-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self)
    {
        self.msgImgView = [[UIImageView alloc] init];
        self.msgImgView.userInteractionEnabled = NO;
        // A Boolean value that determines whether subviews are confined to the bounds of the view.
        [self.msgImgView setClipsToBounds:YES];
        [self.msgImgView.layer setCornerRadius:3];
        [self.msgImgView setContentMode:UIViewContentModeScaleAspectFill];
        [self.contentView addSubview:self.msgImgView];
        [self.bubbleImageView setClipsToBounds:YES];
        self.photos = [NSMutableArray new];
    }
    return self;
}
-(void)showPreview
{
    if (self.msgImgView.image == nil)
    {
        return;
    }
    [self.photos removeAllObjects];
    [self.photos addObject:[MWPhoto photoWithImage:self.msgImgView.image]];
    
    MWPhotoBrowser *browser = [[MWPhotoBrowser alloc] initWithDelegate:self];
    browser.displayActionButton = NO;
    browser.displayNavArrows = NO;
    browser.wantsFullScreenLayout = YES;
    browser.zoomPhotosToFill = YES;
    [browser setCurrentPhotoIndex:0];
    BTChatImagePreviewViewController *preViewControll = [BTChatImagePreviewViewController new];
    preViewControll.photos = self.photos;
    
    [[BTChattingMainViewController shareInstance].navigationController pushViewController:preViewControll animated:YES];
}

- (void)setContent:(BTMessageEntity*)content
{
    
    [super setContent:content];
    if (content.msgContentType == MSG_TYPE_IMAGE)
    {
        NSDictionary *messageContent = [NSDictionary initWithJsonString:content.msgContent];
        if (!messageContent)
        {
            NSString *urlString = content.msgContent;
            urlString = [urlString stringByReplacingOccurrencesOfString:kBTImageMessagePrefix withString:@""];
            urlString = [urlString stringByReplacingOccurrencesOfString:kBTImageMessageSuffix withString:@""];
            NSURL *url = [NSURL URLWithString:urlString];
            [self showSending];
            [self.msgImgView sd_setImageWithURL:url completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
                [self showSendSuccess];
            }];
            
            return;
        }
        if (messageContent[kBTImageLocalPath])
        {
            //加载本地图片
            NSString *localPath = messageContent[kBTImageLocalPath];
            NSData *data = [[BTPhotosCache sharedPhotoCache] photoCacheForKey:localPath];
            UIImage *image = [[UIImage alloc] initWithData:data];
            [self.msgImgView setImage:image];
        }
        else
        {
            //加载服务器上的图片
            NSString *url = messageContent[kBTImageRemoteUrl];
            __weak BTChatImageCell *weakSelf = self;
            
            [self showSending];
            [self.msgImgView sd_setImageWithURL:[NSURL URLWithString:url] completed:^(UIImage *image, NSError *error, SDImageCacheType cacheType, NSURL *imageURL) {
                [weakSelf showSendSuccess];
                if (error) {
                    
                }
            }];
        }
    }
}

#pragma mark -
#pragma mark BTChatCellProtocol Protocol
-(CGSize)sizeForContent:(BTMessageEntity *)content
{
    float height = 133;
    float width = 80;
    return CGSizeMake(width, height);
}

-(float)contentUpGapWithBubble
{
    return 1;
}

-(float)contentDownGapWithBubble
{
    return 1;
}

-(float)contentLeftGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            return 1;
        case BUBBLE_LEFT:
            return 8.5;
    }
    return 0;
}

-(float)contentRightGapWithBubble
{
    switch (self.location)
    {
        case BUBBLE_RIGHT:
            return 6.5;
            break;
        case BUBBLE_LEFT:
            return 1;
            break;
    }
    return 0;
}

-(void)layoutContentView:(BTMessageEntity *)content
{
    float x = self.bubbleImageView.left + [self contentLeftGapWithBubble];
    float y = self.bubbleImageView.top + [self contentUpGapWithBubble];
    CGSize size = [self sizeForContent:content];
    [self.msgImgView setFrame:CGRectMake(x, y, size.width, size.height)];
}

-(float)cellHeightForMessage:(BTMessageEntity *)message
{
    return 27 + 2 * bt_bubbleUpDown;
}

-(void)dealloc
{
    self.photos = nil;
}

#pragma mark -
#pragma mark BTMenuImageViewDelegate
- (void)clickTheSendAgain:(BTMenuImageView *)imageView
{
    if (self.sendAgain)
    {
        self.sendAgain();
    }
}

-(void)sendImageAgain:(BTMessageEntity *)message
{
    [self showSending];
    NSDictionary *dic = [NSDictionary initWithJsonString:message.msgContent];
    NSString *locaPath = dic[kBTImageLocalPath];
    __block UIImage *image = [[SDImageCache sharedImageCache] imageFromDiskCacheForKey:locaPath];
    if (!image)
    {
        NSData *data = [[BTPhotosCache sharedPhotoCache] photoCacheForKey:locaPath];
        image = [[UIImage alloc] initWithData:data];
        if (!image)
        {
            [self showSendFailure];
            return ;
        }
    }
    
    [[BTSendPhotoMessageAPI sharedPhotoCache] uploadImage:locaPath success:^(NSString *imageURL) {
        NSDictionary *tempMessageContent = [NSDictionary initWithJsonString:message.msgContent];
        NSMutableDictionary *mutalMessageContent = [[NSMutableDictionary alloc] initWithDictionary:tempMessageContent];
        [mutalMessageContent setValue:imageURL forKey:kBTImageRemoteUrl];
        NSString *messageContent = [mutalMessageContent jsonString];
        message.msgContent = messageContent;
        image = nil;
        [[BTMessageSendManager instance] sendMessage:message
                                             isGroup:[message isGroupMessage]
                                             session:[[BTSessionModule sharedInstance] getSessionById:message.sessionId]
                                          completion:^(BTMessageEntity *theMessage,NSError *error) {
            if (error)
            {
                BTLog(@"发送消息失败");
                message.state = MSG_SEND_FAILURE;
                // update db
                [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
                    if (result)
                    {
                        [self showSendFailure];
                    }
                }];
            }
            else
            {
                message.state = MSG_SEND_SUCCESS;
                // update db
                [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
                    if (result)
                    {
                        [self showSendSuccess];
                    }
                }];
            }
        } error:^(NSError *error) {
            [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
                if (result)
                {
                    [self showSendFailure];
                }
            }];
        }];
        
    } failure:^(id error) {
        message.state = MSG_SEND_FAILURE;
        [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
            if (result)
            {
                [self showSendFailure];
            }
        }];
    }];
    
}
-(void)clickThePreview:(BTMenuImageView *)imageView
{
    if (self.preview)
    {
        self.preview();
    }
}
@end
