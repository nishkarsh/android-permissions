# android-permissions [![Build & Publish](https://github.com/nishkarsh/android-permissions/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/nishkarsh/android-permissions/actions/workflows/build-and-release.yml)
An android library that makes it really easy to deal with dynamic permissions. Based on the context, library automatically decides whether to show a dialog (in case app is in foreground) or a notification (in case permission is required by a background service).

> After migration of artifacts to Maven Central, the newer builds would be published under group id `io.github.nishkarsh`.
> To include into the project, please select a particular published version and follow the instructions provided for the dependency management tool used: https://search.maven.org/artifact/io.github.nishkarsh/android-permissions

### How does it work?
- Get an instance of `PermissionManager` using `PermissionManager#getInstance()`
- Invoke `PermissionManager#checkPermissions(Collection<String> permissions, PermissionRequestListener listener)`
- If permissions are already granted by user, `PermissionRequestListener#onPermissionGranted()` is called directly.
- In case permissions are not yet provided, user is asked for permissions (a dialog in case app is in foreground, a notification in case it's in background). Asking for permissions from a `Service` follows the same principle.
- If user taps on `Deny` for any of the permissions asked by the app, `PermissionRequestListener#onPermissionDenied(DeniedPermissions deniedPermissions)` is called.
- If user taps on `Allow` for all the permissions asked by the app, `PermissionRequestListener#onPermissionGranted()` is called. This behaviour is per set of permissions asked.
- If `Don't ask again` is checked by the user in the permission dialog displayed, `DeniedPermission#shouldShowRationale()`  returns false.

### Customising notification
The notification shown to ask for permissions when the app is in background, could be customised using `NotificationSettings`. An instance of `NotificationSettings` can be created using `NotificationsSettings.Builder` to customise the title, message and/or a small icon for the notification.

```
PermissionManager instance = PermissionManager.getInstance(this);
NotificationSettings.Builder builder = new NotificationSettings.Builder();

NotificationSettings notificationSettings = builder
                .withTitle(R.string.title_action_needed)
                .withMessage(R.string.message_permission_required)
                .withSmallIcon(R.drawable.app_icon).build();

instance.setNotificationSettings(notificationSettings);
```

In the above example, the `R.string.title_action_needed`, `R.string.message_permission_required` and `R.drawable.app_icon` are the custom resources created in the project.

### Checking for specific permissions
```
PermissionManager permissionManager = PermissionManager.getInstance(context);
permissionManager.checkPermissions(singleton(Manifest.permission.CAMERA), new PermissionManager.PermissionRequestListener() {
                @Override
                public void onPermissionGranted() {
                    Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionDenied(DeniedPermissions deniedPermissions) {
                    String deniedPermissionsText = "Denied: " + Arrays.toString(deniedPermissions.toArray());
                    Toast.makeText(context, deniedPermissionsText, Toast.LENGTH_SHORT).show();
                    
                    for (DeniedPermission deniedPermission : deniedPermissions) {
                        if(deniedPermission.shouldShowRationale()) {
                            // Display a rationale about why this permission is required
                        }
                    }
                }
            });
```

### Suggestions for Usage
Permissions must be separated per functionality. After getting the instance of `PermissionManager`, the call to `PermissionManager#checkPermissions(Collection<String> permissions, PermissionRequestListener listener)` must be made for each set of permissions that are required for specific features. 

For example, if you need to access location for scanning beacons and need to access contacts and storage to perform another operation, ask for location permission and other permissions separately. This would help you focus only on a particular functionality at a time.

### Including into project

Gradle: `implementation 'io.github.nishkarsh:android-permissions:2.0.54'`

Add android-permissions as dependency inside app module build.gradle under dependencies block. Your app level build.gradle should look like:

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'io.github.nishkarsh:android-permissions:2.0.54'
}
```
