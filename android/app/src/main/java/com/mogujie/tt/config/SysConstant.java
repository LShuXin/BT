package com.mogujie.tt.config;

/**
 * 系统的常量类
 */
public interface SysConstant {

    /**头像尺寸大小定义*/
    String AVATAR_APPEND_32 ="_32x32.jpg";
    String AVATAR_APPEND_100 ="_100x100.jpg";
    String AVATAR_APPEND_120 ="_100x100.jpg";//头像120*120的pic 没有 所以统一100
    String AVATAR_APPEND_200="_200x200.jpg";

    /**协议头相关 start*/
    int PROTOCOL_HEADER_LENGTH = 16;// 默认消息头的长度
	int PROTOCOL_VERSION = 6;
	int PROTOCOL_FLAG = 0;
	char PROTOCOL_ERROR = '0';
	char PROTOCOL_RESERVED = '0';


    // 读取磁盘上文件， 分支判断其类型
    int FILE_SAVE_TYPE_IMAGE = 0X00013;
	int FILE_SAVE_TYPE_AUDIO = 0X00014;


	float MAX_SOUND_RECORD_TIME = 60.0f;// 单位秒
	int MAX_SELECT_IMAGE_COUNT = 6;


    /**表情使用*/
    int pageSize = 21;
    int yayaPageSize = 8;


    // 好像设定了，但是好像没有用
    int ALBUM_PREVIEW_BACK = 3;
    // resultCode 返回值
    int ALBUM_BACK_DATA = 5;
    int CAMERA_WITH_DATA = 3023;


    /**
     *1. 配置的全局key
     * */
    String SETTING_GLOBAL = "Global";
    String UPLOAD_IMAGE_INTENT_PARAMS = "com.mogujie.tt.upload.image.intent";
    /**
     * event 优先级
     * */
    int SERVICE_EVENTBUS_PRIORITY = 10;
    int MESSAGE_EVENTBUS_PRIORITY = 100;

    //message 每次拉取的条数
    int MSG_CNT_PER_PAGE = 18;
}
