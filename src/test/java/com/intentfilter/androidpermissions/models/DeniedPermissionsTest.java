package com.intentfilter.androidpermissions.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DeniedPermissionsTest {
    @Test
    public void shouldReturnPermissionNamesInCollection() {
        DeniedPermissions deniedPermissions = new DeniedPermissions();
        deniedPermissions.add(new DeniedPermission("permissionOne", true));
        deniedPermissions.add(new DeniedPermission("permissionTwo", false));
        deniedPermissions.add(new DeniedPermission("permissionThree", true));

        Collection<String> strippedPermissions = deniedPermissions.stripped();

        assertEquals(strippedPermissions.size(), 3);
        assertThat(strippedPermissions, hasItem("permissionOne"));
        assertThat(strippedPermissions, hasItem("permissionTwo"));
        assertThat(strippedPermissions, hasItem("permissionThree"));
    }
}