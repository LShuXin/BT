//
//  BTScanQRCodeController.h
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>


@interface BTScanQRCodeController : UIViewController<AVCaptureMetadataOutputObjectsDelegate>
{
    int num;
    BOOL upOrdown;
    NSTimer *timer;
}

@property(strong, nonatomic)AVCaptureDevice *device;
@property(strong, nonatomic)AVCaptureDeviceInput *input;
@property(strong, nonatomic)AVCaptureMetadataOutput *output;
@property(strong, nonatomic)AVCaptureSession *session;
@property(strong, nonatomic)AVCaptureVideoPreviewLayer *preview;
@property(nonatomic, retain)UIImageView *line;
@property(weak)IBOutlet UIView *scanView;

-(IBAction)scanAgain:(id)sender;

@end

