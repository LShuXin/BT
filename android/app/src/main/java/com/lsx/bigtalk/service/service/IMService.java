package com.lsx.bigtalk.service.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import androidx.core.app.NotificationCompat;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.R;
import com.lsx.bigtalk.AppConstant;
import com.lsx.bigtalk.service.event.LoginEvent;
import com.lsx.bigtalk.storage.db.BTDB;
import com.lsx.bigtalk.storage.db.entity.MessageEntity;
import com.lsx.bigtalk.service.event.PriorityEvent;
import com.lsx.bigtalk.service.manager.IMContactManager;
import com.lsx.bigtalk.service.manager.IMGroupManager;
import com.lsx.bigtalk.service.manager.IMHeartBeatManager;
import com.lsx.bigtalk.service.manager.IMLoginManager;
import com.lsx.bigtalk.service.manager.IMMessageManager;
import com.lsx.bigtalk.service.manager.IMNotificationManager;
import com.lsx.bigtalk.service.manager.IMReconnectManager;
import com.lsx.bigtalk.service.manager.IMSessionManager;
import com.lsx.bigtalk.service.manager.IMSocketManager;
import com.lsx.bigtalk.service.manager.IMUnreadMsgManager;
import com.lsx.bigtalk.storage.sp.BTSp;
import com.lsx.bigtalk.utils.ImageLoaderUtil;
import com.lsx.bigtalk.logs.Logger;


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
    private final BTDB btDb = BTDB.instance();

    @Override
    public void onCreate() {
        super.onCreate();
        logger.i("IMService#onCreate");
        EventBus.getDefault().register(this, AppConstant.SysConstant.SERVICE_EVENTBUS_PRIORITY);
        startForeground();
    }

    @Override
    public void onDestroy() {
        logger.i("IMService onDestroy");
        EventBus.getDefault().unregister(this);
        handleLogout();
        btDb.close();
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

    public void onEvent(LoginEvent event) {
        switch (event) {
            case NORMAL_LOGIN_SUCCESS:
            {
                onNormalLoginOk();
                break;
            }
            case LOCAL_LOGIN_SUCCESS:
            {
                onLocalLoginOk();
                break;
            }
            case REMOTE_LOGIN_SUCCESS:
            {
                onRemoteLoginOk();
                break;
            }
            case LOGIN_OUT:
            {
                handleLogout();
                break;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("IMService#onStartCommand");
        Context ctx = getApplicationContext();
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

    private void onNormalLoginOk() {
        logger.d("IMService#onNormalLoginOk");
        int loginId = imLoginManager.getLoginId();
        BTSp.getInstance().setUserIdentifier(String.valueOf(loginId));
        btDb.initDbHelp(getApplicationContext(), loginId);
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
        int loginId = imLoginManager.getLoginId();
        BTSp.getInstance().setUserIdentifier(String.valueOf(loginId));
        btDb.initDbHelp(getApplicationContext(), loginId);
        imContactManager.onLocalLoginOk();
        imGroupManager.onLocalLoginOk();
        imSessionManager.onLocalLoginOk();
        imReconnectManager.onLocalLoginOk();
        imNotificationManager.onLoginSuccess();
        imMessageManager.onLoginSuccess();
    }

    private void onRemoteLoginOk() {
        int loginId = imLoginManager.getLoginId();
        BTSp.getInstance().setUserIdentifier(String.valueOf(loginId));
        btDb.initDbHelp(getApplicationContext(), loginId);
        imContactManager.onRemoteLoginOk();
        imGroupManager.onRemoteLoginOk();
        imSessionManager.onRemoteLoginOk();
        imUnReadMsgManager.onRemoteLoginOk();
        imReconnectManager.onRemoteLoginOk();
        imHeartBeatManager.onRemoteLoginOk();
    }

    private void handleLogout() {
        logger.d("IMService#handleLogout");
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

    public BTDB getbtDb() {
        return btDb;
    }


}
