package com.intentfilter.androidpermissions;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

public class NotificationSettings {
    @StringRes
    private int titleResId;
    @StringRes
    private int messageResId;
    @DrawableRes
    private int smallIconResId;
    @ColorInt
    private int color;

    private NotificationSettings(@StringRes int titleResId, @StringRes int messageResId,
                                 @DrawableRes int smallIconResId, @ColorInt int color) {
        this.titleResId = titleResId;
        this.messageResId = messageResId;
        this.smallIconResId = smallIconResId;
        this.color = color;
    }

    static NotificationSettings getDefault() {
        return new NotificationSettings(R.string.title_permission_required,
                R.string.message_permission_required,
                android.R.mipmap.sym_def_app_icon, NotificationCompat.COLOR_DEFAULT);
    }

    @StringRes
    public int getTitleResId() {
        return titleResId;
    }

    @StringRes
    public int getMessageResId() {
        return messageResId;
    }

    @DrawableRes
    public int getSmallIconResId() {
        return smallIconResId;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public static class Builder {
        private final NotificationSettings notificationSettings;

        public Builder() {
            this.notificationSettings = getDefault();
        }

        public Builder withTitle(@StringRes int titleResId) {
            this.notificationSettings.titleResId = titleResId;
            return this;
        }

        public Builder withMessage(@StringRes int messageResId) {
            this.notificationSettings.messageResId = messageResId;
            return this;
        }

        public Builder withSmallIcon(@DrawableRes int smallIconResId) {
            this.notificationSettings.smallIconResId = smallIconResId;
            return this;
        }

        public Builder withColor(@ColorInt int colorResId) {
            this.notificationSettings.color = colorResId;
            return this;
        }

        public NotificationSettings build() {
            return notificationSettings;
        }
    }
}
