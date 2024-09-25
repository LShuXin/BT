package com.lsx.bigtalk.service.callback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;

import android.os.Handler;

import com.lsx.bigtalk.logs.Logger;


public class ListenerQueue {
    private static final ListenerQueue listenerQueue = new ListenerQueue();
    private final Logger logger = Logger.getLogger(ListenerQueue.class);
    public static ListenerQueue getInstance() {
        return listenerQueue;
    }

    private volatile boolean stopFlag = false;
    private volatile boolean hasTask = false;
    
    private final Map<Integer, PacketListener> callBackQueue = new ConcurrentHashMap<>();
    private final Handler timerHandler = new Handler();
    
    public void onStart() {
        logger.d("ListenerQueue#onStart");
        stopFlag = false;
        startTimer();
    }
    public void onDestroy() {
        logger.d("ListenerQueue#onDestroy");
        callBackQueue.clear();
        stopTimer();
    }
    
    private void startTimer() {
        if (!stopFlag && !hasTask) {
            hasTask = true;
            timerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timerImpl();
                    hasTask = false;
                    startTimer();
                }
            }, 5 * 1000);
        }
    }

    private void stopTimer() {
        stopFlag = true;
    }

    private void timerImpl() {
        long currentRealtime = System.currentTimeMillis();
        for (Entry<Integer, PacketListener> entry : callBackQueue.entrySet()) {
            PacketListener packetlistener = entry.getValue();
            Integer seqNo = entry.getKey();
            long timeRange = currentRealtime - packetlistener.getCreateTime();

            try {
                if (timeRange >= packetlistener.getTimeout()) {
                    logger.d("ListenerQueue#find timeout msg");
                    PacketListener listener = pop(seqNo);
                    if (null != listener) {
                        listener.onTimeout();
                    }
                }
            } catch (Exception e) {
                logger.d("ListenerQueue#timerImpl crush, exception is %s", e.getCause());
            }
        }
    }

    public void push(int seqNo, PacketListener packetlistener) {
        if (0 >= seqNo || null == packetlistener) {
            logger.d("ListenerQueue#push error, cause by Illegal params");
            return;
        }
        callBackQueue.put(seqNo, packetlistener);
    }
    
    public PacketListener pop(int seqNo) {
        synchronized (ListenerQueue.this) {
            if (callBackQueue.containsKey(seqNo)) {
                return callBackQueue.remove(seqNo);
            }
            return null;
        }
    }
}
