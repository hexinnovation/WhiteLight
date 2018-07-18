package com.hexinnovation.flashlight;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.io.IOException;

public class FlashlightService extends Service {
    public final static String TURN_OFF_ACTION = "com.hexinnovation.flashlight.TURN_OFF_ACTION";
    public final static String TURN_ON_ACTION = "com.hexinnovation.flashlight.TURN_ON_ACTION";

    private final static int TURN_ON = 0;
    private final static int TURN_OFF = 1;
    public final static int SCREEN_OFF = 2;
    public final static int SCREEN_ON = 3;
    public final static int USER_PRESENT = 4;

    private final static int NOTIFICATION = 2;

    private final static String CHANNEL_ID = "com.hexinnovation.flashlight.notification";

    private static ServiceHandler sServiceHandler;
    private static FlashlightService sService;

    private static final Object sLock = new Object();
    private static boolean sIsLightOn = false;
    private static boolean sIsToggling = false;
    private static boolean sIsForeground = false;
    private static boolean sWasOnBefore = true;
    private static RemoteViews sNotificationView;
    private static Notification sNotification;
    private static MyActivity sMyActivity;

    private static void startService(String intentAction) {
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, FlashlightService.class);
        intent.setAction(intentAction);
        context.startService(intent);
    }
    public static void setActivity(MyActivity activity) {
        synchronized (sLock) {
            sMyActivity = activity;
        }

        refreshNotification();
    }
    public static void turnOff() {
        if (sService == null) {
            startService(TURN_OFF_ACTION);
        } else {
            synchronized (sLock) {
                if (sIsLightOn  != sIsToggling) {
                    sServiceHandler.clearMessages();
                    sServiceHandler.sendEmptyMessage(TURN_OFF);
                }
            }
        }
    }
    public static void turnOn() {
        if (sService == null) {
            startService(TURN_ON_ACTION);
        } else {
            synchronized (sLock) {
                if (sIsLightOn  == sIsToggling) {
                    sServiceHandler.clearMessages();
                    sServiceHandler.sendEmptyMessage(TURN_ON);
                }
            }
        }
    }
    public static boolean isLightOn() {
        synchronized (sLock) {
            return sIsLightOn != sIsToggling;
        }
    }

    private static boolean isScreenOn() {
        PowerManager pm = (PowerManager)sService.getSystemService(Context.POWER_SERVICE);

        if (pm == null) {
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                return pm.isInteractive();
            } else {
                // noinspection deprecation
                return pm.isScreenOn();
            }
        }
    }
    @Override
    public void onCreate() {
        synchronized (FlashlightService.class) {
            if (sServiceHandler == null) {
                HandlerThread thread = new HandlerThread("FlashlightServiceHandlerThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
                sServiceHandler = new ServiceHandler(thread.getLooper());
                sService = this;

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                filter.addAction(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_USER_PRESENT);

                getApplicationContext().registerReceiver(new ScreenReceiver(), filter);

                if (isScreenOn()) {
                    sServiceHandler.sendEmptyMessage(SCREEN_ON);
                }
            }
        }
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (TURN_OFF_ACTION.equals(intent.getAction())) {
                turnOff();
            } else if (TURN_ON_ACTION.equals(intent.getAction())) {
                turnOn();
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                refreshNotification();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private static boolean isScreenLocked() {
        KeyguardManager manager = ((KeyguardManager) sService.getSystemService(Context.KEYGUARD_SERVICE));
        return manager != null && manager.inKeyguardRestrictedInputMode();
    }
    private static boolean shouldShowNotification() {
        // First off, we need to have a flashlight.
        if (!sService.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return false;
        }

        // Secondly, we don't show the notification if the activity is showing.
        if (sMyActivity != null) {
            return false;
        }

        switch (Preferences.getNotificationSetting()) {
            case Preferences.NOTIFICATION_ALWAYS:
                return true;
            case Preferences.NOTIFICATION_NEVER:
                return isLightOn();
            case Preferences.NOTIFICATION_SOMETIMES:
                return isLightOn() || isScreenLocked();
            default:
                throw new RuntimeException("Unsupported notification setting");
        }
    }

    private static void createNotificationBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = sService.getString(R.string.notification_channel_name);
            String description = sService.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = sService.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(sService, CHANNEL_ID)
                .setOngoing(false)
                .setContentText("Hello")
                .setSubText("World")
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .setContentTitle("Title")
                .setVibrate(new long[0])
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Resources res = sService.getResources();

        sNotificationView = new RemoteViews(sService.getPackageName(), R.layout.notification);
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ic_notification);
        int targetSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17.4545454545F, res.getDisplayMetrics()));
        bmp = Bitmap.createScaledBitmap(bmp, targetSize, targetSize, true);
        sNotificationView.setImageViewBitmap(R.id.icon, bmp);
//        builder.setContent(view);

        builder.setCustomContentView(sNotificationView);

        sNotification = builder.build();
//        Notification notification = builder.build();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            notification.contentView = view;
//        }

//        return notification;
    }
    public static void refreshNotification() {
        if (sService == null) {
            // Start the service and have it refresh the notification.
            startService(Intent.ACTION_BOOT_COMPLETED);
        } else {
            if (shouldShowNotification()) {
                boolean isLightOn;
                synchronized (sLock) {
                    isLightOn = isLightOn();
                }

                Intent intent = new Intent(sService, FlashlightService.class);
                intent.setAction(isLightOn ? TURN_OFF_ACTION : TURN_ON_ACTION);
                PendingIntent pendingIntent = PendingIntent.getService(sService, NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (sNotification == null) {
                    createNotificationBuilder();
                }

                Resources res = sService.getResources();

                sNotification.contentIntent = pendingIntent;
                sNotificationView.setTextViewText(R.id.title, res.getString(R.string.app_name));
                sNotificationView.setTextViewText(R.id.text, res.getString(isLightOn ? R.string.notification_light_on : R.string.notification_light_off));


                startForegroundUpdatingNotification();
            } else {
                synchronized (sLock) {
                    if (sIsForeground) {
                        sIsForeground = false;
//                        sService.stopForeground(true);
                        NotificationManager notificationService = (NotificationManager)sService.getSystemService(NOTIFICATION_SERVICE);
                        notificationService.cancel(NOTIFICATION);
                    }
                }
            }
        }
    }
    private static void startForegroundUpdatingNotification() {
        synchronized (sLock) {
//            if (sIsForeground) {
//                sService.stopForeground(false);
//                sService.startForeground(NOTIFICATION, sNotification);
//            } else {
//                sService.startForeground(NOTIFICATION, sNotification);
            if (!sIsForeground || sWasOnBefore != isLightOn()) {
                NotificationManagerCompat.from(sService).notify(NOTIFICATION, sNotification);
                sWasOnBefore = !sWasOnBefore;
            }
            sIsForeground = true;
//            }
        }
    }


    private static final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        private void clearMessages() {
            removeMessages(TURN_ON);
            removeMessages(TURN_OFF);
        }
        private Camera mCamera;
        private SurfaceTexture mSurfaceTexture;

        private void turnOn() {
            synchronized (sLock) {
                if (mCamera == null) {
                    mCamera = Camera.open();
                }
                turnOn(mCamera.getParameters());
            }
        }
        private void turnOff() {
            synchronized (sLock) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(sService);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(sService, FlashlightWidgetProvider.class));
                if (appWidgetIds.length > 0) {
                    appWidgetManager.updateAppWidget(appWidgetIds, FlashlightWidgetProvider.createRemoteViews(sService, false));
                }

                if (mCamera == null) {
                    mCamera = Camera.open();
                }
                Camera.Parameters camParams = mCamera.getParameters();
                camParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(camParams);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                mSurfaceTexture = null;
            }
        }

        private void turnOn(Camera.Parameters camParams) {
            synchronized (sLock) {
                camParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(camParams);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    try {
                        mCamera.setPreviewTexture(mSurfaceTexture = new SurfaceTexture(0));
                    } catch (IOException e) {
                        // let it go
                    }
                }
                mCamera.startPreview();

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(sService);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(sService, FlashlightWidgetProvider.class));
                if (appWidgetIds.length > 0) {
                    appWidgetManager.updateAppWidget(appWidgetIds, FlashlightWidgetProvider.createRemoteViews(sService, true));
                }
            }
        }
        private void ensureLightIsOn() {
            synchronized (sLock) {
                if (isLightOn() && Preferences.getCycleLightOnScreenOff()) {
                    if (mCamera == null) {
                        mCamera = Camera.open();
                    } else {
                        mCamera.unlock();
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                                mCamera.reconnect();
                            } else {
                                mCamera = Camera.open();
                            }
                        } catch (IOException e) {
                            mCamera = Camera.open();
                        }
                    }

                    Camera.Parameters camParams = mCamera.getParameters();
                    if (Camera.Parameters.FLASH_MODE_TORCH.equals(camParams.getFlashMode())) {
                        turnOff();
                        turnOn();
                    } else {
                        turnOn(camParams);
                    }
                }
            }
        }
        @Override
        public synchronized void handleMessage(Message msg) {
            switch (msg.what) {
                case SCREEN_OFF:
                    try {
                        ensureLightIsOn();
                    } catch (Exception e) {
                        Log.e("FlashlightService", "Exception ensuring screen was on", e);
                    }
                    break;
                case TURN_ON:
                    synchronized (sLock) {
                        if (sIsLightOn) {
                            return;
                        } else {
                            sIsToggling = true;
                        }
                    }

                    try {
                        turnOn();
                        synchronized (sLock) {
                            sIsToggling = false;
                            sIsLightOn = true;
                        }
                        refreshNotification();
                    } catch (Exception e) {
                        synchronized (sLock) {
                            sIsToggling = false;
                        }
                        Log.e("FlashlightService", e.getMessage(), e);

                        if (shouldShowNotification() && sNotification != null) {
                            sNotification.contentView.setTextViewText(R.id.text, sService.getString(R.string.error_opening_camera));
                            startForegroundUpdatingNotification();

                            sServiceHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sNotification.contentView.setTextViewText(R.id.text, sService.getString(R.string.notification_light_off));
                                    startForegroundUpdatingNotification();
                                }
                            }, 1500);
                        }
                        synchronized (sLock) {
                            if (sMyActivity != null) {
                                sMyActivity.notifyTurnOnFailed();
                            }
                        }
                    }
                    break;
                case TURN_OFF:
                    synchronized (sLock) {
                        if (!sIsLightOn) {
                            return;
                        } else {
                            sIsToggling = true;
                        }
                    }

                    try {
                        refreshNotification();
                        turnOff();
                    } catch (Exception e) {
                        Log.e("FlashlightService", e.getMessage(), e);
                        mCamera = null;

                        if (shouldShowNotification() && sNotification != null) {
                            sNotification.contentView.setTextViewText(R.id.text, sService.getString(R.string.error_opening_camera));
                            startForegroundUpdatingNotification();

                            sServiceHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    refreshNotification();
                                }
                            }, 1500);
                        }

                        synchronized (sLock) {
                            if (sMyActivity != null) {
                                sMyActivity.notifyTurnOnFailed();
                            }
                        }
                    }

                    synchronized (sLock) {
                        sIsToggling = false;
                        sIsLightOn = false;
                    }
                    break;
                case SCREEN_ON:
                case USER_PRESENT:
                    refreshNotification();
                    break;
            }
        }
    }
    public static class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                sServiceHandler.sendEmptyMessage(SCREEN_OFF);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                sServiceHandler.sendEmptyMessage(SCREEN_ON);
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                sServiceHandler.sendEmptyMessage(USER_PRESENT);
            }
        }
    }
}
