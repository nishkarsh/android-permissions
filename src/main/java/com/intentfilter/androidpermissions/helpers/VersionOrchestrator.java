package com.intentfilter.androidpermissions.helpers;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

public class VersionOrchestrator {
    public static int getImmutablePendingIntentFlags(int... flags) {
        int combinedImmutableFlags = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            combinedImmutableFlags = FLAG_IMMUTABLE;
        }

        for (int flag : flags) {
            combinedImmutableFlags = combinedImmutableFlags | flag;
        }

        return combinedImmutableFlags;
    }
}
