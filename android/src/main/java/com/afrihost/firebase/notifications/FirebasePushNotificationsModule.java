
package com.afrihost.firebase.notifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.afrihost.firebase.notifications.Utils.getResId;


public class FirebasePushNotificationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final String BADGE_FILE = "BadgeCountFile";
    private static final String BADGE_KEY = "BadgeCount";
    private static final String TAG = "FirebaseNotifications";

    private SharedPreferences sharedPreferences = null;

    private RNFirebaseNotificationManager notificationManager;

    FirebasePushNotificationsModule(ReactApplicationContext context) {
        super(context);
        context.addActivityEventListener(this);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

        // Subscribe to message events
        localBroadcastManager.registerReceiver(
                new MessageReceiver(),
                new IntentFilter(RNFirebaseMessagingService.MESSAGE_EVENT)
        );

        // Subscribe to new token events
        localBroadcastManager.registerReceiver(
                new RefreshTokenReceiver(),
                new IntentFilter(RNFirebaseMessagingService.NEW_TOKEN_EVENT)
        );


        notificationManager = new RNFirebaseNotificationManager(context);
        sharedPreferences = context.getSharedPreferences(BADGE_FILE, Context.MODE_PRIVATE);



        // Subscribe to remote notification events
        localBroadcastManager.registerReceiver(
                new FirebasePushNotificationsModule.RemoteNotificationReceiver(),
                new IntentFilter(RNFirebaseMessagingService.REMOTE_NOTIFICATION_EVENT)
        );

        // Subscribe to scheduled notification events
        localBroadcastManager.registerReceiver(
                new FirebasePushNotificationsModule.ScheduledNotificationReceiver(),
                new IntentFilter(RNFirebaseNotificationManager.SCHEDULED_NOTIFICATION_EVENT)
        );
    }

    @Override
    public String getName() {
        return "FirebaseNotifications";
    }

    

    @ReactMethod
    public void getToken(final Promise promise) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    promise.resolve(token);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            promise.reject("messaging/fcm-token-error", e.getMessage());
        }
    }

    @ReactMethod
    public void deleteToken(Promise promise) {
        try {
            String senderId = FirebaseApp.getInstance().getOptions().getGcmSenderId();
            FirebaseMessaging.getInstance().deleteToken();
            promise.resolve(null);
        } catch (Throwable e) {
            e.printStackTrace();
            promise.reject("messaging/fcm-token-error", e.getMessage());
        }
    }

    @ReactMethod
    public void requestPermission(Promise promise) {
        promise.resolve(null);
    }

    // Non Web SDK methods
    @ReactMethod
    public void hasPermission(Promise promise) {
        Boolean enabled = NotificationManagerCompat
                .from(getReactApplicationContext())
                .areNotificationsEnabled();
        promise.resolve(enabled);
    }

    @ReactMethod
    public void sendMessage(ReadableMap messageMap, Promise promise) {
        if (!messageMap.hasKey("to")) {
            promise.reject("messaging/invalid-message", "The supplied message is missing a 'to' field");
            return;
        }

        RemoteMessage.Builder mb = new RemoteMessage.Builder(messageMap.getString("to"));

        if (messageMap.hasKey("collapseKey")) {
            mb = mb.setCollapseKey(messageMap.getString("collapseKey"));
        }
        if (messageMap.hasKey("messageId")) {
            mb = mb.setMessageId(messageMap.getString("messageId"));
        }
        if (messageMap.hasKey("messageType")) {
            mb = mb.setMessageType(messageMap.getString("messageType"));
        }
        if (messageMap.hasKey("ttl")) {
            mb = mb.setTtl(messageMap.getInt("ttl"));
        }
        if (messageMap.hasKey("data")) {
            ReadableMap dataMap = messageMap.getMap("data");
            ReadableMapKeySetIterator iterator = dataMap.keySetIterator();
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                mb = mb.addData(key, dataMap.getString(key));
            }
        }

        FirebaseMessaging.getInstance().send(mb.build());

        // TODO: Listen to onMessageSent and onSendError for better feedback?
        promise.resolve(null);
    }

    @ReactMethod
    public void subscribeToTopic(String topic, final Promise promise) {
        FirebaseMessaging
                .getInstance()
                .subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@Nonnull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "subscribeToTopic:onComplete:success");
                            promise.resolve(null);
                        } else {
                            Exception exception = task.getException();
                            Log.e(TAG, "subscribeToTopic:onComplete:failure", exception);
                            promise.reject(exception);
                        }
                    }
                });
    }

    @ReactMethod
    public void unsubscribeFromTopic(String topic, final Promise promise) {
        FirebaseMessaging
                .getInstance()
                .unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@Nonnull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "unsubscribeFromTopic:onComplete:success");
                            promise.resolve(null);
                        } else {
                            Exception exception = task.getException();
                            Log.e(TAG, "unsubscribeFromTopic:onComplete:failure", exception);
                            promise.reject(exception);
                        }
                    }
                });
    }

    @ReactMethod
    public void addListener(String eventName) {
        // For RN built in Event Emitter Calls.
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // For RN built in Event Emitter Calls.
    }


    @ReactMethod
    public void cancelAllNotifications(Promise promise) {
        notificationManager.cancelAllNotifications(promise);
    }

    @ReactMethod
    public void cancelNotification(String notificationId, Promise promise) {
        notificationManager.cancelNotification(notificationId, promise);
    }

    @ReactMethod
    public void displayNotification(ReadableMap notification, Promise promise) {
        notificationManager.displayNotification(notification, promise);
    }

    @ReactMethod
    public void getBadge(Promise promise) {
        int badge = sharedPreferences.getInt(BADGE_KEY, 0);
        Log.d(TAG, "Got badge count: " + badge);
        promise.resolve(badge);
    }

    @ReactMethod
    public void getInitialNotification(Promise promise) {
        WritableMap notificationOpenMap = null;
        if (getCurrentActivity() != null) {
            notificationOpenMap = parseIntentForNotification(getCurrentActivity().getIntent());
        }
        promise.resolve(notificationOpenMap);
    }

    @ReactMethod
    public void getScheduledNotifications(Promise promise) {
        ArrayList<Bundle> bundles = notificationManager.getScheduledNotifications();
        WritableArray array = Arguments.createArray();
        for (Bundle bundle : bundles) {
            array.pushMap(parseNotificationBundle(bundle));
        }
        promise.resolve(array);
    }

    @ReactMethod
    public void removeAllDeliveredNotifications(Promise promise) {
        notificationManager.removeAllDeliveredNotifications(promise);
    }

    @ReactMethod
    public void removeDeliveredNotification(String notificationId, Promise promise) {
        notificationManager.removeDeliveredNotification(notificationId, promise);
    }

    @ReactMethod
    public void removeDeliveredNotificationsByTag(String tag, Promise promise) {
        notificationManager.removeDeliveredNotificationsByTag(tag, promise);
    }

    @ReactMethod
    public void setBadge(int badge, Promise promise) {
        // Store the badge count for later retrieval
        sharedPreferences
                .edit()
                .putInt(BADGE_KEY, badge)
                .apply();
        if (badge == 0) {
            Log.d(TAG, "Remove badge count");
            ShortcutBadger.removeCount(this.getReactApplicationContext());
        } else {
            Log.d(TAG, "Apply badge count: " + badge);
            ShortcutBadger.applyCount(this.getReactApplicationContext(), badge);
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void scheduleNotification(ReadableMap notification, Promise promise) {
        notificationManager.scheduleNotification(notification, promise);
    }

    //////////////////////////////////////////////////////////////////////
    // Start Android specific methods
    //////////////////////////////////////////////////////////////////////
    @ReactMethod
    public void createChannel(ReadableMap channelMap, Promise promise) {
        try {
            notificationManager.createChannel(channelMap);
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void createChannelGroup(ReadableMap channelGroupMap, Promise promise) {
        try {
            notificationManager.createChannelGroup(channelGroupMap);
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void createChannelGroups(ReadableArray channelGroupsArray, Promise promise) {
        try {
            notificationManager.createChannelGroups(channelGroupsArray);
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void createChannels(ReadableArray channelsArray, Promise promise) {
        try {
            notificationManager.createChannels(channelsArray);
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void deleteChannelGroup(String channelId, Promise promise) {
        try {
            notificationManager.deleteChannelGroup(channelId);
            promise.resolve(null);
        } catch (NullPointerException e) {
            promise.reject(
                    "notifications/channel-group-not-found",
                    "The requested NotificationChannelGroup does not exist, have you created it?"
            );
        }
    }

    @ReactMethod
    public void deleteChannel(String channelId, Promise promise) {
        try {
            notificationManager.deleteChannel(channelId);
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void getChannel(String channelId, Promise promise) {
        try {
            promise.resolve(notificationManager.getChannel(channelId));
            return;
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void getChannels(Promise promise) {
        try {
            promise.resolve(notificationManager.getChannels());
            return;
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(Collections.emptyList());
    }

    @ReactMethod
    public void getChannelGroup(String channelGroupId, Promise promise) {
        try {
            promise.resolve(notificationManager.getChannelGroup(channelGroupId));
            return;
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void getChannelGroups(Promise promise) {
        try {
            promise.resolve(notificationManager.getChannelGroups());
            return;
        } catch (Throwable t) {
            // do nothing - most likely a NoSuchMethodError for < v4 support lib
        }
        promise.resolve(Collections.emptyList());
    }
    //////////////////////////////////////////////////////////////////////
    // End Android specific methods
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    // Start ActivityEventListener methods
    //////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // FCM functionality does not need this function
    }

    @Override
    public void onNewIntent(Intent intent) {
        WritableMap notificationOpenMap = parseIntentForNotification(intent);
        if (notificationOpenMap != null) {
            Utils.sendEvent(
                    getReactApplicationContext(),
                    "notifications_notification_opened",
                    notificationOpenMap
            );
        }
    }

    //////////////////////////////////////////////////////////////////////
    // End ActivityEventListener methods
    //////////////////////////////////////////////////////////////////////

    private WritableMap parseIntentForNotification(Intent intent) {
        WritableMap notificationOpenMap = parseIntentForRemoteNotification(intent);
        if (notificationOpenMap == null) {
            notificationOpenMap = parseIntentForLocalNotification(intent);
        }
        return notificationOpenMap;
    }

    private WritableMap parseIntentForLocalNotification(Intent intent) {
        if (intent.getExtras() == null || !intent.hasExtra("notificationId")) {
            return null;
        }

        WritableMap notificationMap = Arguments.makeNativeMap(intent.getExtras());
        WritableMap notificationOpenMap = Arguments.createMap();
        notificationOpenMap.putString("action", intent.getAction());
        notificationOpenMap.putMap("notification", notificationMap);

        // Check for remote input results
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            notificationOpenMap.putMap("results", Arguments.makeNativeMap(remoteInput));
        }

        return notificationOpenMap;
    }

    private WritableMap parseIntentForRemoteNotification(Intent intent) {
        // Check if FCM data exists
        if (intent.getExtras() == null || !intent.hasExtra("google.message_id")) {
            return null;
        }

        Bundle extras = intent.getExtras();

        WritableMap notificationMap = Arguments.createMap();
        WritableMap dataMap = Arguments.createMap();

        for (String key : extras.keySet()) {
            if (key.equals("google.message_id")) {
                notificationMap.putString("notificationId", extras.getString(key));
            } else if (key.equals("collapse_key")
                    || key.equals("from")
                    || key.equals("google.sent_time")
                    || key.equals("google.ttl")
                    || key.equals("_fbSourceApplicationHasBeenSet")) {
                // ignore known unneeded fields
            } else {
                dataMap.putString(key, extras.getString(key));
            }
        }
        notificationMap.putMap("data", dataMap);

        WritableMap notificationOpenMap = Arguments.createMap();
        notificationOpenMap.putString("action", intent.getAction());
        notificationOpenMap.putMap("notification", notificationMap);

        return notificationOpenMap;
    }

    private WritableMap parseNotificationBundle(Bundle notification) {
        return Arguments.makeNativeMap(notification);
    }

    private WritableMap parseRemoteMessage(RemoteMessage message) {
        RemoteMessage.Notification notification = message.getNotification();

        WritableMap notificationMap = Arguments.createMap();
        WritableMap dataMap = Arguments.createMap();

        // Cross platform notification properties
        String body = getNotificationBody(notification);
        if (body != null) {
            notificationMap.putString("body", body);
        }
        if (message.getData() != null) {
            for (Map.Entry<String, String> e : message
                    .getData()
                    .entrySet()) {
                dataMap.putString(e.getKey(), e.getValue());
            }
        }
        notificationMap.putMap("data", dataMap);
        if (message.getMessageId() != null) {
            notificationMap.putString("notificationId", message.getMessageId());
        }
        if (notification.getSound() != null) {
            notificationMap.putString("sound", notification.getSound());
        }
        String title = getNotificationTitle(notification);
        if (title != null) {
            notificationMap.putString("title", title);
        }

        // Android specific notification properties
        WritableMap androidMap = Arguments.createMap();
        if (notification.getClickAction() != null) {
            androidMap.putString("clickAction", notification.getClickAction());
        }
        if (notification.getColor() != null) {
            androidMap.putString("color", notification.getColor());
        }
        if (notification.getIcon() != null) {
            WritableMap iconMap = Arguments.createMap();
            iconMap.putString("icon", notification.getIcon());
            androidMap.putMap("smallIcon", iconMap);
        }
        if (notification.getImageUrl() != null) {
            String imageUrl = notification.getImageUrl().toString();
            WritableMap bigPictureMap = Arguments.createMap();
            bigPictureMap.putString("picture", imageUrl);
            bigPictureMap.putNull("largeIcon");
            androidMap.putMap("bigPicture", bigPictureMap);
            androidMap.putString("largeIcon", imageUrl);
        }
        if (notification.getTag() != null) {
            androidMap.putString("group", notification.getTag());
            androidMap.putString("tag", notification.getTag());
        }
        if (notification.getChannelId() != null) {
            androidMap.putString("channelId", notification.getChannelId());
        }
        notificationMap.putMap("android", androidMap);

        return notificationMap;
    }

    private @Nullable
    String getNotificationBody(RemoteMessage.Notification notification) {
        String body = notification.getBody();
        String bodyLocKey = notification.getBodyLocalizationKey();
        if (bodyLocKey != null) {
            String[] bodyLocArgs = notification.getBodyLocalizationArgs();
            Context ctx = getReactApplicationContext();
            int resId = getResId(ctx, bodyLocKey);
            return ctx
                    .getResources()
                    .getString(resId, (Object[]) bodyLocArgs);
        } else {
            return body;
        }
    }

    private @Nullable
    String getNotificationTitle(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String titleLocKey = notification.getTitleLocalizationKey();
        if (titleLocKey != null) {
            String[] titleLocArgs = notification.getTitleLocalizationArgs();
            Context ctx = getReactApplicationContext();
            int resId = getResId(ctx, titleLocKey);
            return ctx
                    .getResources()
                    .getString(resId, (Object[]) titleLocArgs);
        } else {
            return title;
        }
    }


    private class RemoteNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                Log.d(TAG, "Received new remote notification");

                RemoteMessage message = intent.getParcelableExtra("notification");
                WritableMap messageMap = parseRemoteMessage(message);

                Utils.sendEvent(
                        getReactApplicationContext(),
                        "notifications_notification_received",
                        messageMap
                );
            }
        }
    }

    private class ScheduledNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                Log.d(TAG, "Received new scheduled notification");

                Bundle notification = intent.getBundleExtra("notification");
                WritableMap messageMap = parseNotificationBundle(notification);

                Utils.sendEvent(
                        getReactApplicationContext(),
                        "notifications_notification_received",
                        messageMap
                );
            }
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                Log.d(TAG, "Received new message");

                RemoteMessage message = intent.getParcelableExtra("message");
                WritableMap messageMap = MessagingSerializer.parseRemoteMessage(message);

                Utils.sendEvent(getReactApplicationContext(), "messaging_message_received", messageMap);
            }
        }
    }

    private class RefreshTokenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                Log.d(TAG, "Received new messaging token.");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                String token = null;
                                if (!task.isSuccessful()) {
                                    return;
                                }

                                // Get new FCM registration token
                                token = task.getResult();

                                if (token != null) {
                                    Log.d(TAG, "Sending new messaging token event.");
                                    Utils.sendEvent(getReactApplicationContext(), "messaging_token_refreshed", token);
                                }
                            }
                        });
                    }
                });

                thread.start();
            }
        }
    }
}