package com.intentfilter.androidpermissions.models;

import android.support.annotation.NonNull;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class DeniedPermission {
    String permission;
    boolean shouldShowRationale;

    @ParcelConstructor
    public DeniedPermission(String permission, boolean shouldShowRationale) {
        this.permission = permission;
        this.shouldShowRationale = shouldShowRationale;
    }

    public boolean shouldShowRationale() {
        return shouldShowRationale;
    }

    @NonNull
    @Override
    public String toString() {
        return permission;
    }


    @Override
    public boolean equals(Object anotherDeniedPermission) {
        if (this == anotherDeniedPermission) return true;
        if (anotherDeniedPermission == null) return false;
        return permission.equals(((DeniedPermission) anotherDeniedPermission).permission);
    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }
}
