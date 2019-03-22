package com.intentfilter.androidpermissions;

import com.intentfilter.androidpermissions.PermissionManager.PermissionRequestListener;
import com.intentfilter.androidpermissions.helpers.AppStatus;
import com.intentfilter.androidpermissions.helpers.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;

import static com.intentfilter.androidpermissions.services.BroadcastService.IntentAction.ACTION_PERMISSIONS_REQUEST;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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

        verify(manager, never()).showPermissionNotification(ArgumentMatchers.<String>anySet(), anyInt(), anyInt());
        verify(manager, never()).startPermissionActivity(ArgumentMatchers.<String>anySet());
    }

    @Test
    public void shouldNotRespondToListenerIfAnyRequiredPermissionPending() {
        permissionHandler.requestPermissions(new HashSet<>(singletonList(PERMISSION_2)));
        reset(manager);

        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        verify(requestListener, never()).onPermissionDenied();
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
        verify(manager, never()).showPermissionNotification(ArgumentMatchers.<String>anySet(), anyInt(), anyInt());
    }

    @Test
    public void shouldShowPermissionsNotificationIfAppIsInBackground() {
        List<String> permissions = asList(PERMISSION_1, PERMISSION_2);
        when(appStatus.isInForeground()).thenReturn(false);

        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager).showPermissionNotification(new HashSet<>(permissions), R.string.title_permission_required, R.string.message_permission_required);
        verify(manager, never()).startPermissionActivity(ArgumentMatchers.<String>anySet());
    }

    @Test
    public void shouldInformListenersForDeniedPermissions() {
        List<String> permissions = singletonList(PERMISSION_1);
        permissionHandler.checkPermissions(permissions, requestListener);

        permissionHandler.onPermissionsResult(new String[]{}, new String[]{PERMISSION_1});

        verify(requestListener).onPermissionDenied();
    }

    @Test
    public void shouldInformListenersForGrantedPermissions() {
        List<String> permissions = singletonList(PERMISSION_1);
        permissionHandler.checkPermissions(permissions, requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_1}, new String[]{});

        verify(requestListener).onPermissionGranted();
    }

    @Test
    public void shouldInformListenerOnlyWhenAllPermissionsGranted() {
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_1}, new String[]{});

        verify(requestListener, never()).onPermissionGranted();
        verify(requestListener, never()).onPermissionDenied();

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, new String[]{});

        verify(requestListener).onPermissionGranted();
        verify(requestListener, never()).onPermissionDenied();
    }

    @Test
    public void shouldInformListenersForRespectivePermissions() {
        PermissionRequestListener anotherRequestListener = Mockito.mock(PermissionRequestListener.class);
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);
        permissionHandler.checkPermissions(singletonList(PERMISSION_2), anotherRequestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, new String[]{PERMISSION_1});

        verify(requestListener).onPermissionDenied();
        verify(anotherRequestListener).onPermissionGranted();
    }

    @Test
    public void shouldUnregisterForBroadcastWhenAllPermissionRequestsAreResponded() {
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, new String[]{PERMISSION_1});

        verify(manager).unregisterBroadcastReceiver();
    }

    @Test
    public void shouldNotUnregisterForBroadcastIfAnyPermissionIsPending() {
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_2}, new String[]{});

        verify(manager, never()).unregisterBroadcastReceiver();
    }

    @Test
    public void shouldRemoveListenerOnceInformedAboutPermissionsResult() {
        PermissionRequestListener anotherRequestListener = Mockito.mock(PermissionRequestListener.class);
        permissionHandler.checkPermissions(singletonList(PERMISSION_1), anotherRequestListener);
        permissionHandler.checkPermissions(asList(PERMISSION_1, PERMISSION_2), requestListener);
        permissionHandler.onPermissionsResult(new String[]{}, new String[]{PERMISSION_2});

        permissionHandler.onPermissionsResult(new String[]{PERMISSION_1}, new String[]{});

        verify(anotherRequestListener).onPermissionGranted();
        verify(requestListener, never()).onPermissionGranted();
        verify(requestListener, times(1)).onPermissionDenied();
    }

    @Test
    public void shouldInvalidatePendingPermissionRequests() {
        List<String> permissions = asList(PERMISSION_1, PERMISSION_2);
        when(appStatus.isInForeground()).thenReturn(true);
        permissionHandler.checkPermissions(permissions, requestListener);

        permissionHandler.invalidatePendingPermissionRequests(permissions);

        verify(manager).unregisterBroadcastReceiver();
        verify(requestListener).onPermissionDenied();

        //To verify if permissions are asked again once invalidated
        permissionHandler.checkPermissions(permissions, requestListener);

        verify(manager, times(2)).startPermissionActivity(new HashSet<>(permissions));
    }
}