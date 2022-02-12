package com.intentfilter.androidpermissions.helpers;

import android.os.Build;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VersionOrchestratorTest {
    @Test
    public void shouldReturnCombinedFlagsWithImmutableFlagForVersionAboveM() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), android.os.Build.VERSION_CODES.N);

        int immutablePendingIntentFlags = VersionOrchestrator.getImmutablePendingIntentFlags(FLAG_ONE_SHOT);

        assertThat(immutablePendingIntentFlags, is(FLAG_ONE_SHOT | FLAG_IMMUTABLE));
    }

    @Test
    public void shouldReturnCombinedFlagsWithImmutableFlagForVersionEqualsM() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), android.os.Build.VERSION_CODES.M);

        int immutablePendingIntentFlags = VersionOrchestrator.getImmutablePendingIntentFlags(FLAG_ONE_SHOT);

        assertThat(immutablePendingIntentFlags, is(FLAG_ONE_SHOT | FLAG_IMMUTABLE));
    }

    @Test
    public void shouldReturnCombinedFlagsWithoutImmutableFlagForVersionBelowM() throws Exception {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), Build.VERSION_CODES.LOLLIPOP);

        int pendingIntentFlags = VersionOrchestrator.getImmutablePendingIntentFlags(FLAG_ONE_SHOT);

        assertThat(pendingIntentFlags, is(FLAG_ONE_SHOT));
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}