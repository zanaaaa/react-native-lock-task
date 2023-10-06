
package com.rnlocktask;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.app.ActivityManager;
import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import android.os.PowerManager;
import android.content.pm.PackageManager;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import java.util.ArrayList;

public class RNLockTaskModule extends ReactContextBaseJavaModule {


  private static final String ACTIVITY_GONE = "ACTIVITY_GONE";
  private static final String DEVICE_OWNER_CLEARED = "DEVICE_OWNER_CLEARED";
  private static final String LOCKED_TASK = "LOCKED_TASK";
  private static final String LOCKED_TASK_AS_OWNER = "LOCKED_TASK_AS_OWNER";
  private static final String UNLOCKED_TASK = "UNLOCKED_TASK";
  private static final String STARTED_BATTERY_INTENT = "STARTED BATTERY INTENT";
  private static final String AUTO_GRANTED_PERMISSIONS = "AUTO GRANTED PERMISSIONS";

  public RNLockTaskModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "RNLockTask";
  }

  @ReactMethod
  public void requestBatteryOptimizations(Promise promise) {
    try{
      String packageName = getCurrentActivity().getPackageName();

      // Check if the app is already on the battery optimization whitelist
      PowerManager powerManager = (PowerManager) getCurrentActivity().getSystemService(Context.POWER_SERVICE);
      if (powerManager != null) {
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
          // If not on the whitelist, start the intent to request exemption
          Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
          intent.setData(Uri.parse("package:" + packageName));
          getCurrentActivity().startActivity(intent);
          promise.resolve(STARTED_BATTERY_INTENT);
        }
      }
    } catch(Exception e) {
      promise.reject(e);
    }

  }

  @ReactMethod
  public  void clearDeviceOwnerApp(Promise promise) {
    try {
      Activity mActivity = getCurrentActivity();
      if (mActivity != null) {
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        myDevicePolicyManager.clearDeviceOwnerApp(mActivity.getPackageName());
        promise.resolve(DEVICE_OWNER_CLEARED);
      }
      promise.reject(ACTIVITY_GONE, "Activity gone or mismatch");
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  @ReactMethod
  public  void autoGrantPermissions(Promise promise) {
    try {
      Activity mActivity = getCurrentActivity();
      if (mActivity != null) {
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDPM = new ComponentName(mActivity, MyAdmin.class);
        myDevicePolicyManager.setPermissionPolicy(mDPM, myDevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT);
        promise.resolve(AUTO_GRANTED_PERMISSIONS);
      }
      promise.reject(ACTIVITY_GONE, "Activity gone or mismatch");
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  @ReactMethod
  public void startLockTaskWith(ReadableArray additionalPackages, Promise promise) {
    try {
      Activity mActivity = getCurrentActivity();
      ActivityManager activityManager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
      int lockTaskModeState = activityManager.getLockTaskModeState();
      if (lockTaskModeState != ActivityManager.LOCK_TASK_MODE_LOCKED && 
      lockTaskModeState != ActivityManager.LOCK_TASK_MODE_PINNED) {
        if (mActivity != null) {
            mActivity.startLockTask();
            promise.resolve(LOCKED_TASK);
        } else{
          promise.reject(ACTIVITY_GONE, "Activity gone or mismatch");
        }
      } 
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  @ReactMethod
  public void startLockTask(Promise promise) {
    startLockTaskWith(null, promise);
  }

  @ReactMethod
  public  void stopLockTask(Promise promise) {
    try {
      Activity mActivity = getCurrentActivity();
      if (mActivity != null) {
        mActivity.stopLockTask();
        promise.resolve(UNLOCKED_TASK);
      } else {
          promise.reject(ACTIVITY_GONE, "Activity gone or mismatch");
      }
    } catch (Exception e) {
      promise.reject(e);
    }
  }
}