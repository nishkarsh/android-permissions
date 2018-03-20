# android-permissions [ ![Download](https://api.bintray.com/packages/nishkarsh/maven/com.intentfilter%3Aandroid-permissions/images/download.svg) ](https://bintray.com/nishkarsh/maven/com.intentfilter%3Aandroid-permissions/_latestVersion) [![Build Status](https://travis-ci.org/nishkarsh/android-permissions.svg?branch=master)](https://travis-ci.org/nishkarsh/android-permissions)
An android library that makes it really easy to deal with dynamic permissions. Based on the context, library automatically decides whether to show a dialog (in case app is in foreground) or a notification (in case permission is required by a background service).

### How does it work?
- Get an instance of `PermissionManager` using `PermissionManager#getInstance()`
- Invoke `PermissionManager#checkPermissions(Collection<String> permissions, PermissionRequestListener listener)`
- If permissions are already granted by user, `PermissionRequestListener#onPermissionGranted()` would be called directly.
- In case permissions are not yet provided, user would be asked for permissions (a dialog in case app is in foreground, a notification in case it's in background)
- If user taps on `Deny` for any of the permissions asked by the app, `PermissionRequestListener#onPermissionDenied()` would be called.
- If user taps on `Allow` for all the permissions asked by the app, `PermissionRequestListener#onPermissionGranted()` would be called.

Example:
```
PermissionManager permissionManager = PermissionManager.getInstance(context);
permissionManager.checkPermissions(singleton(Manifest.permission.CAMERA), new PermissionManager.PermissionRequestListener() {
                @Override
                public void onPermissionGranted() {
                    Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionDenied() {
                    Toast.makeText(context, "Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            });
```

### Including into project

Gradle: `implementation 'com.intentfilter:android-permissions:0.1.6'`

Add as android-permissions as dependency inside app module level build.gradle under dependencies block. Your app level build.gradle should look like:

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.intentfilter:android-permissions:0.1.6'
}
```
