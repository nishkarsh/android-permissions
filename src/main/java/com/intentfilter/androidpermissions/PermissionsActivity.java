package com.intentfilter.androidpermissions;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.intentfilter.androidpermissions.helpers.Logger;
import com.intentfilter.androidpermissions.services.BroadcastService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@TargetApi(23)
public class PermissionsActivity extends AppCompatActivity {

    static final int PERMISSIONS_REQUEST_CODE = 100;
    public static final String EXTRA_PERMISSIONS_GRANTED = BuildConfig.APPLICATION_ID + ".PERMISSIONS_GRANTED";
    public static final String EXTRA_PERMISSIONS_DENIED = BuildConfig.APPLICATION_ID + ".PERMISSIONS_DENIED";
    public static final String EXTRA_PERMISSIONS_DISABLED = BuildConfig.APPLICATION_ID + ".PERMISSIONS_DISABLED";
    static final String EXTRA_PERMISSIONS = BuildConfig.APPLICATION_ID + ".PERMISSIONS";
    static final Logger logger = Logger.loggerFor(PermissionsActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0 || permissions.length == 0) {
            logger.e("Permission request interrupted. Aborting.");

            PermissionManager.getInstance(this)
                    .removePendingPermissionRequests(asList(getIntent().getStringArrayExtra(EXTRA_PERMISSIONS)));

            //TODO figure out how to finish this activity when request is interrupted to avoid duplicate dialog
            finish();
            return;
        }

        logger.i("RequestPermissionsResult, sending broadcast for permissions " + Arrays.toString(permissions));

        sendPermissionResponse(permissions, grantResults);
        finish();
    }

    private void sendPermissionResponse(@NonNull String[] permissions, @NonNull int[] grantResults) {
        Set<String> grantedPermissions = new HashSet<>();
        Set<String> deniedPermissions = new HashSet<>();
        Set<String> disabledPermissions = new HashSet<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            } else {
                if (shouldShowRequestPermissionRationale(permissions[i])) deniedPermissions.add(permissions[i]);
                else disabledPermissions.add(permissions[i]);
            }
        }

        new BroadcastService(this).broadcastPermissionRequestResult(grantedPermissions, deniedPermissions, disabledPermissions);
    }
}