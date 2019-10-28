package com.intentfilter.androidpermissions;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.intentfilter.androidpermissions.helpers.Logger;
import com.intentfilter.androidpermissions.models.DeniedPermission;
import com.intentfilter.androidpermissions.models.DeniedPermissions;
import com.intentfilter.androidpermissions.services.BroadcastService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class PermissionsActivity extends AppCompatActivity {
    static final int PERMISSIONS_REQUEST_CODE = 100;
    public static final String EXTRA_PERMISSIONS_GRANTED = BuildConfig.LIBRARY_PACKAGE_NAME + ".PERMISSIONS_GRANTED";
    public static final String EXTRA_PERMISSIONS_DENIED = BuildConfig.LIBRARY_PACKAGE_NAME + ".PERMISSIONS_DENIED";
    static final String EXTRA_PERMISSIONS = BuildConfig.LIBRARY_PACKAGE_NAME + ".PERMISSIONS";
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
        DeniedPermissions deniedPermissions = new DeniedPermissions();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i]);
            } else {
                boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                deniedPermissions.add(new DeniedPermission(permissions[i], shouldShowRationale));
            }
        }

        new BroadcastService(this).broadcastPermissionRequestResult(grantedPermissions, deniedPermissions);
    }
}