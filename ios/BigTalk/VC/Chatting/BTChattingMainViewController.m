//
//  BTChattingMainViewController.m
//  bt_ios
//
//  Created by LShuXin on 24-4-13.
//

#import "BTChattingMainViewController.h"
#import "BTChatUtilityViewController.h"
#import "BTPublicDefine.h"
#import "BTPhotosCache.h"
#import "BTGroupModule.h"
#import "BTMessageSendManager.h"
#import "BTMsgReadACKAPI.h"
#import "BTDatabaseUtil.h"
#import "BTChatTextCell.h"
#import "BTChatVoiceCell.h"
#import "BTChatImageCell.h"
#import "BTChattingEditViewController.h"
#import "BTPromptCell.h"
#import "UIView+BTAddition.h"
#import "BTMessageModule.h"
#import "BTRecordingView.h"
#import "BTAnalysisImage.h"
#import "BTTouchDownGestureRecognizer.h"
#import "BTSendPhotoMessageAPI.h"
#import "NSDictionary+BTJSON.h"
#import "BTEmotionsModule.h"
#import "BTRuntimeStatus.h"
#import "BTEmotionCell.h"
#import "BTRecentUsersViewController.h"
#import "BTPublicProfileViewController.h"
#import "BTUnAckMessageManager.h"
#import "BTGetMessageQueueAPI.h"
#import "BTGetLatestMsgId.h"
#import "BTNotificationHelper.h"
#import "BTConsts.h"


typedef NS_ENUM(NSUInteger, BottomShowComponent)
{
    SHOW_INPUT_VIEW                    = 1,        // 1
    SHOW_KEYBOARD                      = 1 << 1,   // 2
    SHOW_EMOTION                       = 1 << 2,   // 4
    SHOW_UTILITY                       = 1 << 3    // 8
};

typedef NS_ENUM(NSUInteger, BottomHideComponent)
{
    HIDE_INPUT_VIEW                    = 14,
    HIDE_KEYBOARD                      = 13,
    HIDE_EMOTION                       = 11,
    HIDE_UTILITY                       = 7
};

typedef NS_ENUM(NSUInteger, InputButtonType)
{
    INPUT_BUTTON_TYPE_VOICE,
    INPUT_BUTTON_TYPE_TEXT
};

typedef NS_ENUM(NSUInteger, PanelStatus)
{
    PANEL_STATUS_VOICE_INPUT,
    PANEL_STATUS_TEXT_INPUT,
    PANEL_STATUS_EMOTION_INPUT,
    PANEL_STATUS_IMAGE_INPUT
};

#define BTInputMinHeight            60.0f
#define BTInputHeight               self.chatInputView.size.height
#define BTInputBottomFrame          CGRectMake(0, BTContentHeight + BTNavBarHeight - self.chatInputView.height, BTFullWidth, self.chatInputView.height)
#define BTInputTopFrame             CGRectMake(0, BTContentHeight + BTNavBarHeight + self.chatInputView.height - 300, BTFullWidth, self.chatInputView.height)
#define BTUtilityFrame              CGRectMake(0, BTContentHeight + BTNavBarHeight - 216, BTFullWidth, 216)
#define BTEmotionFrame              CGRectMake(0, BTContentHeight + BTNavBarHeight - 216, BTFullWidth, 216)
#define BTComponentBottom           CGRectMake(0, BTContentHeight + BTNavBarHeight, BTFullWidth, 216)


@interface BTChattingMainViewController()<UIGestureRecognizerDelegate>

@property(nonatomic, assign)CGPoint inputViewCenter;
@property(nonatomic, strong)UIActivityIndicatorView *activity;
@property(assign)PanelStatus panelStatus;
@property(strong)NSString *chatObjectId;
@property(strong)UIButton *titleBtn;

-(void)recentViewController;

-(UITableViewCell *)p_textCell_tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath message:(BTMessageEntity *)message;
-(UITableViewCell *)p_voiceCell_tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath message:(BTMessageEntity *)message;
-(UITableViewCell *)p_promptCell_tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath message:(BTPromptEntity *)message;

-(void)n_receiveMessage:(NSNotification *)notification;
-(void)p_clickTheRecordButton:(UIButton *)button;
-(void)p_record:(UIButton *)button;
-(void)p_willCancelRecord:(UIButton *)button;
-(void)p_cancelRecord:(UIButton *)button;
-(void)p_endCancelRecord:(UIButton *)button;
-(void)p_sendRecord:(UIButton *)button;

-(void)p_tapOnTableView:(UIGestureRecognizer *)sender;
-(void)p_hideBottomComponent;

-(void)p_enableChatFunction;
-(void)p_unableChatFunction;

@end


@implementation BTChattingMainViewController
{
    BTTouchDownGestureRecognizer *_touchDownGestureRecognizer;
    NSString *_currentInputContent;
    UIButton *_recordButton;
    BottomShowComponent _bottomShowComponent;
    float _inputViewY;
    int _type;
}

+(instancetype)shareInstance
{
    static dispatch_once_t onceToken;
    static BTChattingMainViewController *_sharedManager = nil;
    dispatch_once(&onceToken, ^{
        _sharedManager = [BTChattingMainViewController new];
    });
    return _sharedManager;
}

-(instancetype)init
{
    self = [super init];
    if (self)
    {
        [self initNavTitle];
    }
    return self;
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    [self initView];
    [self subscribeNotificationCenter];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.chatInputView.textView setText:nil];
    [self.tabBarController.tabBar setHidden:YES];
    [self.navigationController.navigationBar setBarStyle:UIBarStyleDefault];
    [self p_hideBottomComponent];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.tabBarController.tabBar setHidden:NO];
    [self.module.ids removeAllObjects];
    [[PlayerManager sharedManager] stopPlaying];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:BTNotificationUserReloginSuccess
                                                  object:nil];
    [self.chatInputView resignFirstResponder];
}

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

-(void)initView
{
    self.extendedLayoutIncludesOpaqueBars = YES;
    
    _tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    _tableView.translatesAutoresizingMaskIntoConstraints = NO;
    _tableView.indicatorStyle = UIScrollViewIndicatorStyleDefault;
    _tableView.scrollEnabled = YES;
    _tableView.contentMode = UIViewContentModeScaleToFill;
    [self.view addSubview:_tableView];
    [[_tableView.topAnchor constraintEqualToAnchor:self.view.topAnchor] setActive:YES];
    [[_tableView.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor] setActive:YES];
    [[_tableView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor] setActive:YES];
    [[_tableView.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor] setActive:YES];
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(p_tapOnTableView:)];
    [self.tableView addGestureRecognizer:tap];
    UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(p_tapOnTableView:)];
    pan.delegate = self;
    [self.tableView addGestureRecognizer:pan];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    [self scrollToBottomAnimated:NO];
    
    [self initialInput];
    
    self.activity = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    self.activity.frame = CGRectMake(BTFullWidth / 2 - 10, 70, 20, 20);
    [self.view addSubview:self.activity];
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"myprofile"]
                                                             style:UIBarButtonItemStylePlain
                                                            target:self
                                                            action:@selector(edit)];
    self.navigationItem.rightBarButtonItem = item;
    
    [self.navigationController.navigationBar setBackgroundColor: [UIColor whiteColor]];
    
    [self.module addObserver:self
                  forKeyPath:@"showingMessages"
                     options:NSKeyValueObservingOptionOld|NSKeyValueObservingOptionNew
                     context:NULL];
    [self.module addObserver:self
                  forKeyPath:@"sessionEntity.sessionId"
                     options:NSKeyValueObservingOptionNew|NSKeyValueObservingOptionOld
                     context:NULL];
    
    [self.navigationItem.titleView setUserInteractionEnabled:YES];
    self.navigationItem.titleView = _titleBtn;
    self.view.backgroundColor = [UIColor whiteColor];
    self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    UIButton *backButton = [UIButton buttonWithType:101];
    [backButton addTarget:self action:@selector(p_popViewController) forControlEvents:UIControlEventTouchUpInside];
    [backButton setImage:[UIImage imageNamed:@"top_back"] forState:UIControlStateNormal];
    UIBarButtonItem *backItem = [[UIBarButtonItem alloc] initWithCustomView:backButton];
    self.navigationItem.backBarButtonItem = backItem;
}

-(void)initialInput
{
    CGRect inputFrame = CGRectMake(0,
                                   BTContentHeight + BTNavBarAndStatusBarHeight - BTInputMinHeight,
                                   BTFullWidth,
                                   BTInputMinHeight);
    _chatInputView = [[JSMessageInputView alloc] initWithFrame:inputFrame delegate:self];
    [_chatInputView setBackgroundColor:RGB(249, 249, 249)];
    [self.view addSubview:_chatInputView];
    [_chatInputView.emotionbutton addTarget:self
                                     action:@selector(showEmotions)
                           forControlEvents:UIControlEventTouchUpInside];
    
    [_chatInputView.showUtilitysbutton addTarget:self
                           action:@selector(showUtilitys)
                 forControlEvents:UIControlEventTouchDown];
    
    [_chatInputView.voiceButton addTarget:self
                                   action:@selector(p_clickTheRecordButton:)
                         forControlEvents:UIControlEventTouchUpInside];

    _touchDownGestureRecognizer = [[BTTouchDownGestureRecognizer alloc] initWithTarget:self action:nil];
    __weak BTChattingMainViewController *weakSelf = self;
    _touchDownGestureRecognizer.touchDown = ^{
        [weakSelf p_record:nil];
    };
    _touchDownGestureRecognizer.moveInside = ^{
        [weakSelf p_endCancelRecord:nil];
    };
    _touchDownGestureRecognizer.moveOutside = ^{
        [weakSelf p_willCancelRecord:nil];
    };
    _touchDownGestureRecognizer.touchEnd = ^(BOOL inside) {
        if (inside)
        {
            [weakSelf p_sendRecord:nil];
        }
        else
        {
            [weakSelf p_cancelRecord:nil];
        }
    };
    [self.chatInputView.recordButton addGestureRecognizer:_touchDownGestureRecognizer];
    
    _recordingView = [[BTRecordingView alloc] initWithState:SHOW_VOLUMN];
    [_recordingView setHidden:YES];
    [_recordingView setCenter:CGPointMake(BTFullWidth / 2, self.view.centerY)];
    [self addObserver:self forKeyPath:@"_inputViewY" options:NSKeyValueObservingOptionNew|NSKeyValueObservingOptionOld context:nil];
}

-(void)initNavTitle
{
    _titleBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    _titleBtn.frame = CGRectMake(0, 0, 150, 40);
    [_titleBtn setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_titleBtn setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
    [_titleBtn addTarget:self action:@selector(titleTap) forControlEvents:UIControlEventTouchUpInside];
    [_titleBtn.titleLabel setTextAlignment:NSTextAlignmentLeft];
}

-(void)checkSessionLastMsgIdThenUpdate:(NSNotification *)notification
{
//    //用于在后台系统被系统杀死后恢复消息的
//    NSDictionary *sessionDic = [notification object];
//    SessionEntity *session = sessionDic[@"session"];
//    [self.module loadHisToryMessageFromServer:session.lastMsgID loadCount:session.unReadMsgCount Completion:^(NSUInteger addcount, NSError *error) {
//        self.module.sessionEntity.unReadMsgCount = 0;
//        [self.tableView reloadData];
//    }];
}

-(BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer
{
    CGPoint location = [gestureRecognizer locationInView:self.view];
    if (CGRectContainsPoint(BTInputBottomFrame, location))
    {
        return NO;
    }
    return YES;
}

-(void)sendImageMessage:(BTPhoto *)photo Image:(UIImage *)image
{
    BTLog(@"prepare to send image message");
    NSDictionary *messageContentDic = @{kBTImageLocalPath: photo.localPath};
    NSString *messageContent = [messageContentDic jsonString];

    BTMessageEntity *message = [BTMessageEntity makeMessage:messageContent withChatModule:self.module msgType:MSG_TYPE_IMAGE];
    [self scrollToBottomAnimated:YES];
    NSData *photoData = UIImagePNGRepresentation(image);
    [[BTPhotosCache sharedPhotoCache] storePhoto:photoData forKey:photo.localPath toDisk:YES];
    [[BTDatabaseUtil instance] insertMessages:@[message] success:^{
        BTLog(@"insert image message into db success");
    } failure:^(NSString *error) {
        BTLog(@"insert image message into db failed: %@", error);
    }];
    photo = nil;
    [[BTSendPhotoMessageAPI sharedPhotoCache] uploadImage:messageContentDic[kBTImageLocalPath] success:^(NSString *imageURL) {
        [self scrollToBottomAnimated:YES];
        message.state = MSG_SENDING;
        NSDictionary *tempMessageContent = [NSDictionary initWithJsonString:message.msgContent];
        NSMutableDictionary *mutalMessageContent = [[NSMutableDictionary alloc] initWithDictionary:tempMessageContent];
        [mutalMessageContent setValue:imageURL forKey:kBTImageRemoteUrl];
        NSString *messageContent = [mutalMessageContent jsonString];
        message.msgContent = messageContent;
        [self sendMessage:imageURL messageEntity:message completion: ^(BTMessageEntity *message, NSError *error) {
            
        }];
        [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
            BTLog(@"update image message in db success");
            // TODO: update database、ui
        }];
    } failure:^(id error) {
        BTLog(@"send image message failed");
        message.state = MSG_SEND_FAILURE;
        [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
            if (result)
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    BTLog(@"update image message in db success");
                    // TODO: update database、ui
                    // [_tableView reloadData];
                    // NSUInteger index = [self.module.showingMessages indexOfObject:message];
                    // [_tableView beginUpdates];
                    // [_tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:index inSection:0]] withRowAnimation:UITableViewRowAnimationNone];
                    // [_tableView reloadData];
                });
            }
        }];
    }];
}

-(void)sendTextMessage
{
    // send text message
    NSString *text = [self.chatInputView.textView text];
    
    NSString *parten = @"\\s";
    NSRegularExpression *reg = [NSRegularExpression regularExpressionWithPattern:parten options:NSRegularExpressionCaseInsensitive error:nil];
    NSString *checkoutText = [reg stringByReplacingMatchesInString:text
                                                           options:NSMatchingReportProgress
                                                             range:NSMakeRange(0, [text length]) withTemplate:@""];
    if ([checkoutText length] == 0)
    {
        return;
    }

    BTMessageEntity *message = [BTMessageEntity makeMessage:text withChatModule:self.module msgType:MSG_TYPE_TEXT];
    [self.chatInputView.textView setText:nil];
    [[BTDatabaseUtil instance] insertMessages:@[message] success:^{
        BTLog(@"insert text message into db success");
    } failure:^(NSString *error) {
        BTLog(@"insert text message into db failed: %@", error);
    }];
    [self sendMessage:text messageEntity:message completion: ^(BTMessageEntity *message, NSError *error) {
        [self.tableView reloadData];
    }];
}

-(void)sendMessage:(NSString *)msg messageEntity:(BTMessageEntity *)message completion: (BTSendMessageCompletion)completion
{
    BOOL isGroup = [self.module.sessionEntity isGroup];
    [[BTMessageSendManager instance] sendMessage:message
                                         isGroup:isGroup
                                         session:self.module.sessionEntity
                                      completion:^(BTMessageEntity *theMessage, NSError *error) {

        dispatch_async(dispatch_get_main_queue(), ^{
            BTLog(@"send messsage success");
            [self scrollToBottomAnimated:YES];
            completion(theMessage, nil);
        });
    } error:^(NSError *error) {
        BTLog(@"send message failed: %@", error);
        // TODO: update database、ui
        message.state = MSG_SEND_FAILURE;
        NSUInteger index = [self.module.showingMessages indexOfObject:message];
        [self.tableView beginUpdates];
        [self.tableView reloadRowsAtIndexPaths:@[[NSIndexPath indexPathForRow:index inSection:0]] withRowAnimation:UITableViewRowAnimationNone];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.tableView reloadData];
        });
    }];
}

-(void)sendVoiceMessage:(NSString *)filePath time:(NSTimeInterval)interval
{
    BTLog(@"prepare to send voice messsage");
    NSMutableData *muData = [[NSMutableData alloc] init];
    NSData *data = [NSData dataWithContentsOfFile:filePath];
    int length = [RecorderManager sharedManager].recordedTimeInterval;
    if (length < 1)
    {
        BTLog(@"invalid record as the record duration is too short");
        dispatch_async(dispatch_get_main_queue(), ^{
            [_recordingView setHidden:NO];
            [_recordingView setRecordingState:SHOW_RECORD_TIME_TOO_SHORT];
        });
        return;
    }
    else
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [_recordingView setHidden:YES];
        });
    }
    // add the binary data of the recording duration to the begining of the recording data.
    int8_t ch[4];
    for (int32_t i = 0; i < 4; i++)
    {
        ch[i] = ((length >> ((3 - i)*8)) & 0x0ff);
    }
    [muData appendBytes:ch length:4];
    [muData appendData:data];
    BTMessageContentType msgContentType = MSG_TYPE_VOICE;
    BTMessageEntity *message = [BTMessageEntity makeMessage:filePath withChatModule:self.module msgType:msgContentType];
    BOOL isGroup = [self.module.sessionEntity isGroup];
    if (isGroup)
    {
        message.msgType = MsgType_MsgTypeGroupAudio;
    }
    else
    {
        message.msgType = MsgType_MsgTypeSingleAudio;
    }
    [message.info setObject:@(length) forKey:kBTVoiceLength];
    [message.info setObject:@(1) forKey:kBTVoicePlayed];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self scrollToBottomAnimated:YES];
        [[BTDatabaseUtil instance] insertMessages:@[message] success:^{
            NSLog(@"recording message insert into db success");
        } failure:^(NSString *error) {
            NSLog(@"recording message insert into db failed: %@", error);
        }];
    });
    // TODO: separate upload and send logic.
    BTLog(@"voice message sending...");
    [[BTMessageSendManager instance] sendVoiceMessage:muData
                                             filePath:filePath
                                            sessionId:self.module.sessionEntity.sessionId
                                              isGroup:isGroup
                                              message:message
                                              session:self.module.sessionEntity
                                           completion:^(BTMessageEntity *theMessage, NSError *error) {
        
        if (!error)
        {
            BTLog(@"send voice message success: %@", message);
            [[PlayerManager sharedManager] playAudioWithFileName:@"msg.caf" playerType:Speaker delegate:self];
            message.state = MSG_SEND_SUCCESS;
            [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [_tableView reloadData];
                });
            }];
        }
        else
        {
            BTLog(@"send voice message failed:%@", error);
            message.state = MSG_SEND_FAILURE;
            [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [_tableView reloadData];
                });
            }];
        }
    }];
}

//--------------------------------------------------------------------------------------------
#pragma mark -
#pragma mark RecordingDelegate
-(void)recordingFinishedWithFileName:(NSString *)filePath time:(NSTimeInterval)interval
{
    BTLog(@"record finished with fileName: %@ time: %f", filePath, (double)interval);
    [self sendVoiceMessage: filePath time:interval];
}

-(void)playingStoped
{
    
}

-(void)subscribeNotificationCenter
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(n_receiveMessage:)
                                                 name:BTNotificationReceiveMessage
                                               object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(handleWillShowKeyboard:)
												 name:UIKeyboardWillShowNotification
                                               object:nil];
    
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(handleWillHideKeyboard:)
												 name:UIKeyboardWillHideNotification
                                               object:nil];
    
//     [[NSNotificationCenter defaultCenter] addObserver:self
//                                              selector:@selector(checkSessionLastMsgIThenUpdate:)
//                                                  name:@"ChattingSessionUpdate"
//                                                object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(reloginSuccess)
                                                 name:BTNotificationUserReloginSuccess
                                               object:nil];
}

-(void)setThisViewTitle:(NSString *)title
{
     [self.titleBtn setTitle:title forState:UIControlStateNormal];
}

-(void)edit
{
    BTChattingEditViewController *chattingedit = [BTChattingEditViewController new];
    chattingedit.session = self.module.sessionEntity;
    self.title = @"";
    [self.navigationController pushViewController:chattingedit animated:YES];
}

-(void)scrollToBottomAnimated:(BOOL)animated
{
    NSInteger rows = [self.tableView numberOfRowsInSection:0];
    if (rows > 0)
    {
        [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:rows - 1 inSection:0]
                              atScrollPosition:UITableViewScrollPositionBottom
                                      animated:animated];
    }
}

-(BTChattingModule *)module
{
    if (!_module)
    {
        _module = [[BTChattingModule alloc] init];
    }
    return _module;
}

#pragma mark -
#pragma mark ActionMethods  发送sendAction 音频 voiceChange  显示表情 disFaceKeyboard
-(void)sendAction
{
    if (self.chatInputView.textView.text.length > 0)
    {
        BTLog(@"点击发送");
        [self.chatInputView.textView setText:@""];
    }
}

#pragma mark -
#pragma mark UIGesture Delegate
-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    if ([gestureRecognizer.view isEqual:_tableView])
    {
        return YES;
    }
    return NO;
}

#pragma mark - EmojiFace Funcation
-(void)insertEmojiFace:(NSString *)string
{
    BTMessageEntity *message = [BTMessageEntity makeMessage:string withChatModule:self.module msgType:MSG_TYPE_EMOTION];
    [self.chatInputView.textView setText:nil];
    [[BTDatabaseUtil instance] insertMessages:@[message] success:^{
        BTLog(@"emoji message insert into db success");
    } failure:^(NSString *error) {
        BTLog(@"emoji message insert into db failed: %@", error);
    }];
    [self sendMessage:string messageEntity:message completion: ^(BTMessageEntity *message, NSError *error) {
        [self.tableView reloadData];
    }];
    // NSMutableString *content = [NSMutableString stringWithString:self.chatInputView.textView.text];
    // [content appendString:string];
    // [self.chatInputView.textView setText:content];
}

-(void)deleteEmojiFace
{
    BTEmotionsModule *emotionModule = [BTEmotionsModule shareInstance];
    NSString *toDeleteString = nil;
    if (self.chatInputView.textView.text.length == 0)
    {
        return;
    }
    if (self.chatInputView.textView.text.length == 1)
    {
        self.chatInputView.textView.text = @"";
    }
    else
    {
        toDeleteString = [self.chatInputView.textView.text substringFromIndex:self.chatInputView.textView.text.length - 1];
        int length = [emotionModule.emotionLength[toDeleteString] intValue];
        if (length == 0)
        {
            toDeleteString = [self.chatInputView.textView.text substringFromIndex:self.chatInputView.textView.text.length - 2];
            length = [emotionModule.emotionLength[toDeleteString] intValue];
        }
        length = length == 0 ? 1 : length;
        self.chatInputView.textView.text = [self.chatInputView.textView.text substringToIndex:self.chatInputView.textView.text.length - length];
    }
}


#pragma mark - UITableView DataSource
-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.module.showingMessages count];
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    float height = 0;
    id object = self.module.showingMessages[indexPath.row];
    if ([object isKindOfClass:[BTMessageEntity class]])
    {
        BTMessageEntity *message = object;
        height = [self.module messageHeight:message];
    }
    else if ([object isKindOfClass:[BTPromptEntity class]])
    {
        height = 30;
    }
    return height + 10;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    id object = self.module.showingMessages[indexPath.row];
    UITableViewCell *cell = nil;
    if ([object isKindOfClass:[BTMessageEntity class]])
    {
        BTMessageEntity *message = (BTMessageEntity *)object;
        if (message.msgContentType == MSG_TYPE_TEXT)
        {
            cell = [self p_textCell_tableView:tableView cellForRowAtIndexPath:indexPath message:message];
        }
        else if (message.msgContentType == MSG_TYPE_VOICE)
        {
            cell = [self p_voiceCell_tableView:tableView cellForRowAtIndexPath:indexPath message:message];
        }
        else if(message.msgContentType == MSG_TYPE_IMAGE)
        {
            cell = [self p_imageCell_tableView:tableView cellForRowAtIndexPath:indexPath message:message];
        }
        else if (message.msgContentType == MSG_TYPE_EMOTION)
        {
            cell = [self p_emotionCell_tableView:tableView cellForRowAtIndexPath:indexPath message:message];
        }
        else
        {
            cell = [self p_textCell_tableView:tableView cellForRowAtIndexPath:indexPath message:message];
        }
    }
    else if ([object isKindOfClass:[BTPromptEntity class]])
    {
        BTPromptEntity *prompt = (BTPromptEntity *)object;
        cell = [self p_promptCell_tableView:tableView cellForRowAtIndexPath:indexPath message:prompt];
    }
    
    return cell;
}

-(void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    static BOOL loadingHistory = NO;
 
     if (scrollView.contentOffset.y < -100 && [self.module.showingMessages count] > 0 && !loadingHistory)
     {
         loadingHistory = YES;
         self.hadLoadHistory = YES;
         [self.activity startAnimating];
         NSInteger preCount = [self.module.showingMessages count];
         dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
             [self.module loadMoreHistoryCompletion:^(NSUInteger addCount, NSError *error) {
                [self.activity stopAnimating];
                loadingHistory = NO;
                [self.tableView reloadData];
                if (addCount)
                {
                     NSInteger index = [self.module.showingMessages count] - preCount;
                     [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:index inSection:0]
                                           atScrollPosition:UITableViewScrollPositionTop animated:NO];
                 }
             }];
         });
     }
}


#pragma mark PublicAPI
-(void)showChattingContentForSession:(BTSessionEntity *)session
{
    BTLog(@"prepare to load history messages for session(%@)", session.sessionId);
    self.module.sessionEntity = nil;
    self.hadLoadHistory = NO;
    [self p_unableChatFunction];
    [self p_enableChatFunction];
    [self.module.showingMessages removeAllObjects];
    [self.tableView reloadData];
    self.module.sessionEntity = session;
    [self setThisViewTitle:session.name];
    BTLog(@"loading history messages for session(%@)", session.sessionId);
    [self.module loadMoreHistoryCompletion:^(NSUInteger addcount, NSError *error) {
        BTLog(@"history messages for session(%@) loaded, count: %d", session.sessionId, (unsigned int)addcount);
        if (session.unReadMsgCount != 0)
        {
            BTMsgReadACKAPI *readACK = [[BTMsgReadACKAPI alloc] init];
            BTLog(@"Begin Ack.");
            [readACK requestWithObject:@[
                                         self.module.sessionEntity.sessionId,
                                         @(self.module.sessionEntity.lastMsgId),
                                         @(self.module.sessionEntity.sessionType)
                                       ]
                            completion:nil];
            
            self.module.sessionEntity.unReadMsgCount = 0;
            [[BTDatabaseUtil instance] updateSession:self.module.sessionEntity completion:^(NSError *error) {
                BTLog(@"ack finished, db update finished");
            }];
        }
    }];
 }


#pragma mark - Text view delegate
-(void)viewheightChanged:(float)height
{
    [self setValue:@(self.chatInputView.origin.y) forKeyPath:@"_inputViewY"];
}


#pragma mark PrivateAPI
-(UITableViewCell *)p_textCell_tableView:(UITableView *)tableView
                  cellForRowAtIndexPath:(NSIndexPath *)indexPath
                                message:(BTMessageEntity *)message
{
    static NSString *identifier = @"BTChatTextCellIdentifier";
    BTChatBaseCell *cell = (BTChatBaseCell *)[tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell)
    {
        cell = [[BTChatTextCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.session = self.module.sessionEntity;
    NSString *myUserId = [BTRuntimeStatus instance].user.objId;
    if ([message.senderId isEqualToString:myUserId])
    {
        [cell setLocation:BUBBLE_RIGHT];
    }
    else
    {
        [cell setLocation:BUBBLE_LEFT];
    }
    
    if (![[BTUnAckMessageManager instance] isInUnAckQueue:message] && message.state == MSG_SENDING && [message isSendBySelf])
    {
        message.state = MSG_SEND_FAILURE;
    }
    [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
        
    }];

    [cell setContent:message];
    __weak BTChatTextCell *weakCell = (BTChatTextCell *)cell;
    cell.sendAgain = ^{
        [weakCell showSending];
        [weakCell sendTextAgain:message];
    };
    
    return cell;
}

-(UITableViewCell *)p_voiceCell_tableView:(UITableView *)tableView
                    cellForRowAtIndexPath:(NSIndexPath *)indexPath
                                  message:(BTMessageEntity *)message
{
    static NSString *identifier = @"BTVoiceCellIdentifier";
    BTChatBaseCell *cell = (BTChatBaseCell *)[tableView dequeueReusableCellWithIdentifier:identifier];
    
    if (!cell)
    {
        cell = [[BTChatVoiceCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.session = self.module.sessionEntity;
    NSString *myUserId = [BTRuntimeStatus instance].user.objId;
    if ([message.senderId isEqualToString:myUserId])
    {
        [cell setLocation:BUBBLE_RIGHT];
    }
    else
    {
        [cell setLocation:BUBBLE_LEFT];
    }
    [cell setContent:message];
    __weak BTChatVoiceCell *weakCell = (BTChatVoiceCell *)cell;
    [(BTChatVoiceCell *)cell setTapInBubble:^{
        if ([[PlayerManager sharedManager] playingFileName:message.msgContent])
        {
            [[PlayerManager sharedManager] stopPlaying];
        }
        else
        {
            NSString *fileName = message.msgContent;
            [[PlayerManager sharedManager] playAudioWithFileName:fileName delegate:self];
            [message.info setObject:@(1) forKey:kBTVoicePlayed];
            [weakCell showVoicePlayed];
            [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
                BTLog(@"voice message played and db updated");
            }];
        }
    }];
    
    [(BTChatVoiceCell *)cell setEarphonePlay:^{
        NSString *fileName = message.msgContent;
        [[PlayerManager sharedManager] playAudioWithFileName:fileName playerType:EarPhone delegate:self];
        [message.info setObject:@(1) forKey:kBTVoicePlayed];
        [weakCell showVoicePlayed];

        [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
            BTLog(@"voice message earphone played and db updated");
        }];
    }];
    
    [(BTChatVoiceCell *)cell setSpeakerPlay:^{
        NSString *fileName = message.msgContent;
        [[PlayerManager sharedManager] playAudioWithFileName:fileName playerType:Speaker delegate:self];
        [message.info setObject:@(1) forKey:kBTVoicePlayed];
        [weakCell showVoicePlayed];
        [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
            BTLog(@"voice message speaker played and db updated");
        }];
    }];
    
    [(BTChatVoiceCell *)cell setSendAgain:^{
        [weakCell showSending];
        [weakCell sendVoiceAgain:message];
    }];
    
    return cell;
}

-(UITableViewCell *)p_promptCell_tableView:(UITableView *)tableView
                     cellForRowAtIndexPath:(NSIndexPath *)indexPath
                                   message:(BTPromptEntity *)prompt
{
    static NSString *identifier = @"BTPromptCellIdentifier";
    BTPromptCell *cell = (BTPromptCell *)[tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell)
    {
        cell = [[BTPromptCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    NSString *promptMessage = prompt.message;
    [cell setprompt:promptMessage];
    return cell;
}

-(UITableViewCell *)p_emotionCell_tableView:(UITableView *)tableView
                      cellForRowAtIndexPath:(NSIndexPath *)indexPath
                                    message:(BTMessageEntity *)message
{
    static NSString *identifier = @"BTEmotionCellIdentifier";
    BTEmotionCell *cell = (BTEmotionCell *)[tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell)
    {
        cell = [[BTEmotionCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.session = self.module.sessionEntity;
    NSString *myUserId = [BTRuntimeStatus instance].user.objId;
    if ([message.senderId isEqualToString:myUserId])
    {
        [cell setLocation:BUBBLE_RIGHT];
    }
    else
    {
        [cell setLocation:BUBBLE_LEFT];
    }
    
    [cell setContent:message];
    __weak BTEmotionCell *weakCell = cell;
    
    [cell setSendAgain:^{
        [weakCell sendTextAgain:message];
    }];
    
    [cell setTapInBubble:^{
        BTLog(@"emotion cell tapped in bubble");
    }];
    
    return cell;
}


-(UITableViewCell *)p_imageCell_tableView:(UITableView *)tableView
                   cellForRowAtIndexPath:(NSIndexPath *)indexPath
                                 message:(BTMessageEntity *)message
{
    static NSString *identifier = @"BTImageCellIdentifier";
    BTChatImageCell *cell = (BTChatImageCell *)[tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell)
    {
        cell = [[BTChatImageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.session = self.module.sessionEntity;
    NSString *myUserId = [BTRuntimeStatus instance].user.objId;
    if ([message.senderId isEqualToString:myUserId])
    {
        [cell setLocation:BUBBLE_RIGHT];
    }
    else
    {
        [cell setLocation:BUBBLE_LEFT];
    }
    
    [[BTDatabaseUtil instance] updateMessage:message completion:^(BOOL result) {
        
    }];
    [cell setContent:message];
    __weak BTChatImageCell *weakCell = cell;
  
    [cell setSendAgain:^{
        [weakCell sendImageAgain:message];
    }];
    
    [cell setTapInBubble:^{
        [weakCell showPreview];
    }];
    
    [cell setPreview:cell.tapInBubble];
    
    return cell;
}

-(void)p_clickTheRecordButton:(UIButton *)button
{
    switch (button.tag)
    {
        case INPUT_BUTTON_TYPE_VOICE:
        {
            // prepare to record
            [button setImage:[UIImage imageNamed:@"dd_record_normal"] forState:UIControlStateNormal];
            button.tag = INPUT_BUTTON_TYPE_TEXT;
            [self.chatInputView willBeginInput];
            if ([_currentInputContent length] > 0)
            {
                [self.chatInputView.textView setText:_currentInputContent];
            }
            [self.chatInputView.textView resignFirstResponder];
            break;
        }
        case INPUT_BUTTON_TYPE_TEXT:
        {
            // prepare to input text
            [self p_hideBottomComponent];
            [button setImage:[UIImage imageNamed:@"dd_input_normal"] forState:UIControlStateNormal];
            button.tag = INPUT_BUTTON_TYPE_VOICE;
            [self.chatInputView willBeginRecord];
            [self.chatInputView.textView becomeFirstResponder];
            _currentInputContent = self.chatInputView.textView.text;
            if ([_currentInputContent length] > 0)
            {
                [self.chatInputView.textView setText:nil];
            }
            break;
        }
    }
}

-(void)p_record:(UIButton *)button
{
    [self.chatInputView.recordButton setHighlighted:YES];
    [self.chatInputView.buttonTitle setText:@"松开发送"];
    if (![[self.view subviews] containsObject:_recordingView])
    {
        [self.view addSubview:_recordingView];
    }
    [_recordingView setHidden:NO];
    [_recordingView setRecordingState:SHOW_VOLUMN];
    [[RecorderManager sharedManager] setDelegate:self];
    [[RecorderManager sharedManager] startRecording];
    BTLog(@"recordding...");
}

-(void)p_willCancelRecord:(UIButton *)button
{
    [_recordingView setHidden:NO];
    [_recordingView setRecordingState:SHOW_CANCEL_SEND];
    BTLog(@"will cancel record");
}

-(void)p_cancelRecord:(UIButton *)button
{
    [self.chatInputView.recordButton setHighlighted:NO];
    [self.chatInputView.buttonTitle setText:@"按住说话"];
    [_recordingView setHidden:YES];
    [[RecorderManager sharedManager] cancelRecording];
    BTLog(@"cancel record");
}

-(void)p_sendRecord:(UIButton *)button
{
    [self.chatInputView.recordButton setHighlighted:NO];
    [self.chatInputView.buttonTitle setText:@"按住说话"];
    [[RecorderManager sharedManager] stopRecording];
    BTLog(@"send record");
}


-(void)p_endCancelRecord:(UIButton *)button
{
    [_recordingView setHidden:NO];
    [_recordingView setRecordingState:SHOW_VOLUMN];
}

-(void)p_tapOnTableView:(UIGestureRecognizer *)sender
{
    if (_bottomShowComponent)
    {
        [self p_hideBottomComponent];
    }
}

-(void)p_hideBottomComponent
{
    _bottomShowComponent = _bottomShowComponent & 0;
    // hide all bottom component
    [self.chatInputView.textView resignFirstResponder];
    [UIView animateWithDuration:0.25 animations:^{
        [self.chatUtility.view setFrame:BTComponentBottom];
        [self.emotions.view setFrame:BTComponentBottom];
        [self.chatInputView setFrame:BTInputBottomFrame];
    }];

    [self setValue:@(self.chatInputView.origin.y) forKeyPath:@"_inputViewY"];
}

-(void)p_enableChatFunction
{
    [self.chatInputView setUserInteractionEnabled:YES];
}

-(void)p_unableChatFunction
{
    [self.chatInputView setUserInteractionEnabled:NO];
}

-(void)p_popViewController
{
    [self p_hideBottomComponent];
    // [self.navigationController popViewControllerAnimated:YES];
    self.title = @"";
    [self setThisViewTitle:@""];
}


#pragma mark BTEmotionViewCOntroller Delegate
-(void)emotionViewClickSendButton
{
    [self sendTextMessage];
}


-(void)textViewEnterSend
{
    [self sendTextMessage];
}

#pragma mark - KVO
-(void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqualToString:@"sessionEntity.sessionId"])
    {
        if ([change objectForKey:@"new"] != nil)
        {
            [self setThisViewTitle:self.module.sessionEntity.name];
        }
    }
    if ([keyPath isEqualToString:@"showingMessages"])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.tableView reloadData];
            if (self.hadLoadHistory == NO)
            {
                [self scrollToBottomAnimated:NO];
            }
        });
    }
    if ([keyPath isEqualToString:@"_inputViewY"])
    {
        float maxY = BTFullHeight - BTInputMinHeight;
        float gap = maxY - _inputViewY;
        [UIView animateWithDuration:0.25 animations:^{
            _tableView.contentInset = UIEdgeInsetsMake(_tableView.contentInset.top, 0, gap + 60, 0);
            if (_bottomShowComponent & SHOW_EMOTION)
            {
                [self.emotions.view setTop:self.chatInputView.bottom];
            }
            if (_bottomShowComponent & SHOW_UTILITY)
            {
                [self.chatUtility.view setTop:self.chatInputView.bottom];
            }
            
        } completion:^(BOOL finished) {
        
        }];
        if (gap != 0)
        {
            [self scrollToBottomAnimated:NO];
        }
    }
}

-(void)showUtilitys
{
    [_recordButton setImage:[UIImage imageNamed:@"dd_record_normal"] forState:UIControlStateNormal];
    _recordButton.tag = INPUT_BUTTON_TYPE_VOICE;
    [self.chatInputView willBeginInput];
    if ([_currentInputContent length] > 0)
    {
        [self.chatInputView.textView setText:_currentInputContent];
    }
    
    if (self.chatUtility == nil)
    {
        self.chatUtility = [BTChatUtilityViewController new];
        [self addChildViewController:self.chatUtility];
        self.chatUtility.view.frame = CGRectMake(0, self.view.size.height, BTFullWidth , 280);
        [self.view addSubview:self.chatUtility.view];
    }
    
    if (_bottomShowComponent & SHOW_KEYBOARD)
    {
        //显示的是键盘,这是需要隐藏键盘，显示插件，不需要动画
        _bottomShowComponent = (_bottomShowComponent & 0) | SHOW_UTILITY;
        [self.chatInputView.textView resignFirstResponder];
        [self.chatUtility.view setFrame:BTUtilityFrame];
        [self.emotions.view setFrame:BTComponentBottom];
    }
    else if (_bottomShowComponent & SHOW_UTILITY)
    {
        //插件面板本来就是显示的,这时需要隐藏所有底部界面
        // [self p_hideBottomComponent];
        [self.chatInputView.textView becomeFirstResponder];
        _bottomShowComponent = _bottomShowComponent & HIDE_UTILITY;
    }
    else if (_bottomShowComponent & SHOW_EMOTION)
    {
        //显示的是表情，这时需要隐藏表情，显示插件
        [self.emotions.view setFrame:BTComponentBottom];
        [self.chatUtility.view setFrame:BTUtilityFrame];
        _bottomShowComponent = (_bottomShowComponent & HIDE_EMOTION) | SHOW_UTILITY;
    }
    else
    {
        //这是什么都没有显示，需用动画显示插件
        _bottomShowComponent = _bottomShowComponent | SHOW_UTILITY;
        [UIView animateWithDuration:0.25 animations:^{
            [self.chatUtility.view setFrame:BTUtilityFrame];
            [self.chatInputView setFrame:BTInputTopFrame];
        }];
        [self setValue:@(BTInputTopFrame.origin.y) forKeyPath:@"_inputViewY"];
    }
}

-(void)showEmotions
{
    [_recordButton setImage:[UIImage imageNamed:@"dd_record_normal"] forState:UIControlStateNormal];
    _recordButton.tag = INPUT_BUTTON_TYPE_TEXT;
    [self.chatInputView willBeginInput];
    if ([_currentInputContent length] > 0)
    {
        [self.chatInputView.textView setText:_currentInputContent];
    }
    
    if (self.emotions == nil) {
        self.emotions = [BTEmotionsViewController new];
        [self.emotions.view setBackgroundColor:[UIColor whiteColor]];
        self.emotions.view.frame=BTComponentBottom;
        self.emotions.delegate = self;
        [self.view addSubview:self.emotions.view];
    }
    if (_bottomShowComponent & SHOW_KEYBOARD)
    {
        //显示的是键盘,这是需要隐藏键盘，显示表情，不需要动画
        _bottomShowComponent = (_bottomShowComponent & 0) | SHOW_EMOTION;
        [self.chatInputView.textView resignFirstResponder];
        [self.emotions.view setFrame:BTEmotionFrame];
        [self.chatUtility.view setFrame:BTComponentBottom];
    }
    else if (_bottomShowComponent & SHOW_EMOTION)
    {
        //表情面板本来就是显示的,这时需要隐藏所有底部界面
        [self.chatInputView.textView resignFirstResponder];
        _bottomShowComponent = _bottomShowComponent & HIDE_EMOTION;
    }
    else if (_bottomShowComponent & SHOW_UTILITY)
    {
        //显示的是插件，这时需要隐藏插件，显示表情
        [self.chatUtility.view setFrame:BTComponentBottom];
        [self.emotions.view setFrame:BTEmotionFrame];
        _bottomShowComponent = (_bottomShowComponent & HIDE_UTILITY) | SHOW_EMOTION;
    }
    else
    {
        //这是什么都没有显示，需用动画显示表情
        _bottomShowComponent = _bottomShowComponent | SHOW_EMOTION;
        [UIView animateWithDuration:0.25 animations:^{
            [self.emotions.view setFrame:BTEmotionFrame];
            [self.chatInputView setFrame:BTInputTopFrame];
        }];
        [self setValue:@(BTInputTopFrame.origin.y) forKeyPath:@"_inputViewY"];
    }
}


#pragma mark - KeyBoardNotification
-(void)handleWillShowKeyboard:(NSNotification *)notification
{
    CGRect keyboardRect;
    keyboardRect = [(notification.userInfo)[UIKeyboardFrameEndUserInfoKey] CGRectValue];
    keyboardRect = [self.view convertRect:keyboardRect fromView:nil];
    _bottomShowComponent = _bottomShowComponent | SHOW_KEYBOARD;
    [UIView animateWithDuration:0.25 animations:^{
        [self.chatInputView setFrame:CGRectMake(0, keyboardRect.origin.y - BTInputHeight, self.view.size.width, BTInputHeight)];
    }];
    [self setValue:@(keyboardRect.origin.y - BTInputHeight) forKeyPath:@"_inputViewY"];
}

-(void)handleWillHideKeyboard:(NSNotification *)notification
{
    CGRect keyboardRect;
    keyboardRect = [notification.userInfo[UIKeyboardFrameEndUserInfoKey] CGRectValue];
    keyboardRect = [self.view convertRect:keyboardRect fromView:nil];
    _bottomShowComponent = _bottomShowComponent & HIDE_KEYBOARD;
    if (_bottomShowComponent & SHOW_UTILITY)
    {
        // 显示的是插件
        [UIView animateWithDuration:0.25 animations:^{
            [self.chatInputView setFrame:BTInputTopFrame];
        }];
        [self setValue:@(self.chatInputView.origin.y) forKeyPath:@"_inputViewY"];
    }
    else if (_bottomShowComponent & SHOW_EMOTION)
    {
        // 显示的是表情
        [UIView animateWithDuration:0.25 animations:^{
            [self.chatInputView setFrame:BTInputTopFrame];
        }];
        [self setValue:@(self.chatInputView.origin.y) forKeyPath:@"_inputViewY"];
    }
    else
    {
        [self p_hideBottomComponent];
    }
}

-(void)titleTap
{
    if ([self.module.sessionEntity isGroup])
    {
        return;
    }
    
    [self.module getCurrentUser:^(BTUserEntity *user) {
        BTPublicProfileViewController *profile = [BTPublicProfileViewController new];
        profile.title = user.nick;
        profile.user = user;
        [self.navigationController pushViewController:profile animated:YES];
    }];
}

-(void)n_receiveMessage:(NSNotification *)notification
{
    if (![self.navigationController.topViewController isEqual:self])
    {
        // 当前不是聊天界面直接返回
        BTLog(@"进来了");
        return;
    }
    
    BTMessageEntity *message = [notification object];
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    if (state == UIApplicationStateBackground)
    {
        if ([message.sessionId isEqualToString:self.module.sessionEntity.sessionId])
        {
            [self.module addShowMessage:message];
            [self.module updateSessionUpdateTime:message.msgTime];
            dispatch_async(dispatch_get_main_queue(), ^{
                [self scrollToBottomAnimated:YES];
            });
        }
        return;
    }
    
    // 显示消息
    if ([message.sessionId isEqualToString:self.module.sessionEntity.sessionId])
    {
        [self.module addShowMessage:message];
        [self.module updateSessionUpdateTime:message.msgTime];
        [[BTMessageModule shareInstance] sendMsgRead:message];
        // [self scrollToBottomAnimated:YES];
    }
}

-(void)recordingTimeout
{
    
}

// 录音机停止采集声音
-(void)recordingStopped
{
    
}

-(void)recordingFailed:(NSString *)failureInfoString
{
    
}

-(void)levelMeterChanged:(float)levelMeter
{
    [_recordingView setVolume:levelMeter];
}

-(void)reloginSuccess
{
    [self.module getNewMsg:^(NSUInteger addcount, NSError *error) {
        [self.tableView reloadData];
    }];
}

@end
