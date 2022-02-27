package com.intentfilter.androidpermissions;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class NotificationSettings {
    @StringRes
    private int titleResId;
    @StringRes
    private int messageResId;
    @DrawableRes
    private int smallIconResId;

    private NotificationSettings(@StringRes int titleResId, @StringRes int messageResId,
                                 @DrawableRes int smallIconResId) {
        this.titleResId = titleResId;
        this.messageResId = messageResId;
        this.smallIconResId = smallIconResId;
    }

    static NotificationSettings getDefault() {
        return new NotificationSettings(R.string.title_permission_required,
                R.string.message_permission_required, android.R.mipmap.sym_def_app_icon);
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

        public NotificationSettings build() {
            return notificationSettings;
        }
    }
}
