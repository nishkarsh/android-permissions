package com.intentfilter.androidpermissions.helpers;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class AppStatus {
    private final Context context;

    public AppStatus(Context context) {
        this.context = context;
    }

    public boolean isInForeground() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo process : runningAppProcesses) {
            if (process.processName.equals(context.getApplicationInfo().processName))
                return process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
        }
        return false;
    }
}