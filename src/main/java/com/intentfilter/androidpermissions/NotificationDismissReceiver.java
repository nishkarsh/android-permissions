package com.intentfilter.androidpermissions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.intentfilter.androidpermissions.helpers.AppStatus;
import com.intentfilter.androidpermissions.helpers.Logger;

import java.util.Arrays;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.util.Arrays.asList;

public class NotificationDismissReceiver extends BroadcastReceiver {
    private static final Logger logger = Logger.loggerFor(NotificationDismissReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        String[] permissions = intent.getStringArrayExtra(PermissionsActivity.EXTRA_PERMISSIONS);

        if (new AppStatus(context).isInForeground()) {
            showPermissionsDialog(context, permissions);
        } else {
            PermissionManager.getInstance(context).removePendingPermissionRequests(asList(permissions));
        }

        logger.i("Pending permission notification dismissed. Cancelling: " + Arrays.toString(permissions));
    }

    private void showPermissionsDialog(Context context, String[] permissions) {
        Intent permissionsIntent = new Intent(context, PermissionsActivity.class);
        permissionsIntent.putExtra(PermissionsActivity.EXTRA_PERMISSIONS, permissions);
        permissionsIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(permissionsIntent);
    }
}