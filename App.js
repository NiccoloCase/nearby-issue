import React from 'react';
import {
  Button,
  NativeModules,
  SafeAreaView,
  StyleSheet,
  Platform,
} from 'react-native';

const App = () => {
  function onPress() {
    NativeModules.MyNativeModule.initNearby(isPlayServicesAvailable => {
      console.log({isPlayServicesAvailable});

      if (isPlayServicesAvailable) {
        NativeModules.MyNativeModule.start();
        console.log('started');
      }
    });
  }

  return (
    <SafeAreaView style={{flex: 1}}>
      <Button title="Start Scanning" onPress={onPress} />
    </SafeAreaView>
  );
};

export default App;
