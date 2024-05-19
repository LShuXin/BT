//
//  PlayerManager.m
//

#import "PlayerManager.h"

@interface PlayerManager()
-(void)startProximityMonitering;  //开启距离感应器监听(开始播放时)
-(void)stopProximityMonitering;   //关闭距离感应器监听(播放完成时)
@end

@implementation PlayerManager
{
    NSString* _playingFileName;
}
@synthesize decapsulator;
@synthesize avAudioPlayer;

static PlayerManager *mPlayerManager = nil;

+(PlayerManager *)sharedManager
{
    static PlayerManager *g_playerManager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_playerManager = [[PlayerManager alloc] init];
    });
    return g_playerManager;
}

+(id)allocWithZone:(NSZone *)zone
{
    @synchronized(self)
    {
        if (mPlayerManager == nil)
        {
            mPlayerManager = [super allocWithZone:zone];
            return mPlayerManager;
        }
    }
    
    return nil;
}

-(id)init
{
    if (self = [super init])
    {
        
        [[NSNotificationCenter defaultCenter] addObserver:mPlayerManager
                                                 selector:@selector(sensorStateChange:)
                                                     name:@"UIDeviceProximityStateDidChangeNotification"
                                                   object:nil];
        
        AVAudioSession *audioSession = [AVAudioSession sharedInstance];
        NSError *error = nil;

        if (![audioSession setCategory:AVAudioSessionCategoryPlayback error:&error])
        {
            NSLog(@"error occurred while setting audio session category: %@", error.localizedDescription);
        }

        if (![audioSession setActive:YES error:&error])
        {
            NSLog(@"error occurred while activating audio session: %@", error.localizedDescription);
        }

        if (![audioSession overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:&error])
        {
            NSLog(@"error occurred while overriding audio output port: %@", error.localizedDescription);
        }
    }
    return self;
}

-(void)playAudioWithFileName:(NSString *)filename delegate:(id<PlayingDelegate>)newDelegate
{
    if (!filename)
    {
        return;
    }
    if ([filename rangeOfString:@".spx"].location != NSNotFound)
    {
        [[AVAudioSession sharedInstance] setActive:YES error:nil];
        
        [self stopPlaying];
        
        self.delegate = newDelegate;
        
        self.decapsulator = [[Decapsulator alloc] initWithFileName:filename];
        self.decapsulator.delegate = self;
        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
        [self startProximityMonitering];
        _playingFileName = [filename copy];
        [self.decapsulator play];
    }
    else if ([filename rangeOfString:@".mp3"].location != NSNotFound)
    {
        if ( ! [[NSFileManager defaultManager] fileExistsAtPath:filename])
        {
            NSLog(@"要播放的文件不存在:%@", filename);
            _playingFileName = nil;
            [self.delegate playingStoped];
            [newDelegate playingStoped];
            return;
        }
        [self.delegate playingStoped];
        self.delegate = newDelegate;
        
        NSError *error;
        self.avAudioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL URLWithString:filename] error:&error];
        if (self.avAudioPlayer) {
            [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
            [[AVAudioSession sharedInstance] setActive:YES error:nil];
            self.avAudioPlayer.delegate = self;
            _playingFileName = [filename copy];
            [self.avAudioPlayer play];
            [self startProximityMonitering];
        }
        else {
            [self.delegate playingStoped];
        }
    }
    else {
        [self.delegate playingStoped];
    }
}

-(void)playAudioWithFileName:(NSString *)filename playerType:(PlayerType)type delegate:(id<PlayingDelegate>)newDelegate
{
    [[AVAudioSession sharedInstance] setActive:YES error:nil];
    if ([filename rangeOfString:@".spx"].location != NSNotFound)
    {
        [self stopPlaying];
        self.delegate = newDelegate;
        
        self.decapsulator = [[Decapsulator alloc] initWithFileName:filename];
        self.decapsulator.delegate = self;
        [self.decapsulator play];
    }
    else if ([filename rangeOfString:@".mp3"].location != NSNotFound)
    {
        if (![[NSFileManager defaultManager] fileExistsAtPath:filename])
        {
            NSLog(@"要播放的文件不存在:%@", filename);
            _playingFileName = nil;
            [self.delegate playingStoped];
            [newDelegate playingStoped];
            return;
        }
        _playingFileName = nil;
        [self.delegate playingStoped];
        self.delegate = newDelegate;
        
        NSError *error;
        self.avAudioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL URLWithString:filename] error:&error];
        if (self.avAudioPlayer)
        {
            self.avAudioPlayer.delegate = self;
            [self.avAudioPlayer play];
        }
        else
        {
            _playingFileName = nil;
            [self.delegate playingStoped];
        }
    }
    else if ([filename rangeOfString:@".caf"].location != NSNotFound)
    {
        _playingFileName = nil;
        [self.delegate playingStoped];
        self.delegate = newDelegate;
        
        NSArray *pathComponents = [filename componentsSeparatedByString:@"."];
        NSString *soundFilePath = [[NSBundle mainBundle] pathForResource:[pathComponents objectAtIndex:0] ofType:@"caf"];
        if (soundFilePath == nil)
        {
            NSLog(@"audio file does not exists: %@", filename);
            [self.delegate playingStoped];
            [newDelegate playingStoped];
            return;
        }
        
        NSURL *soundUrl = [NSURL fileURLWithPath:soundFilePath];
        NSError *error;
        self.avAudioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:soundUrl error:&error];
        if (error)
        {
            BTLog(@"init AVAudioPlayer failed: %@", error);
            return;
        }


        if (self.avAudioPlayer)
        {
            self.avAudioPlayer.delegate = self;
            [self.avAudioPlayer prepareToPlay];
            [self.avAudioPlayer play];
        }
        else
        {
            _playingFileName = nil;
            [self.delegate playingStoped];
        }
    }
    else
    {
        _playingFileName = nil;
        [self.delegate playingStoped];
    }
    
    switch (type)
    {
        case EarPhone:
            //听筒
            [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
            break;
        case Speaker:
            //扬声器
            [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
            break;
        default:
            break;
    }
}

- (void)stopPlaying {
    _playingFileName = nil;
    [self stopProximityMonitering];

    if (self.decapsulator) {
        [self.decapsulator stopPlaying];
//        self.decapsulator.delegate = nil;   //此行如果放在上一行之前会导致回调问题
        self.decapsulator = nil;
    }
    if (self.avAudioPlayer) {
        [self.avAudioPlayer stop];
        self.avAudioPlayer = nil;
        
//        [self.delegate playingStoped];
    }
    
    [self.delegate playingStoped];
}

- (BOOL)playingFileName:(NSString *)fileName
{
    return [_playingFileName isEqualToString:fileName];
}

- (void)decapsulatingAndPlayingOver {
    _playingFileName = nil;
    [self.delegate playingStoped];
    [self stopProximityMonitering];
}

- (void)sensorStateChange:(NSNotification *)notification {
    //如果此时手机靠近面部放在耳朵旁，那么声音将通过听筒输出，并将屏幕变暗
    if ([[UIDevice currentDevice] proximityState] == YES) {
        NSLog(@"Device is close to user");
        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
    }
    else {
        NSLog(@"Device is not close to user");
        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
    }
}

- (void)startProximityMonitering {
    [[UIDevice currentDevice] setProximityMonitoringEnabled:YES];
    NSLog(@"开启距离监听");
}

- (void)stopProximityMonitering {
//    dispatch_async(dispatch_get_main_queue(), ^{
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
        [[UIDevice currentDevice] setProximityMonitoringEnabled:NO];
        NSLog(@"关闭距离监听");
//    });
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
