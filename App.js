import React, {useEffect, useState} from 'react';
import {
  Button,
  NativeModules,
  SafeAreaView,
  View,
  PermissionsAndroid,
  NativeEventEmitter,
  Text,
  ScrollView,
} from 'react-native';

import {getDeviceName} from 'react-native-device-info';

const App = () => {
  const [deviceName, setDeviceName] = useState('');
  const [devices, setDevices] = useState([]);

  useEffect(() => {
    getDeviceName().then(setDeviceName);

    const eventEmitter = new NativeEventEmitter(NativeModules.MyNativeModule);
    const eventListener = eventEmitter.addListener('onMessageFound', event => {
      console.log(
        `[${deviceName}]: messaggio ricevuto dal dispositivo "${event.message}"`,
      );
      setDevices(d => [...d, event.message]);
    });

    const eventListener2 = eventEmitter.addListener('onMessageLost', event => {
      console.log(
        `[${deviceName}]: messaggio perso dal dispositivo "${event.message}"`,
      );
    });
  }, []);

  function onPressScan() {
    PermissionsAndroid.requestMultiple([
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
      PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION,
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
      PermissionsAndroid.PERMISSIONS.BLUETOOTH_ADVERTISE,
      PermissionsAndroid.PERMISSIONS.NEARBY_WIFI_DEVICES,
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
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
          NativeModules.MyNativeModule.start();
          console.log('started');
        }
      });
    });
  }

  const onPressSend = () => {
    NativeModules.MyNativeModule.send(deviceName);
  };

  const onPressStop = () => {
    NativeModules.MyNativeModule.stop();
  };

  const onPressActivity = () => {
    NativeModules.MyNativeModule.startActivity();
  };

  return (
    <SafeAreaView style={{flex: 1}}>
      <Text
        style={{
          fontSize: 30,
          backgroundColor: 'green',
          margin: 20,
          padding: 10,
        }}>
        Nome del dispositivo: {deviceName}
      </Text>
      <View style={{marginVertical: 10}}>
        <Button title="Start Scanning" onPress={onPressScan} />
      </View>
      <View style={{marginVertical: 10}}>
        <Button title="Send" onPress={onPressSend} />
      </View>
      <View style={{marginVertical: 10}}>
        <Button title="Stop" onPress={onPressStop} />
      </View>
      <View style={{marginVertical: 10}}>
        <Button title="Start Activity" onPress={onPressActivity} />
      </View>
      <ScrollView style={{marginTop: 60}}>
        {devices.map(x => {
          return (
            <View style={{backgroundColor: 'blue', marginVertical: 10}}>
              <Text style={{fontSize: 30}}>{x}</Text>
            </View>
          );
        })}
      </ScrollView>
    </SafeAreaView>
  );
};

export default App;
