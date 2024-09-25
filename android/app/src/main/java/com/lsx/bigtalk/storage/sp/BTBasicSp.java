package com.lsx.bigtalk.storage.sp;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


public class BTBasicSp {

    public static String tag = "BTBasicSp";
    private Context mContext;

    private final String systemSpFileName = "systemSp";
    private SharedPreferences systemSp;

    private final String userSpecificSpFileNamePrefix = "userSp";
    private SharedPreferences userSpecificSp;

    public BTBasicSp() {

    }

    public void init(Context context) throws Exception {
        if (null != mContext) {
            throw new Exception("no more initialization");
        }
        mContext = context;
        systemSp = mContext.getSharedPreferences(systemSpFileName + ".ini", Context.MODE_PRIVATE);
    }

    public void setUserIdentifier(String userIdentifier) {
        userSpecificSp = mContext.getSharedPreferences(
                userSpecificSpFileNamePrefix + userIdentifier + ".ini",
                Context.MODE_PRIVATE);
    }

    /***********************************************************************************************/
    public void setInt(String key, int value, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences.Editor editor = isUserSpecific ? userSpecificSp.edit() : systemSp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return Integer.MIN_VALUE;
        }
        SharedPreferences sp = isUserSpecific ? userSpecificSp : systemSp;
        return sp.getInt(key, defaultValue);
    }

    public void setString(String key, String value, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences.Editor editor = isUserSpecific ? userSpecificSp.edit() : systemSp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        SharedPreferences sp = isUserSpecific ? userSpecificSp : systemSp;
        return sp.getString(key, defaultValue);
    }

    public void setStringSet(String key, HashSet<String> value, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key) || null == value || value.isEmpty()) {
            return;
        }

        SharedPreferences sp = isUserSpecific ? userSpecificSp : systemSp;
        Set<String> newData = new HashSet<>();

        if (!value.isEmpty()) {
            newData.addAll(value);
        }

        Set<String> data = sp.getStringSet(key, new HashSet<>());
        if (!data.isEmpty()) {
            newData.addAll(data);
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(key, newData);
        editor.apply();
    }

    public HashSet<String> getStringSet(String key, HashSet<String> defaultValue, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return new HashSet<>();
        }
        SharedPreferences sp = isUserSpecific ? userSpecificSp : systemSp;
        Set<String> value = sp.getStringSet(key, defaultValue);
        if (null == value) {
            return null;
        }
        return (HashSet<String>) value;
    }

    public void setBoolean(String key, boolean value, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences.Editor editor = isUserSpecific ? userSpecificSp.edit() : systemSp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue, boolean isUserSpecific) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        SharedPreferences sp = isUserSpecific ? userSpecificSp : systemSp;
        return sp.getBoolean(key, defaultValue);
    }
    /***********************************************************************************************/
    public void setSystemInt(String key, int value) {
        setInt(key, value, false);
    }

    public int getSystemInt(String key, int defaultValue) {
        return getInt(key, defaultValue, false);
    }

    public void setUserSpecificInt(String key, int value) {
        setInt(key, value, true);
    }

    public int getUserSpecificInt(String key, int defaultValue) {
        return getInt(key, defaultValue, true);
    }


    public void setSystemBoolean(String key, boolean value) {
        setBoolean(key, value, false);
    }

    public boolean getSystemBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, false);
    }

    public void setUserSpecificBoolean(String key, boolean value) {
        setBoolean(key, value, true);
    }

    public boolean getUserSpecificBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, true);
    }


    public void setSystemString(String key, String value) {
        setString(key, value, false);
    }

    public String getSystemString(String key, String defaultValue) {
         return getString(key, defaultValue, false);
    }

    public void setUserSpecificString(String key, String value) {
        setString(key, value, true);
    }

    public String getUserSpecificString(String key, String defaultValue) {
        return getString(key, defaultValue, true);
    }


    public void setSystemStringSet(String key, HashSet<String> value) {
        setStringSet(key, value, false);
    }

    public HashSet<String> getSystemStringSet(String key, HashSet<String> defaultValue) {
        return getStringSet(key, defaultValue, false);
    }

    public void setUserSpecificStringSet(String key, HashSet<String> value) {
        setStringSet(key, value, true);
    }

    public HashSet<String> getUserSpecificStringSet(String key, HashSet<String> defaultValue) {
        return getStringSet(key, defaultValue, true);
    }

}
