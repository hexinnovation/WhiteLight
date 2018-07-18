package com.hexinnovation.flashlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

public class FlashlightWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent in = new Intent(context, FlashlightService.class);
            in.setAction(intent.getAction());
            context.startService(in);
        }

        super.onReceive(context, intent);
    }

    private static Bitmap sLightOn, sLightOff;
    private static Bitmap createBitmap(Context context, int backgroundColorResId) {
        Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);
        canvas.scale(0.75F, 0.75F);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        final float strokeWidth = 5.656F;
        RectF oval = new RectF(strokeWidth / 2, strokeWidth / 2, 400 - strokeWidth / 2, 400 - strokeWidth / 2);

        // Draw the background
        paint.setColor(ContextCompat.getColor(context, backgroundColorResId));
        canvas.drawOval(oval, paint);

        // Draw the circle
        paint.setColor(ContextCompat.getColor(context, R.color.blue_light));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawOval(oval, paint);

        // Draw the flashlight
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(LightSwitch.createFlashlightPath(), paint);

        // Now draw the flashlight arrows
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (float[] ray : LightSwitch.getRays()) {
            canvas.drawLine(ray[0], ray[1], ray[2], ray[3], paint);
        }

        return bmp;
    }
    private static Bitmap getBitmap(Context context, boolean isLightOn) {
        if (isLightOn && sLightOn == null) {
            sLightOn = createBitmap(context, R.color.yellow_light);
        } else if (!isLightOn && sLightOff == null) {
            sLightOff = createBitmap(context, R.color.track_light);
        }

        return isLightOn ? sLightOn : sLightOff;
    }
    public static RemoteViews createRemoteViews(Context context, boolean isLightOn) {
        // Create a ToggleIntent
        Intent toggleIntent = new Intent(context, FlashlightService.class);
        toggleIntent.setAction(isLightOn ? FlashlightService.TURN_OFF_ACTION : FlashlightService.TURN_ON_ACTION);

        // Now create a pending intent
        PendingIntent togglePendingIntent = PendingIntent.getService(context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Now create our RemoteViews object ...
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.flashlight_widget);

        // Set the on click intent ...
        views.setOnClickPendingIntent(R.id.widget_image, togglePendingIntent);

        // Set the image
        views.setImageViewBitmap(R.id.widget_image, getBitmap(context, isLightOn));

        // Return the views.
        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetManager.updateAppWidget(
                appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass())),
                createRemoteViews(context, FlashlightService.isLightOn()));

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}