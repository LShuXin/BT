package com.lsx.bigtalk.service.event;

import com.lsx.bigtalk.storage.db.entity.MessageEntity;

import java.util.List;


public class HistoryMsgRefreshEvent {
   public int pullTimes;
   public int lastMsgId;
   public int count;
   public List<MessageEntity> listMsg;
   public int peerId;
   public int peerType;
   public String sessionKey;

   public HistoryMsgRefreshEvent() {
      
   }
}
