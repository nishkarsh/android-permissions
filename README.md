# android-permissions
An android library that makes it really easy to deal with dynamic permissions. Based on the context, library automatically decides whether to show a dialog (in case app is in foreground) or a notification (in case permission is required by a background service).

### Including into project

Make your project-level build.gradle has jcenter() under repositories block. Your build.gradle should look like:

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.3'
    }
}
```

Gradle: `compile 'com.intentfilter:android-permissions:0.1.0-alpha'`

Add as android-permissions as dependency inside app module level build.gradle under dependencies block. Your app level build.gradle should look like:

```
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.intentfilter:android-permissions:0.1.0-alpha'
}
```
