package com.intentfilter.androidpermissions;

import com.intentfilter.androidpermissions.PermissionManager.PermissionRequestListener;
import com.intentfilter.androidpermissions.helpers.AppStatus;
import com.intentfilter.androidpermissions.helpers.Logger;
import com.intentfilter.androidpermissions.models.DeniedPermission;
import com.intentfilter.androidpermissions.models.DeniedPermissions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.intentfilter.androidpermissions.services.BroadcastService.IntentAction.ACTION_PERMISSIONS_REQUEST;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionHandlerTest {
    @Mock
    private AppStatus appStatus;
    @Mock
    private PermissionManager manager;
    @Mock
    private PermissionRequestListener requestListener;
    @Mock
    private Logger logger;

    @Captor
    ArgumentCaptor<DeniedPermissions> permissionsCaptor;

    private PermissionHandler permissionHandler;
    private static final String PERMISSION_1 = "permission1";
    private static final String PERMISSION_2 = "permission2";
    private static final String PERMISSION_3 = "permission3";

    @Before
    public void setUp() {
        permissionHandler = new PermissionHandler(appStatus, logger, manager);
    }

    @Test
    public void shouldNotAskForPermissionsIfAlreadyGranted() {
        when(manager.permissionAlreadyGranted(PERMISSION_1)).thenReturn(true);
        when(manager.permissionAlreadyGranted(PERMISSION_2)).thenReturn(true);

        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        verify(requestListener).onPermissionGranted();
        verify(manager, never()).registerBroadcastReceiver(anyString());
    }

    @Test
    public void shouldNotAskForPendingPermissions() {
        permissionHandler.requestPermissions(new HashSet<>(singletonList(PERMISSION_2)));
        reset(manager);
        when(manager.permissionAlreadyGranted(PERMISSION_1)).thenReturn(true);

        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        verify(manager, never()).showPermissionNotification(ArgumentMatchers.<String>anySet());
        verify(manager, never()).startPermissionActivity(ArgumentMatchers.<String>anySet());
    }

    @Test
    public void shouldNotRespondToListenerIfAnyRequiredPermissionPending() {
        permissionHandler.requestPermissions(new HashSet<>(singletonList(PERMISSION_2)));
        reset(manager);

        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        verify(requestListener, never()).onPermissionDenied(any(DeniedPermissions.class));
        verify(requestListener, never()).onPermissionGranted();
    }

    @Test
    public void shouldRegisterForBroadcastsWhenAskingPermissionsForFirstTime() {
        List<String> permissions = singletonList(PERMISSION_1);

        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager).registerBroadcastReceiver(ACTION_PERMISSIONS_REQUEST);
    }

    @Test
    public void shouldNotRegisterForBroadcastIfPermissionsAlreadyPending() {
        List<String> permissions = singletonList(PERMISSION_1);
        permissionHandler.requestPermissions(new HashSet<>(permissions));
        reset(manager);

        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager, never()).registerBroadcastReceiver(ACTION_PERMISSIONS_REQUEST);
    }

    @Test
    public void shouldOnlyAskForNonPendingAndNonGrantedPermissions() {
        permissionHandler.requestPermissions(new HashSet<>(singletonList(PERMISSION_2)));
        reset(manager);
        when(manager.permissionAlreadyGranted(PERMISSION_1)).thenReturn(true);
        when(appStatus.isInForeground()).thenReturn(true);

        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2, PERMISSION_3), requestListener);

        verify(manager, times(3)).permissionAlreadyGranted(anyString());
        verify(manager).startPermissionActivity(new HashSet<>(singletonList(PERMISSION_3)));
        verifyNoMoreInteractions(manager);
    }

    @Test
    public void shouldShowPermissionsDialogIfAppIsInForeground() {
        List<String> permissions = asList(PERMISSION_1, PERMISSION_2);
        when(appStatus.isInForeground()).thenReturn(true);

        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager).startPermissionActivity(new HashSet<>(permissions));
        verify(manager, never()).showPermissionNotification(ArgumentMatchers.<String>anySet());
    }

    @Test
    public void shouldShowPermissionsNotificationIfAppIsInBackground() {
        List<String> permissions = asList(PERMISSION_1, PERMISSION_2);
        when(appStatus.isInForeground()).thenReturn(false);

        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager).showPermissionNotification(new HashSet<>(permissions));
        verify(manager, never()).startPermissionActivity(ArgumentMatchers.<String>anySet());
    }

    @Test
    public void shouldInformListenersForDeniedPermissions() {
        List<String> permissions = singletonList(PERMISSION_1);
        DeniedPermission deniedPermission = new DeniedPermission(PERMISSION_1, false);
        DeniedPermissions deniedPermissions = DeniedPermissions.create(deniedPermission);
        permissionHandler.checkPermissions(permissions, requestListener);

        permissionHandler.onPermissionsResult(new String[]{}, deniedPermissions);

        verify(requestListener).onPermissionDenied(deniedPermissions);
    }

    @Test
    public void shouldInformListenersForGrantedPermissions() {
        List<String> permissions = singletonList(PERMISSION_1);
        permissionHandler.checkPermissions(permissions, requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_1}, new DeniedPermissions());

        verify(requestListener).onPermissionGranted();
    }

    @Test
    public void shouldInformListenerOnlyWhenAllPermissionsGranted() {
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_1}, new DeniedPermissions());

        verify(requestListener, never()).onPermissionGranted();
        verify(requestListener, never()).onPermissionDenied(any(DeniedPermissions.class));

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, new DeniedPermissions());

        verify(requestListener).onPermissionGranted();
        verify(requestListener, never()).onPermissionDenied(any(DeniedPermissions.class));
    }

    @Test
    public void shouldInformListenersForRespectivePermissions() {
        PermissionRequestListener anotherRequestListener = Mockito.mock(PermissionRequestListener.class);
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);
        permissionHandler.checkPermissions(singletonList(PERMISSION_2), anotherRequestListener);
        DeniedPermission deniedPermission = new DeniedPermission(PERMISSION_1, false);
        DeniedPermissions deniedPermissions = DeniedPermissions.create(deniedPermission);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, deniedPermissions);

        verify(requestListener).onPermissionDenied(deniedPermissions);
        verify(anotherRequestListener).onPermissionGranted();
    }

    @Test
    public void shouldUnregisterForBroadcastWhenAllPermissionRequestsAreResponded() {
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);
        DeniedPermission deniedPermission = new DeniedPermission(PERMISSION_1, false);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, DeniedPermissions.create(deniedPermission));

        verify(manager).unregisterBroadcastReceiver();
    }

    @Test
    public void shouldNotUnregisterForBroadcastIfAnyPermissionIsPending() {
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, new DeniedPermissions());

        verify(manager, never()).unregisterBroadcastReceiver();
    }

    @Test
    public void shouldRemoveListenerOnceInformedAboutPermissionsResult() {
        PermissionRequestListener anotherRequestListener = Mockito.mock(PermissionRequestListener.class);
        permissionHandler.checkPermissions(singletonList(PERMISSION_1), anotherRequestListener);
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);
        DeniedPermissions deniedPermissions = DeniedPermissions.create(new DeniedPermission(PERMISSION_2, true));
        permissionHandler.onPermissionsResult(new String[]{}, deniedPermissions);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_1}, new DeniedPermissions());

        verify(anotherRequestListener).onPermissionGranted();
        verify(requestListener, never()).onPermissionGranted();
        verify(requestListener, times(1)).onPermissionDenied(deniedPermissions);
    }

    @Test
    public void shouldInvalidatePendingPermissionRequests() {
        List<String> permissions = asList(PERMISSION_1, PERMISSION_2);
        when(appStatus.isInForeground()).thenReturn(true);
        permissionHandler.checkPermissions(permissions, requestListener);

        permissionHandler.invalidatePendingPermissionRequests(permissions);

        verify(manager).unregisterBroadcastReceiver();
        verify(requestListener).onPermissionDenied(permissionsCaptor.capture());
        DeniedPermissions deniedPermissions = permissionsCaptor.getValue();
        assertThat(deniedPermissions, hasItems(
                new DeniedPermission(PERMISSION_1, false),
                new DeniedPermission(PERMISSION_2, false)));

        //To verify if permissions are asked again once invalidated
        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager, times(2)).startPermissionActivity(new HashSet<>(permissions));
    }

    @Test
    public void shouldSynchronizeAccessToPendingPermissionRequests() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(3);

        for (int num = 0; num < 20; num++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2, PERMISSION_3), Mockito.mock(PermissionRequestListener.class));
                    permissionHandler.onPermissionsResult(new String[]{PERMISSION_1, PERMISSION_2, PERMISSION_3}, new DeniedPermissions());
                    countDownLatch.countDown();
                }
            }).start();
        }

        countDownLatch.await(800, MILLISECONDS);
        assertThat(countDownLatch.getCount(), is(0L));
    }
}