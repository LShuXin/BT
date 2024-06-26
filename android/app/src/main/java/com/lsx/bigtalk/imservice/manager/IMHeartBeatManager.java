package com.lsx.bigtalk.imservice.manager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.RequiresApi;

import com.lsx.bigtalk.imservice.callback.Packetlistener;
import com.lsx.bigtalk.protobuf.IMBaseDefine;
import com.lsx.bigtalk.protobuf.IMOther;
import com.lsx.bigtalk.utils.Logger;


// send heart beat packet every 4 minutes
// server will disconnect socket connection if there is no package in 5 minutes
public class IMHeartBeatManager extends IMManager {
    private final Logger logger = Logger.getLogger(IMHeartBeatManager.class);
    private final String ACTION_SENDING_HEARTBEAT = "com.lsx.bigtalk.imservice.manager.imheartbeatmanager";
    private PendingIntent pendingIntent;
    @SuppressLint("StaticFieldLeak")
    private static IMHeartBeatManager instance;

    public static synchronized IMHeartBeatManager getInstance() {
        if (null == instance) {
            instance = new IMHeartBeatManager();
        }

        return instance;
    }

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        logger.d("IMHeartBeatManager#reset");
        try {
            ctx.unregisterReceiver(imReceiver);
            cancelHeartbeatTimer();
        } catch (Exception e) {
            logger.e("IMHeartBeatManager#reset error:%s", e.getCause());
        }
    }

    @SuppressLint("InlinedApi")
    public void onRemoteLoginOk() {
        logger.e("IMHeartBeatManager#onRemoteLoginOk");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        ctx.registerReceiver(imReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        int HEARTBEAT_INTERVAL = 4 * 60 * 1000;
        scheduleHeartbeat(HEARTBEAT_INTERVAL);
    }

    public void handleMsgServerDisconnected() {
        logger.w("IMHeartBeatManager#onChannelDisconn");
        cancelHeartbeatTimer();
    }

    private void cancelHeartbeatTimer() {
        logger.w("IMHeartBeatManager#cancelHeartbeatTimer");
        if (pendingIntent == null) {
            return;
        }
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    private void scheduleHeartbeat(int seconds) {
        logger.d("IMHeartBeatManager#scheduleHeartbeat");
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_SENDING_HEARTBEAT);
            pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            if (pendingIntent == null) {
                return;
            }
        }

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pendingIntent);
    }

    private final BroadcastReceiver imReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.w("IMHeartBeatManager#imReceiver#receive action:%s", action);
            if (action != null && action.equals(ACTION_SENDING_HEARTBEAT)) {
                sendHeartBeatPacket();
            }
        }
    };

    public void sendHeartBeatPacket() {
        logger.d("IMHeartBeatManager#sendHeartBeatPacket");
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bigtalk:heartBeat_wakelock");
        wl.acquire(10 * 60 * 1000L);
        try {
            final long timeOut = 5 * 1000;
            IMOther.IMHeartBeat imHeartBeat = IMOther.IMHeartBeat.newBuilder()
                    .build();
            int sid = IMBaseDefine.ServiceID.SID_OTHER_VALUE;
            int cid = IMBaseDefine.OtherCmdID.CID_OTHER_HEARTBEAT_VALUE;
            IMSocketManager.getInstance().sendRequest(imHeartBeat, sid, cid, new Packetlistener(timeOut) {
                @Override
                public void onSuccess(Object response) {
                    logger.d("IMHeartBeatManager#send heart beat packet success");
                }

                @Override
                public void onFaild() {
                    logger.w("IMHeartBeatManager#send heart beat packet failed");
                    IMSocketManager.getInstance().handleMsgServerDisconnected();
                }

                @Override
                public void onTimeout() {
                    logger.w("IMHeartBeatManager#send heart beat packet timeout");
                    IMSocketManager.getInstance().handleMsgServerDisconnected();
                }
            });
        } finally {
            wl.release();
        }
    }
}
