//
//  BTMenuImageView.h
//

#import <UIKit/UIKit.h>


@class BTMenuImageView;

typedef NS_ENUM(NSUInteger, BTImageShowMenu)
{
    SHOW_EARPHONE_PLAY                      = 1,        //听筒播放
    SHOW_SPEAKER_PLAY                       = 1 << 1,   //扬声器播放
    SHOW_SEND_AGAIN                         = 1 << 2,   //重发
    SHOW_COPY                               = 1 << 3,   //复制
    SHOW_PREVIEW                            = 1 << 4    //图片预览
};


@protocol BTMenuImageViewDelegate<NSObject>
-(void)clickTheCopy:(BTMenuImageView *)imageView;
-(void)clickTheEarphonePlay:(BTMenuImageView *)imageView;
-(void)clickTheSpeakerPlay:(BTMenuImageView *)imageView;
-(void)clickTheSendAgain:(BTMenuImageView *)imageView;
-(void)clickThePreview:(BTMenuImageView *)imageView;
-(void)tapTheImageView:(BTMenuImageView *)imageView;
@end


@interface BTMenuImageView : UIImageView
@property(nonatomic, assign)id<BTMenuImageViewDelegate> delegate;
@property(nonatomic, assign)BTImageShowMenu showMenu;
@end
