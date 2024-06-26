package com.lsx.bigtalk.config;


public interface SysConstant {
    String AVATAR_APPEND_32  = "_32x32.jpg";
    String AVATAR_APPEND_100 = "_100x100.jpg";
    // 头像120*120的 pic 没有，所以统一100
    String AVATAR_APPEND_120 = "_100x100.jpg";
    String AVATAR_APPEND_200 = "_200x200.jpg";

    // protocol related
    int PROTOCOL_HEADER_LENGTH = 16;
	int PROTOCOL_VERSION       = 6;
	int PROTOCOL_FLAG          = 0;
	char PROTOCOL_ERROR        = '0';
	char PROTOCOL_RESERVED     = '0';
    
    int FILE_SAVE_TYPE_IMAGE = 0X00013;
	int FILE_SAVE_TYPE_AUDIO = 0X00014;
    
    // unit sec
	float MAX_SOUND_RECORD_TIME = 60.0f;
	int MAX_SELECT_IMAGE_COUNT = 6;
    
    int emojiPageSize     = 21;
    int yayaEmojiPageSize = 9;

    
    int IMAGE_PREVIEW_FROM_ALBUM = 3;
    int ALBUM_FOR_DATA = 5;
    int CAMERA_FOR_DATA = 3023;

    String SETTING_GLOBAL = "Global";
    String UPLOAD_IMAGE_INTENT_PARAMS = "com.lsx.bigtalk.upload.image.intent";

    int SERVICE_EVENTBUS_PRIORITY = 10;
    int MESSAGE_EVENTBUS_PRIORITY = 100;
    
    int MSG_PAGE_SIZE = 18;

    String MSG_SERVER_IP = "192.168.1.101";
    int MSG_SERVER_PORT = 8081;
}
