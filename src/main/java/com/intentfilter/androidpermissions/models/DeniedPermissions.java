package com.intentfilter.androidpermissions.models;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.parceler.Parcel;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@Parcel
public class DeniedPermissions extends HashSet<DeniedPermission> {

    @VisibleForTesting
    public static DeniedPermissions create(@NonNull DeniedPermission... deniedPermissions) {
        DeniedPermissions permissions = new DeniedPermissions();
        permissions.addAll(asList(deniedPermissions));
        return permissions;
    }

    @NonNull
    public Set<String> stripped() {
        HashSet<String> strippedPermissions = new HashSet<>();
        for (DeniedPermission deniedPermission : this) {
            strippedPermissions.add(deniedPermission.permission);
        }

        return strippedPermissions;
    }
}
