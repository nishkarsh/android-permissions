# android-permissions [ ![Download](https://api.bintray.com/packages/nishkarsh/maven/com.intentfilter%3Aandroid-permissions/images/download.svg) ](https://bintray.com/nishkarsh/maven/com.intentfilter%3Aandroid-permissions/_latestVersion) [![Build Status](https://travis-ci.org/nishkarsh/android-permissions.svg?branch=master)](https://travis-ci.org/nishkarsh/android-permissions)
An android library that makes it really easy to deal with dynamic permissions. Based on the context, library automatically decides whether to show a dialog (in case app is in foreground) or a notification (in case permission is required by a background service).

### How does it work?
- Get an instance of `PermissionManager` using `PermissionManager#getInstance()`
- Invoke `PermissionManager#checkPermissions(Collection<String> permissions, PermissionRequestListener listener)`
- If permissions are already granted by user, `PermissionRequestListener#onPermissionGranted()` is called directly.
- In case permissions are not yet provided, user is asked for permissions (a dialog in case app is in foreground, a notification in case it's in background). Asking for permissions from a `Service` follows the same principle.
- If user taps on `Deny` for any of the permissions asked by the app, `PermissionRequestListener#onPermissionDenied(DeniedPermissions deniedPermissions)` is called.
- If user taps on `Allow` for all the permissions asked by the app, `PermissionRequestListener#onPermissionGranted()` is called. This behaviour is per set of permissions asked.
- If `Don't ask again` is checked by the user in the permission dialog displayed, `DeniedPermission#shouldShowRationale()`  returns false.

Example:
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

Gradle: `implementation 'com.intentfilter:android-permissions:2.0.54'`

Add android-permissions as dependency inside app module build.gradle under dependencies block. Your app level build.gradle should look like:

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.intentfilter:android-permissions:2.0.54'
}
```
