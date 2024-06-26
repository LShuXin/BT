package com.lsx.bigtalk.imservice.manager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.imservice.event.LoginStatus;
import com.lsx.bigtalk.imservice.event.ReconnectEvent;
import com.lsx.bigtalk.imservice.event.SocketEvent;
import com.lsx.bigtalk.utils.Logger;
import com.lsx.bigtalk.utils.NetworkUtil;


public class IMReconnectManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMReconnectManager.class);
    private volatile ReconnectEvent status = ReconnectEvent.NONE;
    private final int INIT_RECONNECT_INTERVAL_SECONDS = 3;
    private int reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
    private final int HANDLER_CHECK_NETWORK = 1;
    private volatile boolean isAlarmTrigger = false;

    @SuppressLint("StaticFieldLeak")
    private static IMReconnectManager instance;

    public static synchronized IMReconnectManager getInstance() {
        if (null == instance) {
            instance = new IMReconnectManager();
        }
        return instance;
    }

    @Override
    public void doOnStart() {

    }

    public void onNormalLoginOk() {
        onLocalLoginOk();
        status = ReconnectEvent.SUCCESS;
    }

    public void onLocalLoginOk() {
        logger.d("IMReconnectManager#onLocalLoginOk");

        if (!EventBus.getDefault().isRegistered(instance)) {
            EventBus.getDefault().register(instance);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECONNECT);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ctx.registerReceiver(imReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    public void onRemoteLoginOk() {
        logger.d("IMReconnectManager#onRemoteLoginOk");
        status = ReconnectEvent.SUCCESS;
    }

    @Override
    public void reset() {
        try {
            EventBus.getDefault().unregister(instance);
            ctx.unregisterReceiver(imReceiver);
            status = ReconnectEvent.NONE;
            isAlarmTrigger = false;
        } catch (Exception e) {
            logger.e("%s", e.getCause());
        }
    }

    public void onEventMainThread(SocketEvent socketEvent) {
        switch (socketEvent) {
            case MSG_SERVER_DISCONNECTED:
            case CONNECT_MSG_SERVER_FAILED: {
                tryReconnect();
            }
            break;
        }
    }

    public void onEventMainThread(LoginStatus event) {
        switch (event) {
            case LOGIN_INNER_FAILED:
                tryReconnect();
                break;

            case LOCAL_LOGIN_MSG_SERVICE:
                resetReconnectTimeInterval();
                break;
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANDLER_CHECK_NETWORK) {
                if (!NetworkUtil.isNetWorkAvailable((Application) ctx)) {
                    logger.w("reconnect#handleMessage#网络依旧不可用");
                    EventBus.getDefault().post(ReconnectEvent.DISABLE);
                }
            }
        }
    };

    private boolean isReconnecting() {
        SocketEvent socketEvent = IMSocketManager.getInstance().getSocketStatus();
        LoginStatus LoginStatus = IMLoginManager.getInstance().getLoginStatus();

        return socketEvent.equals(SocketEvent.CONNECTING_MSG_SERVER)
                || LoginStatus.equals(com.lsx.bigtalk.imservice.event.LoginStatus.LOGINING);
    }

    private void tryReconnect() {
        if (!NetworkUtil.isNetWorkAvailable((Application) ctx.getApplicationContext())) {
            logger.w("IMReconnectManager#tryReconnect#network is unavailable");
            status = ReconnectEvent.DISABLE;
            handler.sendEmptyMessageDelayed(HANDLER_CHECK_NETWORK, 2000);
            return;
        }

        synchronized (IMReconnectManager.this) {
            if (NetworkUtil.isNetWorkAvailable((Application) ctx.getApplicationContext())) {
                if (
                    status == ReconnectEvent.NONE
                    ||
                    !IMLoginManager.getInstance().getIsEverLoggedIn()
                    || 
                    IMLoginManager.getInstance().getIsKickedOut()
                    || 
                    IMSocketManager.getInstance().isSocketConnected()
                ) {
                    logger.i("IMReconnectManager#tryReconnect#no more reconnect needed");
                    return;
                }
                if (isReconnecting()) {
                    logger.d("IMReconnectManager#tryReconnect#reconnecting...");
                    incrementReconnectInterval();
                    scheduleReconnect(reconnectInterval);
                    logger.d("IMReconnectManager#tryReconnect#next reconnect time interval:%d", reconnectInterval);
                    return;
                }
                
                // make sure the socket is disconnected
                IMSocketManager.getInstance().disconnectFromMsgServer();

                if (isAlarmTrigger) {
                    isAlarmTrigger = false;
                    logger.d("MReconnectManager#tryReconnect#Alarm trigger...");
                    doReconnectMsgServer();
                } else {
                    logger.d("MReconnectManager#tryReconnect#not Alarm trigger...");
                    IMSocketManager.getInstance().reconnectToMsgServer();
                }
            } else {
                logger.d("MReconnectManager#tryReconnect#post a notification");
                status = ReconnectEvent.DISABLE;
                EventBus.getDefault().post(ReconnectEvent.DISABLE);
            }
        }
    }
    
    private void scheduleReconnect(int seconds) {
        logger.d("MReconnectManager#scheduleReconnect after %d seconds", seconds);
        Intent intent = new Intent(ACTION_RECONNECT);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (pi == null) {
            logger.e("reconnect#pi is null");
            return;
        }
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds
                * 1000L, pi);
    }
    
    private void incrementReconnectInterval() {
        int MAX_RECONNECT_INTERVAL_SECONDS = 60;
        if (reconnectInterval >= MAX_RECONNECT_INTERVAL_SECONDS) {
            reconnectInterval = MAX_RECONNECT_INTERVAL_SECONDS;
        } else {
            reconnectInterval = reconnectInterval * 2;
        }
    }
    
    private void resetReconnectTimeInterval() {
        logger.d("MReconnectManager#resetReconnectTimeInterval");
        reconnectInterval = INIT_RECONNECT_INTERVAL_SECONDS;
    }


    /**
     * --------------------boradcast-广播相关-----------------------------
     */
    private final String ACTION_RECONNECT = "com.lsx.bigtalk.imlib.action.reconnect";
    private final BroadcastReceiver imReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.d("reconnect#im#receive action:%s", action);
            onAction(action, intent);
        }
    };

    /**
     * 【重要】这个地方作为重连判断的唯一标准
     * 飞行模式: 触发
     * 切换网络状态： 触发
     * 没有网络 ： 触发
     */
    public void onAction(String action, Intent ignoredIntent) {
        logger.d("reconnect#onAction action:%s", action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            logger.d("reconnect#onAction#网络状态发生变化!!");
            tryReconnect();
        } else if (action.equals(ACTION_RECONNECT)) {
            isAlarmTrigger = true;
            tryReconnect();
        }
    }
    
    private void doReconnectMsgServer() {
        logger.d("IMReconnectManager#doReconnectMsgServer");
        PowerManager powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bigtalk:reconnectWakeLock");
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        try {
            if (!IMLoginManager.getInstance().getIsEverLoggedIn() || IMLoginManager.getInstance().getIsKickedOut()) {
                logger.d("IMReconnectManager#doReconnectMsgServer#never loggedIn or kickedOut, stop reconnecting");
                return;
            }
            int RE_LOGIN_TIME_INTERVAL = 24;
            if (reconnectInterval > RE_LOGIN_TIME_INTERVAL) {
                logger.d("IMReconnectManager#doReconnectMsgServer#relogin...");
                IMLoginManager.getInstance().reLogin();
            } else {
                logger.d("IMReconnectManager#doReconnectMsgServer#connecting...");
                IMSocketManager.getInstance().reconnectToMsgServer();
            }
        } finally {
            wakeLock.release();
        }
    }
}


