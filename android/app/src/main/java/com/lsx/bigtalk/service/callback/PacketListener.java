package com.lsx.bigtalk.service.callback;


public abstract class PacketListener implements IMListener {
    private long createTime;
    private long timeout;
    
    public PacketListener(long timeout) {
        this.timeout = timeout;
        createTime = System.currentTimeMillis();
    }

    public PacketListener() {
        this.timeout = 8 * 1000;
        createTime = System.currentTimeMillis();
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeOut(long timeout) {
        this.timeout = timeout;
    }

    public abstract void onSuccess(Object response);

    public abstract void onFailed();

    public abstract void onTimeout();
}
