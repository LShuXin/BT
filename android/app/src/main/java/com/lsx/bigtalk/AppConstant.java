package com.lsx.bigtalk;

public class AppConstant {
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

    public interface IntentConstant {
        String KEY_AVATAR_URL = "key_avatar_url";
        String KEY_IS_IMAGE_CONTACT_AVATAR = "is_image_contact_avatar";
        String KEY_NOT_AUTO_LOGIN = "login_not_auto";
        String KEY_LOCATE_DEPARTMENT = "key_locate_department";
        String KEY_SESSION_KEY = "chat_session_key";
        String KEY_PEER_ID = "key_peer_id";
        String PREVIEW_TEXT_CONTENT = "content";

        String EXTRA_IMAGE_LIST = "imagelist";
        String EXTRA_ALBUM_NAME = "name";
        String EXTRA_ADAPTER_NAME = "adapter";
        String EXTRA_CHAT_USER_ID = "chat_user_id";

        String USER_DETAIL_PARAM = "FROM_PAGE";
        String WEBVIEW_URL = "WEBVIEW_URL";

        String CUR_MESSAGE = "CUR_MESSAGE";

        String KEY_INTENT_IMAGE_CAPTURE_RESULT = "KEY_INTENT_IMAGE_CAPTURE_RESULT";
    }


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

    public interface HandlerConstant {
        int RECORD_FINISHED                  = 0x01;
        int PLAY_STOPPED                     = 0x02;
        int RECEIVE_MAX_VOLUME               = 0x03;
        int RECORD_AUDIO_TOO_LONG            = 0x04;
        int MESSAGE_RECEIVED                 = 0x05;
        int CONTACT_TAB_CHANGED              = 0x10;
    }

    public interface UrlConstant {
        String AVATAR_URL_PREFIX = "";
    }

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


        String SETTING_GLOBAL = "Global";
        String UPLOAD_IMAGE_INTENT_PARAMS = "com.lsx.bigtalk.upload.image.intent";

        int SERVICE_EVENTBUS_PRIORITY = 10;
        int MESSAGE_EVENTBUS_PRIORITY = 100;

        int MSG_PAGE_SIZE = 18;

        String MSG_SERVER_IP = "192.168.1.100";
        int MSG_SERVER_PORT = 8081;
        String MSFS_SERVER_ADDRESS = "http://192.168.1.100:8700";

        String SYSTEM_STORAGE_DIR_NAME = "BT-IM";
    }

    public interface ResultCodeConstant {
        int CAMERA_FOR_IMAGE = 5;
        int CAMERA_FOR_VIDEO = 5;
        int ALBUM_FOR_SINGLE_IMAGE = 3023;
        int ALBUM_FOR_MULTI_IMAGES = 3023;
    }

}
