package com.lsx.bigtalk.config;


public interface DBConstant {
    int SEX_MAILE  = 1;
    int SEX_FEMALE = 2;

    int MSG_TYPE_SINGLE_TEXT  = 0x01;
    int MSG_TYPE_SINGLE_AUDIO = 0x02;
    int MSG_TYPE_GROUP_TEXT   = 0x11;
    int MSG_TYPE_GROUP_AUDIO  = 0x12;

    /**
     *  saved in db, sync with server side
     * 1. plain text
     * 2. plain image url
     * 3. audio
     * 4. image + text
     * 5. gif
     * */
    int SHOW_TYPE_PLAIN_TEXT = 1;
    int SHOW_TYPE_IMAGE      = 2;
    int SHOW_TYPE_AUDIO      = 3;
    int SHOW_TYPE_RICH_TEXT  = 4;
    int SHOW_TYPE_GIF        = 5;

    String DISPLAY_FOR_IMAGE       = "[图片]";
    String DISPLAY_FOR_RICH_TEXT   = "[图文消息]";
    String DISPLAY_FOR_AUDIO       = "[语音]";
    String DISPLAY_FOR_ERROR       = "[未知消息]";

    int SESSION_TYPE_SINGLE = 1;
    int SESSION_TYPE_GROUP  = 2;

   int GROUP_TYPE_NORMAL = 1;
   int GROUP_TYPE_TEMP   = 2;

    int GROUP_STATUS_ONLINE = 0;
    int GROUP_STATUS_SHIELD = 1;

    int GROUP_MODIFY_TYPE_ADD = 0;
    int GROUP_MODIFY_TYPE_DEL = 1;

    int DEPT_STATUS_OK      = 0;
    int DEPT_STATUS_DELETED = 1;

}
