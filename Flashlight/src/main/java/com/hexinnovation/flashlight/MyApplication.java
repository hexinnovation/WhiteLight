package com.hexinnovation.flashlight;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            // There's no camera ... Disable the widget.
            packageManager.setComponentEnabledSetting(new ComponentName(this, FlashlightWidgetProvider.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        }

        sContext = this;
    }
    private static MyApplication sContext;
    public static Context getContext() {
        return sContext;
    }
}
