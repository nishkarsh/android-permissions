package com.intentfilter.androidpermissions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

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
    private HashMap<PermissionManager.PermissionRequestListener, Set> requiredPermissionsMap = new HashMap<>();
    private Set<String> pendingPermissionRequests = new HashSet<>();

    public PermissionHandler(PermissionManager manager, Context context) {
        this(new AppStatus(context), Logger.loggerFor(PermissionHandler.class), manager);
    }

    public PermissionHandler(AppStatus appStatus, Logger logger, PermissionManager manager) {
        this.logger = logger;
        this.manager = manager;
        this.appStatus = appStatus;
    }

    public void checkPermissions(Collection<String> permissions, PermissionManager.PermissionRequestListener listener) {
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

    public void onPermissionsResult(String[] grantedPermissions, String[] deniedPermissions) {
        informPermissionsDenied(deniedPermissions);
        informPermissionsGranted(grantedPermissions);

        pendingPermissionRequests.removeAll(asList(grantedPermissions));
        pendingPermissionRequests.removeAll(asList(deniedPermissions));
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

    @VisibleForTesting
    void invalidatePendingPermissionRequests(Collection<String> permissions) {
        pendingPermissionRequests.removeAll(permissions);
        informPermissionsDenied(permissions.toArray(new String[permissions.size()]));

        if (pendingPermissionRequests.isEmpty()) {
            manager.unregisterBroadcastReceiver();
        }
    }

    private void informPermissionsDenied(String[] deniedPermissions) {
        ArrayList<PermissionManager.PermissionRequestListener> invalidatedListeners = new ArrayList<>();

        for (String deniedPermission : deniedPermissions) {
            for (PermissionManager.PermissionRequestListener listener : requiredPermissionsMap.keySet()) {
                Set permissionSet = requiredPermissionsMap.get(listener);
                if (permissionSet.contains(deniedPermission)) {
                    listener.onPermissionDenied();
                    invalidatedListeners.add(listener);
                }
            }

            for (PermissionManager.PermissionRequestListener listener : invalidatedListeners) {
                requiredPermissionsMap.remove(listener);
            }
            invalidatedListeners.clear();
        }
    }

    private void informPermissionsGranted(String[] grantedPermissions) {
        ArrayList<PermissionManager.PermissionRequestListener> invalidatedListeners = new ArrayList<>();

        for (PermissionManager.PermissionRequestListener listener : requiredPermissionsMap.keySet()) {
            Set permissionSet = requiredPermissionsMap.get(listener);
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