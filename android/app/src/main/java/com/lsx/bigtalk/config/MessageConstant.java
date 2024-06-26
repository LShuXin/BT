package com.lsx.bigtalk.config;


public interface MessageConstant {
    int MSG_SENDING = 1;
    int MSG_FAILURE = 2;
    int MSG_SUCCESS = 3;
    
    int IMAGE_UNLOAD         = 1;
    int IMAGE_LOADING        = 2;
    int IMAGE_LOADED_SUCCESS = 3;
    int IMAGE_LOADED_FAILURE = 4;
    
    int AUDIO_UNREAD = 1;
    int AUDIO_READ   = 2;

    /**图片消息的前后常量*/
    String IMAGE_MSG_PREFIX = "&$#@~^@[{:";
    String IMAGE_MSG_SUFFIX = ":}]&$~@#@";

}
