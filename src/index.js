import { NativeModules, Platform, NativeEventEmitter } from "react-native"
import Notification from "./notifications/Notification"
import AndroidNotifications from "./notifications/AndroidNotifications"
import IOSNotifications from "./notifications/IOSNotifications"
import EventEmitter from "react-native/Libraries/vendor/emitter/EventEmitter"
import _ from "lodash"

const { FirebaseNotifications } = NativeModules
const { RNFirebaseMessaging } = NativeModules

const NATIVE_EVENTS = [
  "notifications_notification_displayed",
  "notifications_notification_opened",
  "notifications_notification_received"
]

class Notifications extends NativeEventEmitter {
  _android: AndroidNotifications

  _ios: IOSNotifications

  constructor() {
    super(FirebaseNotifications)
    
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

    removeOnNotificationReceived = this.addListener(
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

  get android(): AndroidNotifications {
    return this._android;
  }

  get ios(): IOSNotifications {
    return this._ios;
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
      this.localEventEmitter.removeListener("onNotificationOpened", listener)
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
    const initialNotification = await FirebaseNotifications.getInitialNotification()
    if (_.has(initialNotification, "notification")) {
      return {
        action: initialNotification.action,
        notification: new Notification(initialNotification.notification, this),
        results: initialNotification.results
      }
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
}

export const notifications = new Notifications()
export const messages = new Messaging()
export const NotificationMessage = Notification

//export default FirebaseNotifications
