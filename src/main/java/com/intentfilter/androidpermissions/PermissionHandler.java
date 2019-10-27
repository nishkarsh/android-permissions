package com.intentfilter.androidpermissions;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.intentfilter.androidpermissions.helpers.AppStatus;
import com.intentfilter.androidpermissions.helpers.Logger;
import com.intentfilter.androidpermissions.models.DeniedPermission;
import com.intentfilter.androidpermissions.models.DeniedPermissions;
import com.intentfilter.androidpermissions.services.BroadcastService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

class PermissionHandler {
    private Logger logger;
    private PermissionManager manager;
    private final AppStatus appStatus;
    private HashMap<PermissionManager.PermissionRequestListener, Set<String>> requiredPermissionsMap = new HashMap<>();
    private Set<String> pendingPermissionRequests = new HashSet<>();

    PermissionHandler(PermissionManager manager, Context context) {
        this(new AppStatus(context), Logger.loggerFor(PermissionHandler.class), manager);
    }

    @VisibleForTesting
    PermissionHandler(AppStatus appStatus, Logger logger, PermissionManager manager) {
        this.logger = logger;
        this.manager = manager;
        this.appStatus = appStatus;
    }

    void checkPermissions(Collection<String> permissions, PermissionManager.PermissionRequestListener listener) {
        Set<String> permissionsToRequest = filterGrantedPermissions(permissions);

        if (permissionsToRequest.isEmpty()) {
            listener.onPermissionGranted();
        } else {
            requiredPermissionsMap.put(listener, new HashSet<>(permissionsToRequest));
            registerForBroadcastIfNeeded(BroadcastService.IntentAction.ACTION_PERMISSIONS_REQUEST);
            filterPendingPermissions(permissionsToRequest);
            if (!permissionsToRequest.isEmpty()) {
                requestPermissions(permissionsToRequest);
            }
        }
    }

    void onPermissionsResult(String[] grantedPermissions, DeniedPermissions deniedPermissions) {
        informPermissionsDenied(deniedPermissions);
        informPermissionsGranted(grantedPermissions);

        pendingPermissionRequests.removeAll(asList(grantedPermissions));
        pendingPermissionRequests.removeAll(deniedPermissions.stripped());
        if (pendingPermissionRequests.isEmpty()) {
            manager.unregisterBroadcastReceiver();
        }
    }

    @VisibleForTesting
    void requestPermissions(Set<String> permissions) {
        logger.i("No pending foreground permission request for " + permissions + ", asking.");

        pendingPermissionRequests.addAll(permissions);

        if (appStatus.isInForeground()) {
            manager.startPermissionActivity(permissions);
        } else {
            manager.showPermissionNotification(permissions, R.string.title_permission_required,
                    R.string.message_permission_required);
        }
    }

    void invalidatePendingPermissionRequests(Collection<String> permissions) {
        pendingPermissionRequests.removeAll(permissions);
        DeniedPermissions deniedPermissions = new DeniedPermissions();
        for (String permission : permissions) {
            deniedPermissions.add(new DeniedPermission(permission, false));
        }
        informPermissionsDenied(deniedPermissions);

        if (pendingPermissionRequests.isEmpty()) {
            manager.unregisterBroadcastReceiver();
        }
    }

    private void informPermissionsDenied(DeniedPermissions deniedPermissions) {
        ArrayList<PermissionManager.PermissionRequestListener> invalidatedListeners = new ArrayList<>();

        for (PermissionManager.PermissionRequestListener listener : requiredPermissionsMap.keySet()) {
            Set<String> permissionSet = requiredPermissionsMap.get(listener);
            Set<String> strippedDeniedPermissions = deniedPermissions.stripped();

            strippedDeniedPermissions.retainAll(permissionSet);
            if (!strippedDeniedPermissions.isEmpty()) {
                listener.onPermissionDenied(deniedPermissions);
                invalidatedListeners.add(listener);
            }
        }

        for (PermissionManager.PermissionRequestListener listener : invalidatedListeners) {
            requiredPermissionsMap.remove(listener);
        }
    }

    private void informPermissionsGranted(String[] grantedPermissions) {
        ArrayList<PermissionManager.PermissionRequestListener> invalidatedListeners = new ArrayList<>();

        for (PermissionManager.PermissionRequestListener listener : requiredPermissionsMap.keySet()) {
            Set<String> permissionSet = requiredPermissionsMap.get(listener);
            permissionSet.removeAll(asList(grantedPermissions));
            if (permissionSet.isEmpty()) {
                listener.onPermissionGranted();
                invalidatedListeners.add(listener);
            }
        }

        for (PermissionManager.PermissionRequestListener listener : invalidatedListeners) {
            requiredPermissionsMap.remove(listener);
        }
    }

    private void registerForBroadcastIfNeeded(String action) {
        if (pendingPermissionRequests.isEmpty()) {
            manager.registerBroadcastReceiver(action);
        }
    }

    @NonNull
    private Set<String> filterGrantedPermissions(Collection<String> permissions) {
        Set<String> permissionsToRequest = new HashSet<>();
        for (String permission : permissions) {
            if (!manager.permissionAlreadyGranted(permission)) {
                permissionsToRequest.add(permission);
            }
        }
        return permissionsToRequest;
    }

    private void filterPendingPermissions(Set<String> permissionsToRequest) {
        for (String permission : permissionsToRequest) {
            if (pendingPermissionRequests.contains(permission)) {
                logger.i("Permission request for " + permission + " pending, not asking again.");
            }
        }

        permissionsToRequest.removeAll(pendingPermissionRequests);
    }
}