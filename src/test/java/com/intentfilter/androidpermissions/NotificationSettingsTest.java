package com.intentfilter.androidpermissions;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NotificationSettingsTest {
    @Test
    public void shouldCreateNotificationSettingsWithProperParams() {
        NotificationSettings.Builder builder = new NotificationSettings.Builder();
        NotificationSettings settings = builder.withTitle(R.string.notification_channel_name)
                .withMessage(R.string.message_permission_required)
                .withSmallIcon(android.R.drawable.ic_dialog_info)
                .withColorResId(android.R.color.holo_blue_bright)
                .build();

        assertThat(settings.getTitleResId(), is(R.string.notification_channel_name));
        assertThat(settings.getMessageResId(), is(R.string.message_permission_required));
        assertThat(settings.getSmallIconResId(), is(android.R.drawable.ic_dialog_info));
        assertThat(settings.getColorResId(), is(android.R.color.holo_blue_bright));
    }

    @Test
    public void shouldCreateNotificationSettingsWithDefaultSettings() {
        NotificationSettings settings = new NotificationSettings.Builder().build();

        assertThat(settings.getTitleResId(), is(R.string.title_permission_required));
        assertThat(settings.getMessageResId(), is(R.string.message_permission_required));
        assertThat(settings.getSmallIconResId(), is(android.R.mipmap.sym_def_app_icon));
        assertThat(settings.getColorResId(), is(android.R.color.transparent));
    }
}