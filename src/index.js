import _ from "lodash"
import { NativeEventEmitter, NativeModules, Platform } from "react-native"
import EventEmitter from "react-native/Libraries/vendor/emitter/EventEmitter"
import AndroidAction from "./notifications/AndroidAction"
import AndroidChannel from "./notifications/AndroidChannel"
import AndroidChannelGroup from "./notifications/AndroidChannelGroup"
import AndroidNotifications from "./notifications/AndroidNotifications"
import AndroidRemoteInput from "./notifications/AndroidRemoteInput"
import IOSNotifications from "./notifications/IOSNotifications"
import Notification from "./notifications/Notification"
import {
  BadgeIconType,
  Category,
  Defaults,
  GroupAlert,
  Importance,
  Priority,
  SemanticAction,
  Visibility
} from "./notifications/types"

const { FirebaseNotifications } = NativeModules
const { RNFirebaseMessaging } = NativeModules

const NATIVE_EVENTS = [
  "notifications_notification_displayed",
  "notifications_notification_opened",
  "notifications_notification_received"
]

class Notifications extends NativeEventEmitter {
  constructor() {
    super(FirebaseNotifications)
    this.AndroidNotifications = new AndroidNotifications()
    this.IOSNotifications = new IOSNotifications()
    this.localEventEmitter = new EventEmitter()
    this.removeOnNotificationOpened = this.addListener(
      "notifications_notification_opened",
      event => {
        this.localEventEmitter.emit(
          "onNotificationOpened",
          new Notification(event.notification, this)
        )
      }
    )

    this.removeOnNotificationReceived = this.addListener(
      "notifications_notification_received",
      event => {
        this.localEventEmitter.emit(
          "onNotification",
          new Notification(event, this)
        )
      }
    )

    if (Platform.OS === "ios") {
      FirebaseNotifications.jsInitialised()
    }
  }

  android() {
    return this.AndroidNotifications
  }

  ios() {
    return this.IOSNotifications
  }

  displayNotification = async notification => {
    return await FirebaseNotifications.displayNotification(notification.build())
  }

  onNotificationOpened = nextOrObserver => {
    let listener
    if (_.isFunction(nextOrObserver)) {
      listener = nextOrObserver
    } else if (isObject(nextOrObserver) && _.isFunction(nextOrObserver.next)) {
      listener = nextOrObserver.next
    } else {
      throw new Error(
        "Notifications.onNotificationOpened failed: First argument must be a function or observer object with a `next` function."
      )
    }

    this.localEventEmitter.addListener("onNotificationOpened", listener)

    return () => {
      this.localEventEmitter.removeAllListeners("onNotificationOpened")
    }
  }

  onNotification = nextOrObserver => {
    let listener
    if (_.isFunction(nextOrObserver)) {
      listener = nextOrObserver
    } else if (isObject(nextOrObserver) && _.isFunction(nextOrObserver.next)) {
      listener = nextOrObserver.next
    } else {
      throw new Error(
        "Notifications.onNotification failed: First argument must be a function or observer object with a `next` function."
      )
    }
    this.localEventEmitter.addListener("onNotification", listener)

    return () => {
      this.localEventEmitter.removeListener("onNotification", listener)
    }
  }

  getToken = () => {
    return FirebaseNotifications.getToken()
  }

  getInitialNotification = async () => {
    const initialNotification =
      await FirebaseNotifications.getInitialNotification()
    if (_.has(initialNotification, "notification")) {
      return {
        action: initialNotification.action,
        notification: new Notification(initialNotification.notification, this),
        results: initialNotification.results
      }
    }
    return null
  }

  getBadge = () => {
    return FirebaseNotifications.getBadge()
  }

  setBadge = async num => {
    return await FirebaseNotifications.setBadge(num)
  }

  requestPermission = async () => {
    if (Platform.OS === "ios") {
      return await RNFirebaseMessaging.requestPermission()
    }
    return null
  }

  hasPermission = async () => {
    if (Platform.OS === "ios") {
      return await RNFirebaseMessaging.hasPermission()
    }

    return null
  }
}

class Messaging extends NativeEventEmitter {
  constructor() {
    super(RNFirebaseMessaging)
    this.localEventEmitter = new EventEmitter()

    removeMessageTokenRefreshed = this.addListener(
      "messaging_token_refreshed",
      event => {
        this.localEventEmitter.emit("onTokenRefresh", event)
      }
    )
  }

  onTokenRefresh = nextOrObserver => {
    let listener
    if (_.isFunction(nextOrObserver)) {
      listener = nextOrObserver
    } else if (isObject(nextOrObserver) && _.isFunction(nextOrObserver.next)) {
      listener = nextOrObserver.next
    } else {
      throw new Error(
        "Notifications.onTokenRefresh failed: First argument must be a function or observer object with a `next` function."
      )
    }
    this.localEventEmitter.addListener("onTokenRefresh", listener)

    return () => {
      this.localEventEmitter.removeListener("onTokenRefresh", listener)
    }
  }

  getToken = () => {
    return RNFirebaseMessaging.getToken()
  }

  requestPermission = async () => {
    return await RNFirebaseMessaging.requestPermission()
  }

  hasPermission = async () => {
    return await RNFirebaseMessaging.hasPermission()
  }
}

export const notifications = new Notifications()
export const messages = new Messaging()
export const NotificationMessage = Notification
export const Android = {
  Action: AndroidAction,
  BadgeIconType: BadgeIconType,
  Category: Category,
  Channel: AndroidChannel,
  ChannelGroup: AndroidChannelGroup,
  Defaults: Defaults,
  GroupAlert: GroupAlert,
  Importance: Importance,
  Priority: Priority,
  RemoteInput: AndroidRemoteInput,
  SemanticAction: SemanticAction,
  Visibility: Visibility
}
//export default FirebaseNotifications
