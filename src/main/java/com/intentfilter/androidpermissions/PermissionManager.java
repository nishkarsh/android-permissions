package com.intentfilter.androidpermissions;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;

import com.intentfilter.androidpermissions.helpers.Logger;
import com.intentfilter.androidpermissions.services.NotificationService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.intentfilter.androidpermissions.PermissionsActivity.EXTRA_PERMISSIONS;
import static com.intentfilter.androidpermissions.PermissionsActivity.EXTRA_PERMISSIONS_DENIED;
import static com.intentfilter.androidpermissions.PermissionsActivity.EXTRA_PERMISSIONS_DISABLED;
import static com.intentfilter.androidpermissions.PermissionsActivity.EXTRA_PERMISSIONS_GRANTED;


public class PermissionManager extends BroadcastReceiver {

    private Context context;
    private final Logger logger;
    private static PermissionManager permissionManager;
    private PermissionHandler permissionHandler;

    private PermissionManager(Context context) {
        this.context = context;
        this.permissionHandler = new PermissionHandler(this, context);
        this.logger = Logger.loggerFor(PermissionManager.class);
    }

    public static PermissionManager getInstance(Context context) {
        if (permissionManager == null) {
            permissionManager = new PermissionManager(context.getApplicationContext());
        }
        return permissionManager;
    }

    public void checkPermissions(@NonNull Collection<String> permissions, @NonNull final PermissionRequestListener listener) {
        permissionHandler.checkPermissions(permissions, listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String[] grantedPermissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS_GRANTED);
        String[] deniedPermissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS_DENIED);
        String[] disabledPermissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS_DISABLED);
        logPermissionsResponse(grantedPermissions, deniedPermissions, disabledPermissions);
        permissionHandler.onPermissionsResult(grantedPermissions, deniedPermissions, disabledPermissions);
    }

    void startPermissionActivity(Set<String> permissions) {
        Intent intent = permissionActivityIntent(permissions);
        context.startActivity(intent);
    }

    void showPermissionNotification(Set<String> permissions, @StringRes int titleResId, @StringRes int messageResId) {
        NotificationService notificationService = new NotificationService(context);
        Notification notification = notificationService.buildNotification(context.getString(titleResId),
                context.getString(messageResId), permissionActivityIntent(permissions), notificationDismissIntent(permissions));
        notificationService.notify(permissions.toString(), permissions.hashCode(), notification);
    }

    @NonNull
    private Intent permissionActivityIntent(Set<String> permissions) {
        return new Intent(context, PermissionsActivity.class)
                .putExtra(EXTRA_PERMISSIONS, permissions.toArray(new String[permissions.size()]))
                .setAction(permissions.toString())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    boolean permissionAlreadyGranted(String permission) {
        return checkSelfPermission(context, permission) == PERMISSION_GRANTED;
    }

    void registerBroadcastReceiver(String action) {
        logger.i("Registering for PERMISSIONS_REQUEST broadcast");
        LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(action));
    }

    void unregisterBroadcastReceiver() {
        logger.i("Un-registering for PERMISSIONS_REQUEST broadcast");
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    void removePendingPermissionRequests(List<String> permissions) {
        permissionHandler.invalidatePendingPermissionRequests(permissions);
    }

    @NonNull
    private PendingIntent notificationDismissIntent(Set<String> permissions) {
        Intent notificationDeleteIntent = new Intent(context, NotificationDismissReceiver.class);
        notificationDeleteIntent.putExtra(EXTRA_PERMISSIONS, permissions.toArray(new String[permissions.size()]));
        return PendingIntent.getBroadcast(context, PermissionsActivity.PERMISSIONS_REQUEST_CODE, notificationDeleteIntent, FLAG_ONE_SHOT);
    }

    private void logPermissionsResponse(String[] grantedPermissions, String[] deniedPermissions, String[] disabledPermissions) {
        logger.i(String.format("Received broadcast response for permission(s). \nGranted: %s\nDenied: %s\nDisabled: %s",
                Arrays.toString(grantedPermissions), Arrays.toString(deniedPermissions), Arrays.toString(disabledPermissions)));
    }

    public interface PermissionRequestListener {
        void onPermissionGranted();

        void onPermissionDenied();

        void onPermissionDisabled();
    }
}