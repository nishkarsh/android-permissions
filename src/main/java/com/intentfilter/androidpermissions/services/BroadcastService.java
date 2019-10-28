package com.intentfilter.androidpermissions.services;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.intentfilter.androidpermissions.models.DeniedPermissions;

import org.parceler.Parcels;

import java.util.Set;

import static com.intentfilter.androidpermissions.PermissionsActivity.EXTRA_PERMISSIONS_DENIED;
import static com.intentfilter.androidpermissions.PermissionsActivity.EXTRA_PERMISSIONS_GRANTED;

public class BroadcastService {
    private final Context context;

    public BroadcastService(Context context) {
        this.context = context;
    }

    public void broadcastPermissionRequestResult(Set<String> grantedPermissions, DeniedPermissions deniedPermissions) {
        Intent intent = new Intent(IntentAction.ACTION_PERMISSIONS_REQUEST);
        intent.putExtra(EXTRA_PERMISSIONS_GRANTED, grantedPermissions.toArray(new String[0]));
        intent.putExtra(EXTRA_PERMISSIONS_DENIED, Parcels.wrap(deniedPermissions));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public interface IntentAction {
        String ACTION_PERMISSIONS_REQUEST = "com.intentfilter.androidpermissions.PERMISSIONS_REQUEST";
    }
}