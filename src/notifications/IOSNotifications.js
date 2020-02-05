import { NativeModules } from 'react-native'
const { FirebaseNotifications } = NativeModules
import type Notifications from './';

export type BackgroundFetchResultValue = string;
type BackgroundFetchResult = {
  noData: BackgroundFetchResultValue,
  newData: BackgroundFetchResultValue,
  failure: BackgroundFetchResultValue,
};

export default class IOSNotifications {
  _backgroundFetchResult: BackgroundFetchResult;

  shouldAutoComplete: boolean;

  constructor(notifications: Notifications) {
    this.shouldAutoComplete = true;

    const nativeModule = FirebaseNotifications;
    this._backgroundFetchResult = {
      noData: nativeModule.backgroundFetchResultNoData,
      newData: nativeModule.backgroundFetchResultNewData,
      failure: nativeModule.backgroundFetchResultFailed,
    };
  }

  get backgroundFetchResult(): BackgroundFetchResult {
    return { ...this._backgroundFetchResult };
  }
}
