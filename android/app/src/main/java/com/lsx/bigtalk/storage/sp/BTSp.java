package com.lsx.bigtalk.storage.sp;

import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import de.greenrobot.event.EventBus;

import com.lsx.bigtalk.service.event.SessionEvent;


public class BTSp extends BTBasicSp {
    static final String KEYBOARD_HEIGHT_KEY = "KEYBOARD_HEIGHT_KEY";
    static final String DEFAULT_INPUTMETHOD_KEY = "DEFAULT_INPUTMETHOD_KEY";
    static final String DISCOVERY_DATA_KEY = "DISCOVERY_DATA_KEY";
    static final String NOTIFICATION_SWITCH_KEY = "NOTIFICATION_SWITCH_KEY";
    static final String SOUND_SWITCH_KEY = "SOUND_SWITCH_KEY";
    static final String VIBRATION_SWITCH_KEY = "VIBRATION_SWITCH_KEY";
    static final String SPIN_SESSION_LIST_KEY = "SPIN_SESSION_LIST_KEY";
    static final String SESSION_UPDATE_TIME_KEY = "SESSION_UPDATE_TIME_KEY";
    static final String SETTING_GLOBAL_NOTIFICATION_ON_KEY = "SETTING_GLOBAL_NOTIFICATION_ON_KEY";
    static final String SETTING_SESSION_NOTIFICATION_ON_KEY = "SETTING_SESSION_NOTIFICATION_ON_KEY";
    static final String USER_NAME_KEY = "USER_NAME_KEY";
    static final String PASSWORD_KEY = "PASSWORD_KEY";
    static final String LOGIN_ID_KEY = "LOGIN_ID_KEY";

    @SuppressLint("StaticFieldLeak")
    public static BTSp instance;
    public static BTSp getInstance() {
        if (null == instance) {
            instance = new BTSp();
        }
        return instance;
    }

    /************************************* session related ****************************************/
    /**
     * http://stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
     */
    public void setSessionSpin(String sessionKey, boolean isSpin) {
        if (TextUtils.isEmpty(sessionKey)) {
            return;
        }
        Set<String> oldSpinList = getUserSpecificStringSet(SPIN_SESSION_LIST_KEY, new HashSet<>());
        Set<String> newSpinList = new HashSet<>();
        if (!oldSpinList.isEmpty()) {
            newSpinList.addAll(oldSpinList);
        }

        if (isSpin) {
            newSpinList.add(sessionKey);
        } else {
            newSpinList.remove(sessionKey);
        }
        
        setUserSpecificStringSet(SPIN_SESSION_LIST_KEY, (HashSet<String>) newSpinList);
        
        EventBus.getDefault().post(SessionEvent.SPIN_SESSION_UPDATE);
    }
    
    public HashSet<String> getSpinSessionList() {
        HashSet<String> spinSessionList = getUserSpecificStringSet(SPIN_SESSION_LIST_KEY, new HashSet<>());
        return spinSessionList;
    }
    
    public boolean isSessionSpin(String sessionKey) {
        HashSet<String> spinSessionList = getSpinSessionList();
        return spinSessionList.contains(sessionKey);
    }


    /*************************************** login related ****************************************/
    public static class LoginModel {
        private final String userName;
        private final String password;
        private int loginId;

        public LoginModel(String userName, String password, int loginId) {
            this.userName = userName;
            this.password = password;
            this.loginId = loginId;
        }

        public void setLoginId(int loginId) {
            this.loginId = loginId;
        }
        
        public int getLoginId() {
            return loginId;
        }
        
        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }
    }
    
    public void setLoginInfo(String userName, String password, int loginId) {
        setSystemString(USER_NAME_KEY, userName);
        setSystemString(PASSWORD_KEY, password);
        setSystemInt(LOGIN_ID_KEY, loginId);
    }

    public LoginModel getLoginInfo() {
        String userName = getSystemString(USER_NAME_KEY,"");
        String password = getSystemString(PASSWORD_KEY, "");
        int loginId = getSystemInt(LOGIN_ID_KEY, 0);
        if (TextUtils.isEmpty(userName) || loginId == 0) {
            return null;
        }
        return new LoginModel(userName, password, loginId);
    }


    /*************************************** finder related ****************************************/
    public void setDiscoverData(String data) {
        setSystemString(DISCOVERY_DATA_KEY, data);
    }

    public String getDiscoverData() {
        return getSystemString(DISCOVERY_DATA_KEY, "");
    }

    public void setDefaultInputMethod(String value) {
        setSystemString(DEFAULT_INPUTMETHOD_KEY, value);
    }

    public String getDefaultInputMethod() {
        return getSystemString(DEFAULT_INPUTMETHOD_KEY, "");
    }

    public void setKeyboardHeight(int value) {
        setSystemInt(KEYBOARD_HEIGHT_KEY, value);
    }

    public int getKeyboardHeight() {
        return getSystemInt(KEYBOARD_HEIGHT_KEY, 0);
    }

    public void setGlobalNotificationOn(boolean value) {
        setSystemBoolean(SETTING_GLOBAL_NOTIFICATION_ON_KEY, value);
    }

    public boolean getGlobalNotificationOn() {
        return getSystemBoolean(SETTING_GLOBAL_NOTIFICATION_ON_KEY, false);
    }

    public void setSessionNotificationOn(String sessionKey, boolean value) {
        setSystemBoolean(SETTING_SESSION_NOTIFICATION_ON_KEY + sessionKey , value);
    }

    public boolean getSessionNotificationOn(String sessionKey) {
        return getSystemBoolean(SETTING_SESSION_NOTIFICATION_ON_KEY + sessionKey, false);
    }

    public void setSoundOn(Boolean value) {
        setSystemBoolean(SOUND_SWITCH_KEY, value);
    }

    public boolean getSoundOn() {
        return getSystemBoolean(SOUND_SWITCH_KEY, true);
    }

    public void setVibrationOn(Boolean value) {
        setSystemBoolean(VIBRATION_SWITCH_KEY, value);
    }

    public boolean getVibrationOn() {
        return getSystemBoolean(VIBRATION_SWITCH_KEY, true);
    }




//    public boolean getCfg(String key, CfgDimension dimension) {
//        boolean defaultOnff = dimension != CfgDimension.NOTIFICATION;
//        boolean onOff = sharedPreferences.getBoolean(dimension.name() + key, defaultOnff);
//        return onOff;
//    }
//
//
//    public void setCfg(String key, CfgDimension dimension, boolean onoff) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(dimension.name() + key, onoff);
//        //提交当前数据
//        editor.commit();
//    }
    
}
