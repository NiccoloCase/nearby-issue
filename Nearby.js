import {
  NativeModules,
  Platform,
  PermissionsAndroid,
  NativeEventEmitter,
} from 'react-native';

const isAndroid = Platform.OS == 'android';

/**
 * Inizzia la pubblicazione (anche in background) e lo scanning dei messaggi
 * @param {string} message
 */
const start = message => {
  if (isAndroid) {
    NativeModules.MyNativeModule.start();
    NativeModules.MyNativeModule.startActivity(message);
  }
};

/**
 * Interrompe l'attività di pubblicazione e scanning
 */
const stop = () => {
  if (isAndroid) NativeModules.MyNativeModule.stop();
};

/**
 * Verifica se il servizizio di pubblicazione/scanning è attivo
 * @returns Restituisce una promessa
 */
const isActive = () => {
  return new Promise((resolve, reject) => {
    if (isAndroid) {
      NativeModules.MyNativeModule.isActivityRunning(res => {
        resolve(res);
      });
    } else {
      resolve(false);
    }
  });
};

/**
 * Inizzializza nearby e i permessi
 */
const init = () => {
  if (isAndroid) {
    return new Promise(resolve => {
      PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
        PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_ADVERTISE,
        PermissionsAndroid.PERMISSIONS.NEARBY_WIFI_DEVICES,
      ]).then(result => {
        const isGranted =
          result['android.permission.BLUETOOTH_CONNECT'] ===
            PermissionsAndroid.RESULTS.GRANTED &&
          result['android.permission.BLUETOOTH_SCAN'] ===
            PermissionsAndroid.RESULTS.GRANTED &&
          result['android.permission.ACCESS_FINE_LOCATION'] ===
            PermissionsAndroid.RESULTS.GRANTED;

        console.log({isGranted});

        NativeModules.MyNativeModule.initNearby(isPlayServicesAvailable => {
          console.log({isPlayServicesAvailable});
          if (isPlayServicesAvailable) {
            resolve(true);
            return;
          } else {
            resolve(false);
          }
        });
      });
    });
  }
};

const registerToEvents = (
  onMessageFound,
  onMessageLost,
  onActivityStart,
  onActivityStop,
) => {
  const emitters = [];

  const eventEmitter = new NativeEventEmitter();
  emitters.push(
    eventEmitter.addListener('onMessageFound', onMessageFound),
    eventEmitter.addListener('onMessageLost', onMessageLost),
    eventEmitter.addListener('onActivityStart', onActivityStart),
    eventEmitter.addListener('onActivityStop', onActivityStop),
  );

  return () => {
    emitters.forEach(emitter => emitter.remove());
  };
};

export default {
  init,
  start,
  stop,
  isActive,
  registerToEvents,
};
