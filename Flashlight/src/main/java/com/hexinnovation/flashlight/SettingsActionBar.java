package com.hexinnovation.flashlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

public class SettingsActionBar extends MyActionBar {
    public SettingsActionBar(Context context) {
        super(context);
    }
    public SettingsActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SettingsActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String getTitle() {
        return getResources().getString(R.string.settings);
    }

    @Override
    protected Path createIcon() {
        return LightSwitch.createFlashlightPath();
    }

    @Override
    protected float getIconWorldHeight() {
        return 206.2289582F;
    }

    private static final float[][] sRays = LightSwitch.getRays();

    @Override
    protected void drawCircle(Canvas canvas) {
        for (float[] ray : sRays) {
            canvas.drawLine(ray[0], ray[1], ray[2], ray[3], mPaint);
        }

        float radius = 0;
        if (mCircleRadius != null) {
            radius = mCircleRadius.getCurrentValue();
            if (radius > mMaxCircleRadius) {
                mCircleRadius = null;
                setSolidForeground(true);
                if (mListener != null) {
                    mListener.onIconPressed();
                }
            } else {
                invalidate();
            }
        }

        mPaint.setColor(mFgColor);
        mIconCenterCircle.set(ICON_CENTER_X -radius, ICON_CENTER_Y -radius, ICON_CENTER_X +radius, ICON_CENTER_Y +radius);
        canvas.drawOval(mIconCenterCircle, mPaint);
    }

    @Override
    protected IBezier createCircleRadiusAnimation() {
        return new BezierComposition(0,
                0, 0,
                1.5F*mMaxCircleRadius, 400);
    }

    @Override
    protected IBezier createInverseCircleRadiusAnimation() {
        return new BezierComposition(0,
                mMaxCircleRadius, 0,
                0, 500);
    }
}
