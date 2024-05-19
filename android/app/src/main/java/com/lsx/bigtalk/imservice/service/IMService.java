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
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.lsx.bigtalk.DB.DBInterface;
import com.lsx.bigtalk.DB.entity.MessageEntity;
import com.lsx.bigtalk.DB.sp.ConfigurationSp;
import com.lsx.bigtalk.DB.sp.LoginSp;
import com.lsx.bigtalk.config.SysConstant;
import com.lsx.bigtalk.imservice.event.LoginEvent;
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

import java.util.Objects;

import de.greenrobot.event.EventBus;

/**
 * IMService 负责所有IMManager的初始化与reset
 * 并且Manager的状态的改变 也会影响到IMService的操作
 * 备注: 有些服务应该在LOGIN_OK 之后进行
 * todo IMManager reflect or just like  ctx.getSystemService()
 */
public class IMService extends Service {
    private final Logger logger = Logger.getLogger(IMService.class);

    /**
     * binder
     */
    private final IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        logger.i("IMService onBind");
        return binder;
    }

    //所有的管理类
    private final IMSocketManager socketMgr = IMSocketManager.instance();
    private final IMLoginManager loginMgr = IMLoginManager.instance();
    private final IMContactManager contactMgr = IMContactManager.instance();
    private final IMGroupManager groupMgr = IMGroupManager.instance();
    private final IMMessageManager messageMgr = IMMessageManager.instance();
    private final IMSessionManager sessionMgr = IMSessionManager.instance();
    private final IMReconnectManager reconnectMgr = IMReconnectManager.instance();
    private final IMUnreadMsgManager unReadMsgMgr = IMUnreadMsgManager.instance();
    private final IMNotificationManager notificationMgr = IMNotificationManager.instance();
    private final IMHeartBeatManager heartBeatManager = IMHeartBeatManager.instance();

    private ConfigurationSp configSp;
    private final LoginSp loginSp = LoginSp.instance();
    private final DBInterface dbInterface = DBInterface.instance();

    @Override
    public void onCreate() {
        logger.i("IMService onCreate");
        super.onCreate();
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
        handleLoginout();
        // DB的资源的释放
        dbInterface.close();

        IMNotificationManager.instance().cancelAllNotifications();
        super.onDestroy();
    }

    /**
     * 收到消息需要上层的activity判断 {MessageActicity onEvent(PriorityEvent event)}，这个地方是特殊分支
     */
    public void onEvent(PriorityEvent event) {
        if (Objects.requireNonNull(event.event) == PriorityEvent.Event.MSG_RECEIVED_MESSAGE) {
            MessageEntity entity = (MessageEntity) event.object;
            /**非当前的会话*/
            logger.d("messageactivity#not this session msg -> id:%s", entity.getFromId());
            messageMgr.ackReceiveMsg(entity);
            unReadMsgMgr.add(entity);
        }
    }

    // EventBus 事件驱动
    public void onEvent(LoginEvent event) {
        switch (event) {
            case LOGIN_OK:
                onNormalLoginOk();
                break;
            case LOCAL_LOGIN_SUCCESS:
                onLocalLoginOk();
                break;
            case LOCAL_LOGIN_MSG_SERVICE:
                onLocalNetOk();
                break;
            case LOGIN_OUT:
                handleLoginout();
                break;
        }
    }

    // 负责初始化 每个manager
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("IMService onStartCommand");
        //应用开启初始化 下面这几个怎么释放 todo
        Context ctx = getApplicationContext();
        loginSp.init(ctx);
        // 放在这里还有些问题 todo
        socketMgr.onStartIMManager(ctx);
        loginMgr.onStartIMManager(ctx);
        contactMgr.onStartIMManager(ctx);
        messageMgr.onStartIMManager(ctx);
        groupMgr.onStartIMManager(ctx);
        sessionMgr.onStartIMManager(ctx);
        unReadMsgMgr.onStartIMManager(ctx);
        notificationMgr.onStartIMManager(ctx);
        reconnectMgr.onStartIMManager(ctx);
        heartBeatManager.onStartIMManager(ctx);

        ImageLoaderUtil.initImageLoaderConfig(ctx);
        return START_STICKY;
    }


    /**
     * 用户输入登陆流程
     * userName/pwd -> reqMessage ->connect -> loginMessage ->loginSuccess
     */
    private void onNormalLoginOk() {
        logger.d("imservice#onLogin Successful");
        //初始化其他manager todo 这个地方注意上下文的清除
        Context ctx = getApplicationContext();
        int loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onNormalLoginOk();
        sessionMgr.onNormalLoginOk();
        groupMgr.onNormalLoginOk();
        unReadMsgMgr.onNormalLoginOk();

        reconnectMgr.onNormalLoginOk();
        //依赖的状态比较特殊
        messageMgr.onLoginSuccess();
        notificationMgr.onLoginSuccess();
        heartBeatManager.onloginNetSuccess();
        // 这个时候loginManage中的localLogin 被置为true
    }


    /**
     * 自动登陆/离线登陆成功
     * autoLogin -> DB(loginInfo,loginId...) -> loginSucsess
     */
    private void onLocalLoginOk() {
        Context ctx = getApplicationContext();
        int loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onLocalLoginOk();
        groupMgr.onLocalLoginOk();
        sessionMgr.onLocalLoginOk();
        reconnectMgr.onLocalLoginOk();
        notificationMgr.onLoginSuccess();
        messageMgr.onLoginSuccess();
    }

    /**
     * 1. 从本机加载成功之后，请求MessageService建立链接成功(loginMessageSuccess)
     * 2. 重练成功之后
     */
    private void onLocalNetOk() {
        /**为了防止逗比直接把loginId与userName的对应直接改了,重刷一遍*/
        Context ctx = getApplicationContext();
        int loginId = loginMgr.getLoginId();
        configSp = ConfigurationSp.instance(ctx, loginId);
        dbInterface.initDbHelp(ctx, loginId);

        contactMgr.onLocalNetOk();
        groupMgr.onLocalNetOk();
        sessionMgr.onLocalNetOk();
        unReadMsgMgr.onLocalNetOk();
        reconnectMgr.onLocalNetOk();
        heartBeatManager.onloginNetSuccess();
    }

    private void handleLoginout() {
        logger.d("imservice#handleLoginout");

        // login需要监听socket的变化,在这个地方不能释放，设计上的不合理?
        socketMgr.reset();
        loginMgr.reset();
        contactMgr.reset();
        messageMgr.reset();
        groupMgr.reset();
        sessionMgr.reset();
        unReadMsgMgr.reset();
        notificationMgr.reset();
        reconnectMgr.reset();
        heartBeatManager.reset();
        configSp = null;
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logger.d("imservice#onTaskRemoved");
        // super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    /**
     * 启动前台服务
     */
    private void startForeground() {
        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("kim.hsl", "IMService");
        } else {
            channelId = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

    /**
     * 创建通知通道
     *
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    /**
     * -----------------get/set 的实体定义---------------------
     */
    public IMLoginManager getLoginManager() {
        return loginMgr;
    }

    public IMContactManager getContactManager() {
        return contactMgr;
    }

    public IMMessageManager getMessageManager() {
        return messageMgr;
    }


    public IMGroupManager getGroupManager() {
        return groupMgr;
    }

    public IMSessionManager getSessionManager() {
        return sessionMgr;
    }

    public IMReconnectManager getReconnectManager() {
        return reconnectMgr;
    }


    public IMUnreadMsgManager getUnReadMsgManager() {
        return unReadMsgMgr;
    }

    public IMNotificationManager getNotificationManager() {
        return notificationMgr;
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
