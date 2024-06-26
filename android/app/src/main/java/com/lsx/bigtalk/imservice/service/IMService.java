package com.lsx.bigtalk.imservice.service;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.lsx.bigtalk.DB.DBInterface;
import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.sp.ConfigurationSp;
import com.lsx.bigtalk.DB.sp.LoginSp;
import com.lsx.bigtalk.R;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.event.LoginStatus;
import com.lsx.bigtalk.imservice.event.PriorityEvent;
import com.lsx.bigtalk.imservice.manager.IMContactManager;
import com.lsx.bigtalk.imservice.manager.IMGroupManager;
import com.lsx.bigtalk.imservice.manager.IMHeartBeatManager;
import com.lsx.bigtalk.imservice.manager.IMLoginManager;
import com.lsx.bigtalk.imservice.manager.IMMessageManager;
import com.lsx.bigtalk.imservice.manager.IMNotificationManager;
import com.lsx.bigtalk.imservice.manager.IMReconnectManager;
import com.lsx.bigtalk.imservice.manager.IMSessionManager;
import com.lsx.bigtalk.imservice.manager.IMSocketManager;
import com.lsx.bigtalk.imservice.manager.IMUnreadMsgManager;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.lsx.bigtalk.utils.Logger;

import de.greenrobot.event.EventBus;

/**
 * IMService 负责所有IMManager的初始化与reset
 * 并且Manager的状态的改变 也会影响到IMService的操作
 * 备注: 有些服务应该在LOGIN_OK 之后进行
 * todo IMManager reflect or just like  ctx.getSystemService()
 */
public class IMService extends Service {
    private final Logger logger = Logger.getLogger(IMService.class);
    private final IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        logger.i("IMService#onBind");
        return binder;
    }

    //所有的管理类
    private final IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private final IMLoginManager imLoginManager = IMLoginManager.getInstance();
    private final IMContactManager imContactManager = IMContactManager.getInstance();
    private final IMGroupManager imGroupManager = IMGroupManager.getInstance();
    private final IMMessageManager imMessageManager = IMMessageManager.getInstance();
    private final IMSessionManager imSessionManager = IMSessionManager.getInstance();
    private final IMReconnectManager imReconnectManager = IMReconnectManager.getInstance();
    private final IMUnreadMsgManager imUnReadMsgManager = IMUnreadMsgManager.getInstance();
    private final IMNotificationManager imNotificationManager = IMNotificationManager.getInstance();
    private final IMHeartBeatManager imHeartBeatManager = IMHeartBeatManager.getInstance();

    private ConfigurationSp configSp;
    private final LoginSp loginSp = LoginSp.instance();
    private final DBInterface dbInterface = DBInterface.instance();

    @Override
    public void onCreate() {
        super.onCreate();
        logger.i("IMService#onCreate");
        EventBus.getDefault().register(this, SysConstant.SERVICE_EVENTBUS_PRIORITY);
        // make the service foreground, so stop "360 yi jian qingli"(a clean
        // tool) to stop our app
        // todo eric study wechat's mechanism, use a better solution
        //startForeground((int) System.currentTimeMillis(), new Notification());
        startForeground();
    }

    @Override
    public void onDestroy() {
        logger.i("IMService onDestroy");
        // todo 在onCreate中使用startForeground
        // 在这个地方是否执行 stopForeground呐
        EventBus.getDefault().unregister(this);
        handleLogout();
        dbInterface.close();
        imNotificationManager.cancelAllNotifications();
        super.onDestroy();
    }

    /**
     * 收到消息需要上层的activity判断 {MessageActicity onEvent(PriorityEvent event)}，这个地方是特殊分支
     */
    public void onEvent(PriorityEvent event) {
        if (event.event == PriorityEvent.Event.MSG_RECEIVED_MESSAGE) {
            MessageEntity entity = (MessageEntity) event.object;
            imMessageManager.ackReceiveMsg(entity);
            imUnReadMsgManager.add(entity);
        }
    }

    public void onEvent(LoginStatus event) {
        switch (event) {
            case LOGIN_OK:
                onNormalLoginOk();
                break;
            case LOCAL_LOGIN_SUCCESS:
                onLocalLoginOk();
                break;
            case LOCAL_LOGIN_MSG_SERVICE:
                onRemoteLoginOk();
                break;
            case LOGIN_OUT:
                handleLogout();
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("IMService#onStartCommand");
        Context ctx = getApplicationContext();
        loginSp.init(ctx);
        imSocketManager.onStartIMManager(ctx);
        imLoginManager.onStartIMManager(ctx);
        imContactManager.onStartIMManager(ctx);
        imMessageManager.onStartIMManager(ctx);
        imGroupManager.onStartIMManager(ctx);
        imSessionManager.onStartIMManager(ctx);
        imUnReadMsgManager.onStartIMManager(ctx);
        imNotificationManager.onStartIMManager(ctx);
        imReconnectManager.onStartIMManager(ctx);
        imHeartBeatManager.onStartIMManager(ctx);

        ImageLoaderUtil.initImageLoaderConfig(ctx);
        return START_STICKY;
    }


    /**
     * 用户输入登陆流程
     * userName/pwd -> reqMessage ->connect -> loginMessage ->loginSuccess
     */
    private void onNormalLoginOk() {
        logger.d("IMService#onNormalLoginOk");
        Context ctx = getApplicationContext();
        int loginId = imLoginManager.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        imContactManager.onNormalLoginOk();
        imSessionManager.onNormalLoginOk();
        imGroupManager.onNormalLoginOk();
        imUnReadMsgManager.onNormalLoginOk();
        imReconnectManager.onNormalLoginOk();

        imMessageManager.onLoginSuccess();
        imNotificationManager.onLoginSuccess();
        imHeartBeatManager.onRemoteLoginOk();
    }

    private void onLocalLoginOk() {
        Context ctx = getApplicationContext();
        int loginId = imLoginManager.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        imContactManager.onLocalLoginOk();
        imGroupManager.onLocalLoginOk();
        imSessionManager.onLocalLoginOk();
        imReconnectManager.onLocalLoginOk();
        imNotificationManager.onLoginSuccess();
        imMessageManager.onLoginSuccess();
    }

    private void onRemoteLoginOk() {
        Context ctx = getApplicationContext();
        int loginId = imLoginManager.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        imContactManager.onRemoteLoginOk();
        imGroupManager.onRemoteLoginOk();
        imSessionManager.onLocalNetOk();
        imUnReadMsgManager.onRemoteLoginOk();
        imReconnectManager.onRemoteLoginOk();
        imHeartBeatManager.onRemoteLoginOk();
    }

    private void handleLogout() {
        logger.d("IMService#handleLogout");
        // login需要监听socket的变化,在这个地方不能释放，设计上的不合理?
        imSocketManager.reset();
        imLoginManager.reset();
        imContactManager.reset();
        imMessageManager.reset();
        imGroupManager.reset();
        imSessionManager.reset();
        imUnReadMsgManager.reset();
        imNotificationManager.reset();
        imReconnectManager.reset();
        imHeartBeatManager.reset();
        configSp = null;
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logger.d("IMService#onTaskRemoved");
        this.stopSelf();
    }
    
    private void startForeground() {
        String channelId = createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }
    
    private String createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("kim.hsl",
                "IMService", NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(channel);
        return "kim.hsl";
    }
    
    public IMLoginManager getIMLoginManager() {
        return imLoginManager;
    }

    public IMContactManager getIMContactManager() {
        return imContactManager;
    }

    public IMMessageManager getIMMessageManager() {
        return imMessageManager;
    }
    
    public IMGroupManager getIMGroupManager() {
        return imGroupManager;
    }

    public IMSessionManager getIMSessionManager() {
        return imSessionManager;
    }

    public IMReconnectManager getIMReconnectManager() {
        return imReconnectManager;
    }
    
    public IMUnreadMsgManager getIMUnReadMsgManager() {
        return imUnReadMsgManager;
    }

    public IMNotificationManager getIMNotificationManager() {
        return imNotificationManager;
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }

    public ConfigurationSp getConfigSp() {
        return configSp;
    }

    public LoginSp getLoginSp() {
        return loginSp;
    }

}
