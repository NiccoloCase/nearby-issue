package com.native1; // replace com.your-app-name with your app’s name

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;

import java.util.Map;
import java.util.HashMap;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;

// eventi
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.bluetooth.BluetoothAdapter;

public class MyNativeModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;
    private Message message;
    private MessageListener messageListener;
    private Intent serviceIntent;

    MyNativeModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @Override
    public String getName() {
        return "MyNativeModule";
    }

    @ReactMethod
    public void isAvailable(Callback callBack) {
        boolean isGooglePlayServicesAvailable = isGooglePlayServicesAvailable();
        callBack.invoke(isGooglePlayServicesAvailable);
    }

    @ReactMethod
    public void initNearby(Callback callBack) {
        boolean isGooglePlayServicesAvailable = isGooglePlayServicesAvailable();

        if (isGooglePlayServicesAvailable) {
            this.messageListener = new MessageListener() {
                @Override
                public void onFound(Message message) {
                    Log.d("ReactNative", "Found message: " + new String(message.getContent()));
                    emitMessageEvent("onMessageFound", new String(message.getContent()));
                }

                @Override
                public void onLost(Message message) {
                    Log.d("ReactNative", "Lost sight of message: " + new String(message.getContent()));
                    emitMessageEvent("onMessageLost", new String(message.getContent()));
                }
            };
        }
        callBack.invoke(isGooglePlayServicesAvailable);
    }

    @ReactMethod
    public void start() {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            Nearby.getMessagesClient(currentActivity).subscribe(this.messageListener);
        }
    }

    @ReactMethod
    public void stop() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            if (this.message != null)
                Nearby.getMessagesClient(currentActivity).unpublish(this.message);
            if (this.messageListener != null)
                Nearby.getMessagesClient(currentActivity).unsubscribe(this.messageListener);
            if (this.serviceIntent != null) {
                currentActivity.stopService(this.serviceIntent);
                emitMessageEvent("onActivityStop", "stopped");
                Log.d("ReactNative", "KILLING FOREGROUND SERVICE");
            }
        }

    }

    @ReactMethod
    public void send(String messageText) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            this.message = new Message(messageText.getBytes());
            Nearby.getMessagesClient(currentActivity).publish(this.message);
            Log.d("ReactNative", "sending...");
        }
    }

    @ReactMethod
    public void startActivity(String message) {
        if (!isServiceRunning()) {
            Log.d("ReactNative", "INIZIALIZZAZIONE FOREGROUND SERVICE");

            Activity currentActivity = getCurrentActivity();
            this.serviceIntent = new Intent(currentActivity, MyForegroundService.class);
            Bundle bundle = new Bundle();
            bundle.putString("message", message);
            this.serviceIntent.putExtras(bundle);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentActivity.startForegroundService(this.serviceIntent);
            } else {
                currentActivity.startService(this.serviceIntent);
            }
            emitMessageEvent("onActivityStart", "started");
        }
    }

    @ReactMethod
    public void isActivityRunning(Callback callBack) {
        Boolean running = isServiceRunning();
        callBack.invoke(running);
    }

    private boolean isGooglePlayServicesAvailable() {
        Activity currentActivity = getCurrentActivity();

        final GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        final int availability = googleApi.isGooglePlayServicesAvailable(currentActivity);
        final boolean result = availability == ConnectionResult.SUCCESS;
        if (!result && googleApi.isUserResolvableError(availability)) {
            googleApi.getErrorDialog(currentActivity, availability, 9000).show();
        }
        return result;
    }

    private void emitMessageEvent(String eventName, String message) {
        WritableMap params = Arguments.createMap();
        params.putString("message", message);

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private boolean isServiceRunning() {
        Activity context = getCurrentActivity();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (MyForegroundService.class.getName().equals(service.service.getClassName())) {
                    return service.foreground;
                }
            }
            return false;
        }

        return false;
    }
}