import { NativeModules, Platform, NativeEventEmitter } from "react-native"
import Notification from "./notifications/Notification"
import AndroidNotifications from "./notifications/AndroidNotifications"
import IOSNotifications from "./notifications/IOSNotifications"

import _ from "lodash"

const { FirebaseNotifications } = NativeModules

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
    const eventEmitter = new NativeEventEmitter(FirebaseNotifications)
    removeOnNotificationOpened = eventEmitter.addListener(
      "notifications_notification_opened",
      event => {
        this.emit(
          "onNotificationOpened",
          new Notification(event.notification, this)
        )
      }
    )

    removeOnNotificationReceived = eventEmitter.addListener(
      "notifications_notification_received",
      event => {
        this.emit("onNotification", new Notification(event, this))
      }
    )

    if (Platform.OS === "ios") {
      FirebaseNotifications.jsInitialised()
    }
  }

  onNotificationOpened = nextOrObserver => {
    console.log("onNotificationOpened")
    let listener
    if (_.isFunction(nextOrObserver)) {
      console.log("nextOrObserver", nextOrObserver)
      listener = nextOrObserver
    } else if (isObject(nextOrObserver) && _.isFunction(nextOrObserver.next)) {
      listener = nextOrObserver.next
      console.log("listener = nextOrObserver.next", nextOrObserver)
    } else {
      throw new Error(
        "Notifications.onNotificationOpened failed: First argument must be a function or observer object with a `next` function."
      )
    }

    this.addListener("onNotificationOpened", listener)

    return () => {
      this.removeListener("onNotificationOpened", listener)
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
    this.addListener("onNotification", listener)

    return () => {
      this.removeListener("onNotification", listener)
    }
  }

  getToken = () => {
    return FirebaseNotifications.getToken()
  }

  getInitialNotification = () => {
    return FirebaseNotifications.getInitialNotification().then(
      notificationOpen => {
        if (notificationOpen) {
          return {
            action: notificationOpen.action,
            notification: new Notification(notificationOpen.notification, this),
            results: notificationOpen.results
          }
        }
        return null
      }
    )
  }
}

export const notifications = new Notifications()
export const NotificationMessage = Notification

//export default FirebaseNotifications
