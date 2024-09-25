package com.lsx.bigtalk;

import android.app.Activity;


public class StackManager {
    private static java.util.Stack<Activity> mActivityStack;
    private static StackManager instance;

    public static synchronized StackManager getStackManager() {
        if (instance == null) {
            instance = new StackManager();
        }
        return instance;
    }

    public void popActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
            mActivityStack.remove(activity);
        }
    }

    public Activity currentActivity() {
        if (mActivityStack == null || mActivityStack.isEmpty()) {
            return null;
        }
        return mActivityStack.lastElement();
    }

    public void pushActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new java.util.Stack<>();
        }
        mActivityStack.add(activity);
    }

    public void popTopActivitiesUntil(Class clazz) {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            if (activity.getClass().equals(clazz)) {
                break;
            }
            popActivity(activity);
        }
    }

    public void popAllActivities() {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            popActivity(activity);
        }
    }
}
