package com.lsx.bigtalk.service.event;

import com.lsx.bigtalk.ui.adapter.album.ImageItem;

import java.util.List;


public class ImageSelectEvent {
    private List<ImageItem> list;
    public ImageSelectEvent(List<ImageItem> list){
        this.list = list;
    }

    public void setList(List<ImageItem> list) {
        this.list = list;
    }
    
    public List<ImageItem> getList() {
        return list;
    }
}
