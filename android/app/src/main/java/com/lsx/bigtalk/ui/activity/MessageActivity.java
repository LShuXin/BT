package com.lsx.bigtalk.ui.activity;

import static com.lsx.bigtalk.R.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.DB.entity.GroupEntity;
import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.entity.PeerEntity;
import com.lsx.bigtalk.DB.entity.UserEntity;
import com.lsx.bigtalk.DB.sp.SystemConfigSp;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.app.IMApplication;
import com.lsx.bigtalk.config.DBConstant;
import com.lsx.bigtalk.config.HandlerConstant;
import com.lsx.bigtalk.config.IntentConstant;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.entity.AudioMessageEntity;
import com.lsx.bigtalk.imservice.entity.ImageMessageEntity;
import com.lsx.bigtalk.imservice.entity.TextMessageEntity;
import com.lsx.bigtalk.imservice.entity.UnreadMessageEntity;
import com.lsx.bigtalk.imservice.event.MessageEvent;
import com.lsx.bigtalk.imservice.event.PriorityEvent;
import com.lsx.bigtalk.imservice.event.ImageSelectEvent;
import com.lsx.bigtalk.imservice.manager.IMLoginManager;
import com.lsx.bigtalk.imservice.manager.IMStackManager;
import com.lsx.bigtalk.imservice.service.IMService;
import com.lsx.bigtalk.imservice.support.IMServiceConnector;
import com.lsx.bigtalk.ui.adapter.MessageAdapter;
import com.lsx.bigtalk.ui.adapter.album.AlbumHelper;
import com.lsx.bigtalk.ui.adapter.album.ImageBucket;
import com.lsx.bigtalk.ui.adapter.album.ImageItem;
import com.lsx.bigtalk.ui.base.BTBaseActivity;
import com.lsx.bigtalk.ui.helper.AudioPlayerHandler;
import com.lsx.bigtalk.ui.helper.AudioRecordHandler;
import com.lsx.bigtalk.ui.helper.Emoparser;
import com.lsx.bigtalk.ui.widget.CustomEditView;
import com.lsx.bigtalk.ui.widget.EmojiGridView;
import com.lsx.bigtalk.ui.widget.MGProgressbar;
import com.lsx.bigtalk.ui.widget.YayaEmojiGridView;
import com.lsx.bigtalk.utils.CommonUtil;
import com.lsx.bigtalk.helper.IMUIHelper;
import com.lsx.bigtalk.utils.Logger;


public class MessageActivity extends BTBaseActivity
        implements
        OnRefreshListener2<ListView>,
        View.OnClickListener,
        OnTouchListener,
        TextWatcher,
        SensorEventListener {
    
    private final Logger logger = Logger.getLogger(MessageActivity.class);
    private static Handler recordMsgHandler = null;
    private PullToRefreshListView pullToRefreshListView = null;
    private CustomEditView messageEditView = null;
    private TextView sendMsgBtn = null;
    private Button recordFlatBtn = null;
    private ImageView addTextBtn = null;
    private ImageView volumnImageView = null;
    private LinearLayout volumeBg = null;
    private ImageView addRecordBtn = null;
    private ImageView addPhotoBtn = null;
    private ImageView addEmojiBtn = null;
    private LinearLayout emojiPanelView = null;
    private EmojiGridView emojiGridView = null;
    private YayaEmojiGridView yayaemojiGridView = null;
    private RadioGroup emojiRadioGroup = null;
    private String audioSavePath = null;
    private InputMethodManager inputManager = null;
    private AudioRecordHandler audioRecorderInstance = null;
    private TextView new_msg_tips = null;
    private MessageAdapter messageAdapter = null;
    private Dialog volumeDialog = null;
    private View photoPanelView = null;


    private List<ImageBucket> albumList = null;
    MGProgressbar progressbar = null;
    
    private SensorManager sensorManager = null;
    private Sensor sensor = null;


    private String photoSavePath = "";

    private IMService imService;
    private UserEntity loginUserEntity;
    private PeerEntity peerEntity;
    private String currentSessionKey;
    private int historyMsgPageNo = 0;

    int rootBottom = Integer.MIN_VALUE;
    int keyboardHeight = 0;
    InputMethodSwitchBroadcastReceiver inputMethodSwitchBroadcastReceiver;
    private String currentInputMethod;
    private Toast mToast;

    private final IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("MessageActivity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            initData();
        }

        @Override
        public void onServiceDisconnected() {
            logger.d("MessageActivity#onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentSessionKey = getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);

        initSoftInput();
        initImageRelated();
        initAudioRelated();
        initView();

        imServiceConnector.connect(this);
        EventBus.getDefault().register(this, SysConstant.MESSAGE_EVENTBUS_PRIORITY);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        logger.d("MessageActivity#onNewIntent:%s", this);
        super.onNewIntent(intent);
        setIntent(intent);
        historyMsgPageNo = 0;
        if (intent == null) {
            return;
        }
        String newSessionKey = getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        if (newSessionKey == null) {
            return;
        }
        logger.d("MessageActivity#onNewIntent#newSessionKey:%s", newSessionKey);
        if (!newSessionKey.equals(currentSessionKey)) {
            currentSessionKey = newSessionKey;
            initData();
        }
    }

    @Override
    protected void onResume() {
        logger.d("MessageActivity#onResume");
        super.onResume();
        IMApplication.gifRunning = true;
        historyMsgPageNo = 0;
        // not the first time
        if (imService != null) {
            consumeUnreadMsgs();
        }
    }

    @Override
    protected void onDestroy() {
        logger.d("MessageActivity#onDestroy");
        historyMsgPageNo = 0;
        imServiceConnector.disconnect(this);
        EventBus.getDefault().unregister(this);
        messageAdapter.clearItem();
        albumList.clear();
        sensorManager.unregisterListener(this, sensor);
        ImageMessageEntity.clearImageMessageList();
        unregisterReceiver(inputMethodSwitchBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        IMApplication.gifRunning = false;
        cancelToast();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode) {
            return;
        }
            
        switch (requestCode) {
            case SysConstant.CAMERA_FOR_DATA:
                handleTakePhotoSuccess(data);
                break;
            case SysConstant.ALBUM_FOR_DATA:
                setIntent(data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showToast(int resId) {
        String text = getResources().getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(MessageActivity.this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
    
    private void initData() {
        historyMsgPageNo = 0;
        messageAdapter.clearItem();
        ImageMessageEntity.clearImageMessageList();
        loginUserEntity = imService.getIMLoginManager().getUserEntity();
        peerEntity = imService.getIMSessionManager().findPeerEntity(currentSessionKey);
        // 头像、历史消息加载、取消通知
        setTitleByUser();
        loadHistoryMsg();
        messageAdapter.setImService(imService, loginUserEntity);
        imService.getIMUnReadMsgManager().readUnreadSession(currentSessionKey);
        imService.getIMNotificationManager().cancelSessionNotifications(currentSessionKey);
    }

    private void initSoftInput() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodSwitchBroadcastReceiver = new InputMethodSwitchBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.INPUT_METHOD_CHANGED");
        registerReceiver(inputMethodSwitchBroadcastReceiver, filter);

        SystemConfigSp.instance().init(this);
        String str = Settings.Secure.getString(MessageActivity.this.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        String[] strArr = str.split("\\.");
        if (strArr.length >= 3) {
            currentInputMethod = strArr[1];
            if (SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD).equals(currentInputMethod)) {
                keyboardHeight = SystemConfigSp.instance().getIntConfig(SystemConfigSp.SysCfgDimension.KEYBOARDHEIGHT);
            } else {
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD, currentInputMethod);
            }
        }
    }
    
    private void setTitleByUser() {
        setTitle(peerEntity.getMainName());
        int sessionType = peerEntity.getType();
        switch (sessionType) {
            case DBConstant.SESSION_TYPE_GROUP: 
            {
                GroupEntity group = (GroupEntity) peerEntity;
                Set<Integer> memberIds = group.getlistGroupMemberIds();
                if (!memberIds.contains(loginUserEntity.getPeerId())) {
                    // the app user is not in the group
                    Toast.makeText(MessageActivity.this, R.string.no_group_member, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case DBConstant.SESSION_TYPE_SINGLE: 
            {
                topCenterTitleTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IMUIHelper.openUserProfileActivity(MessageActivity.this, peerEntity.getPeerId());
                    }
                });
                break;
            }
        }
    }

    private void handleChoosePhotoSuccess(List<ImageItem> list) {
        ArrayList<ImageMessageEntity> imageMsgList = new ArrayList<>();
        ArrayList<ImageItem> itemList = (ArrayList<ImageItem>) list;
        for (ImageItem item : itemList) {
            ImageMessageEntity imageMessage = ImageMessageEntity.buildForSend(item, loginUserEntity, peerEntity);
            imageMsgList.add(imageMessage);
            pushMsg(imageMessage);
        }
        imService.getIMMessageManager().sendImages(imageMsgList);
    }



    /**
     * 背景: 1.EventBus的cancelEventDelivery的只能在postThread中运行，而且没有办法绕过这一点
     * 2. onEvent(A a)  onEventMainThread(A a) 这个两个是没有办法共存的
     * 解决: 抽离出那些需要优先级的event，在onEvent通过handler调用主线程，
     * 然后cancelEventDelivery
     * <p/>
     * todo  need find good solution
     */
    public void onEvent(PriorityEvent event) {
        if (event.event == PriorityEvent.Event.MSG_RECEIVED_MESSAGE) {
            MessageEntity entity = (MessageEntity) event.object;
            if (currentSessionKey.equals(entity.getSessionKey())) {
                Message message = Message.obtain();
                message.what = HandlerConstant.MESSAGE_RECEIVED;
                message.obj = entity;
                recordMsgHandler.sendMessage(message);
                EventBus.getDefault().cancelEventDelivery(event);
            }
        }
    }
    
    public void onEventMainThread(ImageSelectEvent event) {
        List<ImageItem> itemList = event.getList();
        if (itemList != null && !itemList.isEmpty())
        {
            handleChoosePhotoSuccess(itemList);
        }
    }
    
    public void onEventMainThread(MessageEvent event) {
        switch (event.getEvent()) {
            case SEND_MESSAGE_SUCCESS:
            {
                handleMsgSendSuccess(event.getMessageEntity());
                break;
            }
            case SEND_MESSAGE_FAILED:
                showToast(R.string.message_send_failed);
            case SEND_MESSAGE_TIMEOUT:
            {
                handleMsgSendFailed(event.getMessageEntity());
                break;
            }
            case IMAGE_UPLOAD_FAILED:
            {
                ImageMessageEntity imageMessage = (ImageMessageEntity) event.getMessageEntity();
                messageAdapter.updateItemState(imageMessage);
                showToast(R.string.message_send_failed);
                break;
            }
            case IMAGE_UPLOAD_SUCCESS:
            {
                ImageMessageEntity imageMessage = (ImageMessageEntity) event.getMessageEntity();
                messageAdapter.updateItemState(imageMessage);
                break;
            }
            case HISTORY_MSG_OBTAINED:
            {
                if (historyMsgPageNo == 1) {
                    messageAdapter.clearItem();
                    loadHistoryMsg();
                }
                break;
            }
                
        }
    }
    
    protected void initAudioRelated() {
        recordMsgHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.RECORD_FINISHED:
                    {
                        onRecordVoiceEnd((Float) msg.obj);
                        break; 
                    }
                    case HandlerConstant.PLAY_STOPPED:
                    {
                        // 其他地方处理了
                        // adapter.stopVoicePlayAnim((String) msg.obj);
                        break;
                    }
                    case HandlerConstant.RECEIVE_MAX_VOLUME:
                    {
                        onReceiveMaxVolume((Integer) msg.obj);
                        break;
                    }
                    case HandlerConstant.RECORD_AUDIO_TOO_LONG:
                    {
                        onRecordLengthReachMax();
                        break;
                    }
                    case HandlerConstant.MESSAGE_RECEIVED:
                    {
                        MessageEntity entity = (MessageEntity) msg.obj;
                        handleMsgReceived(entity);
                        break;
                    }
                    default:
                        break;
                }
            }
        };

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    private void handleMsgSendSuccess(MessageEntity messageEntity) {
        messageAdapter.updateItemState(messageEntity);
    }
    
    private void consumeUnreadMsgs() {
        logger.d("MessageActivity#consumeUnreadMsgs");
        UnreadMessageEntity unreadEntity = imService.getIMUnReadMsgManager().findUnread(currentSessionKey);
        if (null == unreadEntity) {
            return;
        }
        int unReadCnt = unreadEntity.getUnReadCnt();
        if (unReadCnt > 0) {
            imService.getIMNotificationManager().cancelSessionNotifications(currentSessionKey);
            messageAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }
    
    private void handleMsgReceived(MessageEntity entity) {
        logger.d("MessageActivity#handleMsgReceived");
        imService.getIMUnReadMsgManager().ackReadMsg(entity);
        pushMsg(entity);
        ListView listView = pullToRefreshListView.getRefreshableView();
        if (listView != null) {
            if (listView.getLastVisiblePosition() < messageAdapter.getCount()) {
                new_msg_tips.setVisibility(View.VISIBLE);
            } else {
                scrollToBottom();
            }
        }
    }
    
    private void handleMsgSendFailed(MessageEntity messageEntity) {
        logger.d("MessageActivity#handleMsgSendFailed");
        messageAdapter.updateItemState(messageEntity);
    }

    private void showGroupManageActivity() {
        Intent i = new Intent(this, GroupManageActivity.class);
        i.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
        startActivity(i);
    }

    private void initImageRelated() {
        Emoparser.getInstance(MessageActivity.this);
        IMApplication.gifRunning = true;
        AlbumHelper albumHelper = AlbumHelper.getHelper(MessageActivity.this);
        albumList = albumHelper.getImagesBucketList(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        layoutInflater.inflate(R.layout.message_activity, baseActivity);


        // app bar
        setTopLeftBtnImage(R.drawable.top_back);
        setTopLeftBtnTitleText(getResources().getString(R.string.top_left_back));
        setTopRightBtnImage(R.drawable.group_manage);
        topLeftBtnImageView.setOnClickListener(this);
        topLeftBtnTitleTextView.setOnClickListener(this);
        topRightBtnImageView.setOnClickListener(this);


        // msg list
        pullToRefreshListView = findViewById(R.id.message_list);
        new_msg_tips = findViewById(R.id.new_msg_tips);
        pullToRefreshListView.getRefreshableView().addHeaderView(
                layoutInflater.inflate(R.layout.message_list_header, pullToRefreshListView.getRefreshableView(), false));
        Drawable loadingDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.pull_to_refresh_indicator, this.getTheme());
        final int indicatorSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29, getResources().getDisplayMetrics());
        if (null != loadingDrawable) {
           loadingDrawable.setBounds(new Rect(0, indicatorSize, 0, indicatorSize));
        }
        pullToRefreshListView.getLoadingLayoutProxy().setLoadingDrawable(loadingDrawable);
        pullToRefreshListView.getRefreshableView().setCacheColorHint(Color.WHITE);
        pullToRefreshListView.getRefreshableView().setSelector(new ColorDrawable(Color.WHITE));
        pullToRefreshListView.getRefreshableView().setOnTouchListener((v, event) -> {
            v.performClick();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                messageEditView.clearFocus();
                if (emojiPanelView.getVisibility() == View.VISIBLE) {
                    emojiPanelView.setVisibility(View.GONE);
                }

                if (photoPanelView.getVisibility() == View.VISIBLE) {
                    photoPanelView.setVisibility(View.GONE);
                }
                inputManager.hideSoftInputFromWindow(messageEditView.getWindowToken(), 0);
            }
            return false;
        });
        messageAdapter = new MessageAdapter(this);
        pullToRefreshListView.setAdapter(messageAdapter);
        pullToRefreshListView.setOnRefreshListener(this);
        pullToRefreshListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true) {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                        new_msg_tips.setVisibility(View.GONE);
                    }
                }
            }
        });
        new_msg_tips.setOnClickListener(this);


        // input utils
        addRecordBtn = findViewById(R.id.add_record_btn);
        addRecordBtn.setOnClickListener(this);
        addTextBtn = findViewById(R.id.add_text_btn);
        addTextBtn.setOnClickListener(this);

        recordFlatBtn = findViewById(R.id.record_flat_btn);
        recordFlatBtn.setOnTouchListener(this);
        messageEditView = findViewById(id.message_editView);
        messageEditView.setOnFocusChangeListener(msgEditOnFocusChangeListener);
        messageEditView.setOnClickListener(this);
        messageEditView.addTextChangedListener(this);

        addEmojiBtn = findViewById(R.id.add_emoji_btn);
        addEmojiBtn.setOnClickListener(this);

        RelativeLayout.LayoutParams messageEditViewLayoutParam = (LayoutParams) messageEditView.getLayoutParams();
        messageEditViewLayoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.add_record_btn);
        messageEditViewLayoutParam.addRule(RelativeLayout.LEFT_OF, R.id.add_emoji_btn);

        sendMsgBtn = findViewById(R.id.send_message_btn);
        sendMsgBtn.setOnClickListener(this);

        addPhotoBtn = findViewById(R.id.add_photo_btn);
        addPhotoBtn.setOnClickListener(this);
        
        
        // volumn panel 
        volumeDialog = new Dialog(this, R.style.volumeDialogStyle);
        volumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(volumeDialog.getWindow()).setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        volumeDialog.setContentView(R.layout.volume_dialog);
        volumeDialog.setCanceledOnTouchOutside(true);
        volumnImageView = volumeDialog.findViewById(R.id.volume_img);
        volumeBg = volumeDialog.findViewById(R.id.volume_bg);

        
        // photo panel
        photoPanelView = findViewById(id.photo_panel);
        LayoutParams photoPanelViewLayoutparams = (LayoutParams) photoPanelView.getLayoutParams();
        if (keyboardHeight > 0) {
            photoPanelViewLayoutparams.height = keyboardHeight;
            photoPanelView.setLayoutParams(photoPanelViewLayoutparams);
        }
        View choosePhotoBtn = findViewById(R.id.choose_photo_btn);
        choosePhotoBtn.setOnClickListener(this);
        View takePhotoBtn = findViewById(R.id.take_photo_btn);
        takePhotoBtn.setOnClickListener(this);


        // emoji panel
        emojiPanelView = findViewById(R.id.emoji_layout);
        LayoutParams emojiPanelViewLayoutParams = (LayoutParams) emojiPanelView.getLayoutParams();
        if (keyboardHeight > 0) {
            emojiPanelViewLayoutParams.height = keyboardHeight;
            emojiPanelView.setLayoutParams(emojiPanelViewLayoutParams);
        }
        emojiGridView = findViewById(R.id.emo_gridview);
        emojiGridView.setOnEmojiGridViewItemClick(onEmojiGridViewItemClick);
        emojiGridView.setAdapter();
        yayaemojiGridView = findViewById(R.id.yaya_emo_gridview);
        yayaemojiGridView.setOnYayaEmojiGridViewItemClick(yayaOnemojiGridViewItemClick);
        yayaemojiGridView.setAdapter();
        emojiRadioGroup = findViewById(R.id.emoji_radio_group);
        emojiRadioGroup.setOnCheckedChangeListener(emoOnCheckedChangeListener);


        // loading
        View view = LayoutInflater.from(MessageActivity.this)
                .inflate(R.layout.progress_ly, null);
        progressbar = view.findViewById(R.id.tt_progress);
        LayoutParams progressBarLayoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        progressBarLayoutParams.bottomMargin = 50;
        addContentView(view, progressBarLayoutParams);

        //ROOT_LAYOUT_LISTENER
        appBarRoot.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration config) {
        super.onConfigurationChanged(config);
    }
    
    private void loadHistoryMsg() {
        historyMsgPageNo++;
        List<MessageEntity> msgList = imService.getIMMessageManager().loadHistoryMsg(historyMsgPageNo, currentSessionKey, peerEntity);
        pushMsg(msgList);
        scrollToBottom();
    }

    public void pushMsg(MessageEntity msg) {
        logger.d("MessageActivity#pushMsg %s", msg);
        messageAdapter.addItem(msg);
    }

    public void pushMsg(List<MessageEntity> entityList) {
        logger.d("MessageActivity#pushMsg %d", entityList.size());
        messageAdapter.loadHistoryList(entityList);
    }
    
    public void onRecordLengthReachMax() {
        try {
            if (audioRecorderInstance.isRecording()) {
                audioRecorderInstance.setRecording(false);
            }
            if (volumeDialog.isShowing()) {
                volumeDialog.dismiss();
            }

            recordFlatBtn.setBackgroundResource(R.drawable.pannel_btn_voiceforward_normal);

            audioRecorderInstance.setRecordTime(SysConstant.MAX_SOUND_RECORD_TIME);
            onRecordVoiceEnd(SysConstant.MAX_SOUND_RECORD_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onReceiveMaxVolume(int voiceValue) {
        if (voiceValue < 200.0) {
            volumnImageView.setImageResource(R.drawable.volume_01);
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            volumnImageView.setImageResource(R.drawable.volume_02);
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            volumnImageView.setImageResource(R.drawable.volume_03);
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            volumnImageView.setImageResource(R.drawable.volume_04);
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            volumnImageView.setImageResource(R.drawable.volume_05);
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            volumnImageView.setImageResource(R.drawable.volume_06);
        } else if (voiceValue > 28000.0) {
            volumnImageView.setImageResource(R.drawable.volume_07);
        }
    }

    private void handleTakePhotoSuccess(Intent ignored_) {
        ImageMessageEntity imageMessage = ImageMessageEntity.buildForSend(photoSavePath, loginUserEntity, peerEntity);
        List<ImageMessageEntity> sendList = new ArrayList<>(1);
        sendList.add(imageMessage);
        imService.getIMMessageManager().sendImages(sendList);
        pushMsg(imageMessage);
        messageEditView.clearFocus();//消除焦点
    }

    private void onRecordVoiceEnd(float audioLen) {
        logger.d("MessageActivity#chat#onRecordVoiceEnd %f", audioLen);
        AudioMessageEntity audioMessage = AudioMessageEntity.buildForSend(audioLen, audioSavePath, loginUserEntity, peerEntity);
        imService.getIMMessageManager().sendVoice(audioMessage);
        pushMsg(audioMessage);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        
    }

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        refreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ListView msgListView = pullToRefreshListView.getRefreshableView();
                int preCount = msgListView.getCount();
                MessageEntity topMessageEntity = messageAdapter.getTopMsgEntity();
                if (topMessageEntity != null) {
                    List<MessageEntity> historyMsgInfo = imService.getIMMessageManager().loadHistoryMsg(topMessageEntity, historyMsgPageNo);
                    if (!historyMsgInfo.isEmpty()) {
                        historyMsgPageNo++;
                        messageAdapter.loadHistoryList(historyMsgInfo);
                    }
                }

                int afterCount = msgListView.getCount();
                msgListView.setSelection(afterCount - preCount);
                refreshView.onRefreshComplete();
            }
        }, 200);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                actFinish();
                break;
            case R.id.right_btn:
                showGroupManageActivity();
                break;
            case R.id.add_photo_btn:
            {
                recordFlatBtn.setVisibility(View.GONE);
                addTextBtn.setVisibility(View.GONE);
                messageEditView.setVisibility(View.VISIBLE);
                addRecordBtn.setVisibility(View.VISIBLE);
                addEmojiBtn.setVisibility(View.VISIBLE);

                if (keyboardHeight != 0) {
                    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }
                if (photoPanelView.getVisibility() == View.VISIBLE) {
                    if (!messageEditView.hasFocus()) {
                        messageEditView.requestFocus();
                    }
                    inputManager.toggleSoftInputFromWindow(messageEditView.getWindowToken(), 1, 0);
                    if (keyboardHeight == 0) {
                        photoPanelView.setVisibility(View.GONE);
                    }
                } else if (photoPanelView.getVisibility() == View.GONE) {
                    photoPanelView.setVisibility(View.VISIBLE);
                    inputManager.hideSoftInputFromWindow(messageEditView.getWindowToken(), 0);
                }
                if (null != emojiPanelView
                        && emojiPanelView.getVisibility() == View.VISIBLE) {
                    emojiPanelView.setVisibility(View.GONE);
                }

                scrollToBottom();
                break;
            }
            case R.id.choose_photo_btn:
            {
                if (albumList.isEmpty()) {
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.not_found_album), Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MessageActivity.this, PickPhotoActivity.class);
                intent.putExtra(IntentConstant.KEY_SESSION_KEY, currentSessionKey);
                startActivityForResult(intent, SysConstant.ALBUM_FOR_DATA);

                MessageActivity.this.overridePendingTransition(R.anim.album_bottom_enter, R.anim.stay_y);
                messageEditView.clearFocus();
                scrollToBottom();
                break;
            }
            case R.id.take_photo_btn:
            {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoSavePath = CommonUtil.getImageSavePath(System.currentTimeMillis() + ".jpg");
                assert photoSavePath != null;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(photoSavePath)));
                startActivityForResult(intent, SysConstant.CAMERA_FOR_DATA);
                messageEditView.clearFocus();
                scrollToBottom();
                break;
            }
            case R.id.add_emoji_btn:
            {
                recordFlatBtn.setVisibility(View.GONE);
                addTextBtn.setVisibility(View.GONE);
                messageEditView.setVisibility(View.VISIBLE);
                addRecordBtn.setVisibility(View.VISIBLE);
                addEmojiBtn.setVisibility(View.VISIBLE);

                if (keyboardHeight != 0) {
                    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }
                if (emojiPanelView.getVisibility() == View.VISIBLE) {
                    if (!messageEditView.hasFocus()) {
                        messageEditView.requestFocus();
                    }
                    inputManager.toggleSoftInputFromWindow(messageEditView.getWindowToken(), 1, 0);
                    if (keyboardHeight == 0) {
                        emojiPanelView.setVisibility(View.GONE);
                    }
                } else if (emojiPanelView.getVisibility() == View.GONE) {
                    emojiPanelView.setVisibility(View.VISIBLE);
                    yayaemojiGridView.setVisibility(View.VISIBLE);
                    emojiRadioGroup.check(R.id.tab1);
                    emojiGridView.setVisibility(View.GONE);
                    inputManager.hideSoftInputFromWindow(messageEditView.getWindowToken(), 0);
                }
                if (photoPanelView.getVisibility() == View.VISIBLE) {
                    photoPanelView.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.send_message_btn:
            {
                String content = messageEditView.getText().toString();
                if (content.trim().isEmpty()) {
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
                    return;
                }
                TextMessageEntity textMessageEntity = TextMessageEntity.buildForSend(content, loginUserEntity, peerEntity);
                imService.getIMMessageManager().sendText(textMessageEntity);
                messageEditView.setText("");
                pushMsg(textMessageEntity);
                scrollToBottom();
                break;
            }
            case R.id.add_record_btn:
            {
                inputManager.hideSoftInputFromWindow(messageEditView.getWindowToken(), 0);
                messageEditView.setVisibility(View.GONE);
                addRecordBtn.setVisibility(View.GONE);
                recordFlatBtn.setVisibility(View.VISIBLE);
                addTextBtn.setVisibility(View.VISIBLE);
                emojiPanelView.setVisibility(View.GONE);
                photoPanelView.setVisibility(View.GONE);
                messageEditView.setText("");
                break;
            }
            case R.id.add_text_btn:
            {
                recordFlatBtn.setVisibility(View.GONE);
                addTextBtn.setVisibility(View.GONE);
                messageEditView.setVisibility(View.VISIBLE);
                addRecordBtn.setVisibility(View.VISIBLE);
                addEmojiBtn.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.message_editView:
                break;
            case R.id.new_msg_tips:
            {
                scrollToBottom();
                new_msg_tips.setVisibility(View.GONE);
            }
            break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        int id = v.getId();
        scrollToBottom();
        if (id == R.id.add_record_btn) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                if (AudioPlayerHandler.getInstance().isPlaying()) {
                    AudioPlayerHandler.getInstance().stopPlayer();
                }

                y1 = event.getY();
                recordFlatBtn.setBackgroundResource(R.drawable.pannel_btn_voiceforward_pressed);
                recordFlatBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.release_to_send_voice));

                volumnImageView.setImageResource(R.drawable.volume_01);
                volumnImageView.setVisibility(View.VISIBLE);
                volumeBg.setBackgroundResource(R.drawable.recordding);
                volumeDialog.show();
                audioSavePath = CommonUtil
                        .getAudioSavePath(IMLoginManager.getInstance().getLoginId());

                // 这个callback很蛋疼，发送消息从MotionEvent.ACTION_UP 判断
                audioRecorderInstance = new AudioRecordHandler(audioSavePath);

                Thread audioRecorderThread = new Thread(audioRecorderInstance);
                audioRecorderInstance.setRecording(true);
                logger.d("message_activity#audio#audio record thread starts");
                audioRecorderThread.start();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                y2 = event.getY();
                if (y1 - y2 > 180) {
                    volumnImageView.setVisibility(View.GONE);
                    volumeBg.setBackgroundResource(R.drawable.record_will_cancel);
                } else {
                    volumnImageView.setVisibility(View.VISIBLE);
                    volumeBg.setBackgroundResource(R.drawable.recordding);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                y2 = event.getY();
                if (audioRecorderInstance.isRecording()) {
                    audioRecorderInstance.setRecording(false);
                }
                if (volumeDialog.isShowing()) {
                    volumeDialog.dismiss();
                }
                recordFlatBtn.setBackgroundResource(R.drawable.pannel_btn_voiceforward_normal);
                recordFlatBtn.setText(MessageActivity.this.getResources().getString(
                        R.string.tip_for_voice_forward));
                if (y1 - y2 <= 180) {
                    if (audioRecorderInstance.getRecordTime() >= 0.5) {
                        if (audioRecorderInstance.getRecordTime() < SysConstant.MAX_SOUND_RECORD_TIME) {
                            Message msg = recordMsgHandler.obtainMessage();
                            msg.what = HandlerConstant.RECORD_FINISHED;
                            msg.obj = audioRecorderInstance.getRecordTime();
                            recordMsgHandler.sendMessage(msg);
                        }
                    } else {
                        volumnImageView.setVisibility(View.GONE);
                        volumeBg
                                .setBackgroundResource(R.drawable.record_too_short);
                        volumeDialog.show();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                if (volumeDialog.isShowing())
                                    volumeDialog.dismiss();
                                this.cancel();
                            }
                        }, 700);
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onStop() {
        if (null != messageAdapter) {
            messageAdapter.hidePopup();
        }

        AudioPlayerHandler.getInstance().clear();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            sendMsgBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEditView
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.add_emoji_btn);
            addPhotoBtn.setVisibility(View.GONE);
        } else {
            addPhotoBtn.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (LayoutParams) messageEditView
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.add_emoji_btn);
            sendMsgBtn.setVisibility(View.GONE);
        }
    }
    
    private void scrollToBottom() {
        ListView listView = pullToRefreshListView.getRefreshableView();
        if (listView != null) {
            listView.setSelection(messageAdapter.getCount() + 1);
        }
        new_msg_tips.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        logger.d("message_activity#onPause:%s", this);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent arg0) {
        try {
            if (!AudioPlayerHandler.getInstance().isPlaying()) {
                return;
            }
            float range = arg0.values[0];
            if (null != sensor && range == sensor.getMaximumRange()) {
                // 屏幕恢复亮度
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_NORMAL, this);
            } else {
                // 屏幕变黑
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_IN_CALL, this);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static Handler getRecordMsgHandler() {
        return recordMsgHandler;
    }

    private void actFinish() {
        inputManager.hideSoftInputFromWindow(messageEditView.getWindowToken(), 0);
        IMStackManager.getStackManager().popTopActivitiesUntil(MainActivity.class);
        IMApplication.gifRunning = false;
        MessageActivity.this.finish();
    }

    private final RadioGroup.OnCheckedChangeListener emoOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {
            switch (id) {
                case R.id.tab2:
                    if (emojiGridView.getVisibility() != View.VISIBLE) {
                        yayaemojiGridView.setVisibility(View.GONE);
                        emojiGridView.setVisibility(View.VISIBLE);
                    }
                    break;
                case R.id.tab1:
                    if (yayaemojiGridView.getVisibility() != View.VISIBLE) {
                        emojiGridView.setVisibility(View.GONE);
                        yayaemojiGridView.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    private final YayaEmojiGridView.OnYayaEmojiGridViewItemClick yayaOnemojiGridViewItemClick = new YayaEmojiGridView.OnYayaEmojiGridViewItemClick() {
        @Override
        public void onItemClick(int facesPos, int viewIndex) {
            int resId = Emoparser.getInstance(MessageActivity.this).getYayaResIdList()[facesPos];
            logger.d("message_activity#yayaemojiGridView be clicked");

            String content = Emoparser.getInstance(MessageActivity.this).getYayaIdPhraseMap()
                    .get(resId);
            if (content.equals("")) {
                Toast.makeText(MessageActivity.this,
                        getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
                return;
            }

            TextMessageEntity textMessage = TextMessageEntity.buildForSend(content, loginUserEntity, peerEntity);
            imService.getIMMessageManager().sendText(textMessage);
            pushMsg(textMessage);
            scrollToBottom();
        }
    };

    private final EmojiGridView.OnEmojiGridViewItemClick onEmojiGridViewItemClick = new EmojiGridView.OnEmojiGridViewItemClick() {
        @Override
        public void onItemClick(int facesPos, int viewIndex) {
            int deleteId = (++viewIndex) * (SysConstant.MSG_PAGE_SIZE - 1);
            if (deleteId > Emoparser.getInstance(MessageActivity.this).getResIdList().length) {
                deleteId = Emoparser.getInstance(MessageActivity.this).getResIdList().length;
            }
            if (deleteId == facesPos) {
                String msgContent = messageEditView.getText().toString();
                if (msgContent.isEmpty())
                    return;
                if (msgContent.contains("["))
                    msgContent = msgContent.substring(0, msgContent.lastIndexOf("["));
                messageEditView.setText(msgContent);
            } else {
                int resId = Emoparser.getInstance(MessageActivity.this).getResIdList()[facesPos];
                String pharse = Emoparser.getInstance(MessageActivity.this).getIdPhraseMap()
                        .get(resId);
                int startIndex = messageEditView.getSelectionStart();
                Editable edit = messageEditView.getEditableText();
                if (startIndex < 0 || startIndex >= edit.length()) {
                    if (null != pharse) {
                        edit.append(pharse);
                    }
                } else {
                    if (null != pharse) {
                        edit.insert(startIndex, pharse);
                    }
                }
            }
            Editable edtable = messageEditView.getText();
            int position = edtable.length();
            Selection.setSelection(edtable, position);
        }
    };

    private final View.OnFocusChangeListener msgEditOnFocusChangeListener = new android.view.View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (keyboardHeight == 0) {
                    photoPanelView.setVisibility(View.GONE);
                    emojiPanelView.setVisibility(View.GONE);
                } else {
                    MessageActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    if (photoPanelView.getVisibility() == View.GONE) {
                        photoPanelView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            appBarRoot.getGlobalVisibleRect(r);
            // 进入Activity时会布局，第一次调用onGlobalLayout，先记录开始软键盘没有弹出时底部的位置
            if (rootBottom == Integer.MIN_VALUE) {
                rootBottom = r.bottom;
                return;
            }
            // adjustResize，软键盘弹出后高度会变小
            if (r.bottom < rootBottom) {
                //按照键盘高度设置表情框和发送图片按钮框的高度
                keyboardHeight = rootBottom - r.bottom;
                SystemConfigSp.instance().init(MessageActivity.this);
                SystemConfigSp.instance().setIntConfig(SystemConfigSp.SysCfgDimension.KEYBOARDHEIGHT, keyboardHeight);
                LayoutParams params = (LayoutParams) photoPanelView.getLayoutParams();
                params.height = keyboardHeight;
                LayoutParams params1 = (LayoutParams) emojiPanelView.getLayoutParams();
                params1.height = keyboardHeight;
            }
        }
    };

    private class InputMethodSwitchBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.INPUT_METHOD_CHANGED")) {
                String str = Settings.Secure.getString(MessageActivity.this.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                String[] strArr = str.split("\\.");
                if (strArr.length >= 3) {
                    String strCompany = strArr[1];
                    if (!strCompany.equals(currentInputMethod)) {
                        currentInputMethod = strCompany;
                        SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD, currentInputMethod);
                        keyboardHeight = 0;
                        photoPanelView.setVisibility(View.GONE);
                        emojiPanelView.setVisibility(View.GONE);
                        MessageActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//                        inputManager.showSoftInput(messageEditView,0);
                        messageEditView.requestFocus();
                    }
                }
            }
        }
    }
}
