# react-native-firebase-push-notifications

This project is allows you to recieve remote push notifications for iOS and Android. There is support for local notifications and messaging channel's which will be updated periodically.

❗ This is a work in progress, the remote notifications work and has been done as priority, the rest will follow periodically.

To get started follow the instructions below:

## 🚧 Getting started

`$ npm install react-native-firebase-push-notifications --save`

or

`$ yarn add react-native-firebase-push-notifications`

then for iOS

`$ cd ios && pod install`

### Mostly automatic installation

Do this if you are not on react-native 0.59 or less, there will not be much support for manual integration.

`$ react-native link react-native-firebase-push-notifications`

## 🔧 Android Setup

### Add firebase credentials

The Firebase console provides a `google-services.json` file containing a set of credentials for Android devices to use when authenticating with your Firebase project.

### Setup Credentials

1. Select your firebase android project
2. Select the android icon that will open the configuration section
   1. Fill in the required information
   2. Dpwnload the `google-services.json` file. Then Switch to the Project view in Android Studio to see your project root directory. Move the google-services.json file that you just downloaded into your Android app module root directory. eg. `/yourAppsName/android/app`
3. Add the google-services plugin inside `yourAppsName/android`
4. (Miss it if already have firebase) 
```javascript buildscript {
  repositories {
      // Check that you have the following line (if not, add it):
      google()  // Google's Maven repository
  }
  dependencies {
      ...
      // Add this line
      classpath 'com.google.gms:google-services:4.3.3'
    }
  allprojects {
    ...
    repositories {
        // Check that you have the following line (if not, add it):
        google()  // Google's Maven repository
        ...
      }
  }
  ```

5. (Miss it if already have firebase) 
App-level build.gradle (<project>/<app-module>/build.gradle):

```javascript
    apply plugin: 'com.android.application'
    // Add this line
    apply plugin: 'com.google.gms.google-services'

    dependencies {
    // add the Firebase SDK for Google Analytics
    implementation 'com.google.firebase:firebase-analytics:17.2.2'
    // add SDKs for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    }
```

6. Finally, press 'Sync now' in the bar that appears in the IDE
7. Update the Android Manifest
   1. add the permissions

   ```
   <manifest ...>
       <uses-permission android:name="android.permission.INTERNET" />
       <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
       <uses-permission android:name="android.permission.VIBRATE" />
   ```

   2. Setup launch mode

   ```
   <activity
   ...
   android:launchMode="singleTop"
   >
   ```

   3. icon & color (optional) <https://github.com/firebase/quickstart-android/tree/master/messaging#custom-default-icon>
   If conflicts with existed firebase package, remove this lines, because they already exists.

   ```
   <application ...>
   <meta-data
       android:name="com.google.firebase.messaging.default_notification_icon"
       android:resource="@drawable/ic_stat_ic_notification" />
   <meta-data
       android:name="com.google.firebase.messaging.default_notification_color"
       android:resource="@color/colorAccent" />
   </application>
   ```

   4. Notification channels (Optional) - Work in progress
   If conflicts with existed firebase package, remove this lines, because they already exists.

   ```
   <application ...>
   <meta-data
       android:name="com.google.firebase.messaging.default_notification_channel_id"
       android:value="@string/default_notification_channel_id"/>
   </application>
   ```

   5. Scheduled Notifications (Optional) - Work in progress

   ```
   <application ...>
   <receiver android:name="com.afrihost.firebase.notifications.RNFirebaseNotificationReceiver"/>
   <receiver android:enabled="true" android:exported="true"  android:name="com.afrihost.firebase.notifications.RNFirebaseNotificationsRebootReceiver">
      <intent-filter>
      <action android:name="android.intent.action.BOOT_COMPLETED"/>
      <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
      <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
      <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
   </receiver>
   </application>
   ```
8. Run `$ npx react-native run-android` to test if everything is working

## 🔧 iOS Setup

### Add firebase credentials

The Firebase console provides a `GoogleService-Info.plist` file containing a set of credentials for iOS devices to use when authenticating with your Firebase project.

### Setup Credentials

1.  Select your firebase iOS project
2.  Select the iOS icon that will open the configuration section
    1. Fill in the required information
    2. Download the `GoogleService-Info.plist` then, Move the GoogleService-Info.plist file that you just downloaded into the root of your Xcode project and add it to all targets.
    3. Add the firebase SDK if you are **not** using PODS.
3.  Add the following in AppDelegate

    ```
    #import "AppDelegate.h"
    ....
    #import "Firebase.h" <--- Add this
    #import "RNFirebaseMessaging.h" <--- Add this
    #import "FirebasePushNotifications.h" <--- Add this

            @implementation AppDelegate

            - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
            {
            [FIRApp configure]; <--Add this
            [FirebasePushNotifications configure]; <--Add this
            ..........
            ..........
            return YES;
            }
    ```
4.  Follow the cert instructions here: <https://firebase.google.com/docs/cloud-messaging/ios/certs>
    1. Go to the firebase console
    2. select the iOS app (settings)
    3. click cloud messaging
    4. Upload the Authentication Key and fill in the detatils
5.  In Xcode, enable the following capabilities:
    1. Push Notifications
    2. Background modes > Remote notifications
6.  Add the following code to `AppDelegate.m`

    1. To recieve the notifications

    ```
    - (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    [[FirebasePushNotifications instance] didReceiveLocalNotification:notification];
    }
    ```

    2. optional for remote notifications

    ```
    #import "RNFirebaseMessaging.h"
    .....
    .....
    - (void)application:(UIApplication *)application didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo
                                                        fetchCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler{
    [[FirebasePushNotifications instance] didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
    }

    - (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings {

    [[RNFirebaseMessaging instance] didRegisterUserNotificationSettings:notificationSettings];
    }
    ```
7.  Run `$ npx react-native run-ios` to confirm the app communicates with firebase (You may need to uninstall and reinstall your app.)

⚠️ Notifications will only come through on a real physical device - this is a limitation set by Apple.

⚠️ You can only recieve the messages / notifications on iOS if you have permission - ensure you request and have the permission first.

## ✅ Usage

```javascript
import { notifications, NotificationMessage, Android } from 'react-native-firebase-push-notifications'

  getToken = async () => {
    //get the messeging token
    const token = await notifications.getToken()
    //you can also call messages.getToken() (does the same thing)
    return token
  }
  getInitialNotification = async () => {
    //get the initial token (triggered when app opens from a closed state)
    const notification = await notifications.getInitialNotification()
    console.log("getInitialNotification", notification)
    return notification
  }

  onNotificationOpenedListener = () => {
    //remember to remove the listener on un mount
    //this gets triggered when the application is in the background
    this.removeOnNotificationOpened = notifications.onNotificationOpened(
      notification => {
        console.log("onNotificationOpened", notification)
        //do something with the notification
      }
    )
  }

  onNotificationListener = () => {
    //remember to remove the listener on un mount
    //this gets triggered when the application is in the forground/runnning
    //for android make sure you manifest is setup - else this wont work
    //Android will not have any info set on the notification properties (title, subtitle, etc..), but _data will still contain information
    this.removeOnNotification = notifications.onNotification(notification => {
      //do something with the notification
      console.log("onNotification", notification)
    })
  }

  onTokenRefreshListener = () => {
    //remember to remove the listener on un mount
    //this gets triggered when a new token is generated for the user
    this.removeonTokenRefresh = messages.onTokenRefresh(token => {
      //do something with the new token
    })
  }
  setBadge = async number => {
    //only works on iOS and some Android Devices
    return await notifications.setBadge(number)
  }

  getBadge = async () => {
    //only works on iOS and some Android Devices
    return await notifications.getBadge()
  }

  hasPermission = async () => {
    //only works on iOS
    return await notifications.hasPermission()
    //or     return await messages.hasPermission()
  }

  requestPermission = async () => {
    //only works on iOS
    return await notifications.requestPermission()
    //or     return await messages.requestPermission()
  }

localNotification = async () => {
  //required for Android
  const channel = new Android.Channel(
    "test-channel",
    "Test Channel",
    Android.Importance.Max
  ).setDescription("My apps test channel")

  // for android create the channel
  notifications.android().createChannel(channel)
  await notifications.displayNotification(
    new NotificationMessage()
      .setNotificationId("notification-id")
      .setTitle("Notification title")
      .setBody("Notification body")
      .setData({
        key1: "key1",
        key2: "key2",
      })
      .android.setChannelId("test-channel") //required for android
  )
}


    componentWillUnmount() {
    //remove the listener on unmount
    if (this.removeOnNotificationOpened) {
      this.removeOnNotificationOpened()
    }
    if (this.removeOnNotification) {
      this.removeOnNotification()
    }

    if (this.removeonTokenRefresh) {
      this.removeonTokenRefresh()
    }
  }

```

Check out the sample app <https://github.com/afrihost/firebasePushTest>
