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
import android.util.Log;
import android.app.Activity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;

import android.bluetooth.BluetoothAdapter;

public class MyNativeModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;

    private Message message;
    private MessageListener messageListener;

    MyNativeModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @Override
    public String getName() {
        return "MyNativeModule";
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String sayHi() {
        Log.d("MyNativeModule", "bella");
        return "HI";
    }

    @ReactMethod
    public void initNearby(Callback callBack) {
        boolean isGooglePlayServicesAvailable = isGooglePlayServicesAvailable();

        if(isGooglePlayServicesAvailable){
            this.messageListener = new MessageListener() {
                @Override
                public void onFound(Message message) {
                    Log.d("ReactNative", "Found message: " + new String(message.getContent()));
                }

                @Override
                public void onLost(Message message) {
                    Log.d("ReactNative", "Lost sight of message: " + new String(message.getContent()));
                }
            };

            this.message = new Message("Hello World".getBytes());

            Log.d("ReactNative", "Inizializzato Nearby");
        }

        callBack.invoke(isGooglePlayServicesAvailable);
    }

    @ReactMethod
    public void start() {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            Nearby.getMessagesClient(currentActivity).publish(this.message);
            Nearby.getMessagesClient(currentActivity).subscribe(this.messageListener);

        }
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
}