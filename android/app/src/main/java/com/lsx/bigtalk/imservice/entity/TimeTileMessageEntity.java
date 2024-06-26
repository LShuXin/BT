package com.lsx.bigtalk.imservice.entity;


public class TimeTileMessageEntity {
    private int time;

    public TimeTileMessageEntity(int mTime) {
        time = mTime;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }
}
