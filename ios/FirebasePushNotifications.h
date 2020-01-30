#ifndef FirebasePushNotifications_h
#define FirebasePushNotifications_h
#import <Foundation/Foundation.h>

#if __has_include(<FirebaseMessaging/FirebaseMessaging.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface FirebasePushNotifications : RCTEventEmitter<RCTBridgeModule>

+ (void)configure;
+ (_Nonnull instancetype)instance;

#if !TARGET_OS_TV
- (void)didReceiveLocalNotification:(nonnull UILocalNotification *)notification;
- (void)didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo fetchCompletionHandler:(void (^_Nonnull)(UIBackgroundFetchResult))completionHandler;
#endif

@end

#else
@interface FirebasePushNotifications : NSObject
@end
#endif

#endif
