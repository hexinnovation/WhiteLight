package com.hexinnovation.flashlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class GearActionBar extends MyActionBar {
    public GearActionBar(Context context) {
        super(context);
    }
    public GearActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public GearActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected String getTitle() {
        return getResources().getString(R.string.launcher_name);
    }

    @Override
    protected Path createIcon() {
        return createGear();
    }

    @Override
    protected float getIconWorldHeight() {
        return 400;
    }

    @Override
    protected void drawCircle(Canvas canvas) {
        float radius = GEAR_CIRCLE_RADIUS;
        if (mCircleRadius != null) {
            radius = mCircleRadius.getCurrentValue();
            if (radius < -mMaxCircleRadius) {
                mCircleRadius = null;
                setSolidForeground(true);
                if (mListener != null) {
                    mListener.onIconPressed();
                }
            } else {
                invalidate();
            }
        }

        if (radius < 0) {
            radius = -radius;
        } else {
            mPaint.setColor(mBgColor);
        }

        mIconCenterCircle.set(ICON_CENTER_X - radius, ICON_CENTER_Y - radius, ICON_CENTER_X + radius, ICON_CENTER_Y + radius);
        canvas.drawOval(mIconCenterCircle, mPaint);
    }

    @Override
    protected IBezier createCircleRadiusAnimation() {
        return new BezierComposition(0,
                GEAR_CIRCLE_RADIUS, 0,
                0,100,
                -1.5F*mMaxCircleRadius, 400);
    }

    @Override
    protected IBezier createInverseCircleRadiusAnimation() {
        return new BezierComposition(0,
                -mMaxCircleRadius, 0,
                0, 200,
                GEAR_CIRCLE_RADIUS, 300);
    }

    private static Path createGear() {
        Path path = new Path();

        // -135 deg
        path.moveTo(76.8F, 119.2F);
        path.lineTo(45.6F, 83.6F);
        path.arcTo(new RectF(43.75169196F, 72.35169196F, 57.04830804F, 85.64830804F), 136.2188752F, 87.56224953F);
        path.lineTo(74.4F, 45.6F);
        path.arcTo(new RectF(72.35169196F, 43.75169196F, 85.64830804F, 57.04830804F), 226.2188752F, 87.56224953F);
        path.lineTo(119.2F, 76.8F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), -123.2586316F, 21.51726312F);

        // -90 deg
        path.lineTo(173.2F, 8.4F);
        path.arcTo(new RectF(173.128438018065F, 2.00859697092024F, 186.425054098065F, 15.3052130509202F), 181.2188752F, 87.56224953F);
        path.lineTo(220.4F, 2F);
        path.arcTo(new RectF(213.574945901935F, 2.00859697092024F, 226.871561981935F, 15.3052130509202F), 271.2188752F, 87.56224953F);
        path.lineTo(230F, 55.6F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), -78.2586316F, 21.51726312F);

        // -45 deg
        path.lineTo(316.4F, 45.6F);
        path.arcTo(new RectF(314.35169196F, 43.75169196F, 327.64830804F, 57.04830804F), 226.2188752F, 87.56224953F);
        path.lineTo(354.4F, 74.4F);
        path.arcTo(new RectF(342.95169196F, 72.35169196F, 356.24830804F, 85.64830804F), 316.2188752F, 87.56224953F);
        path.lineTo(323.2F, 119.2F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), -33.2586316F, 21.51726312F);

        // 0 deg
        path.lineTo(391.6F, 173.2F);
        path.arcTo(new RectF(384.69478694908F, 173.128438018065F, 397.99140302908F, 186.425054098065F), 271.2188752F, 87.56224953F);
        path.lineTo(398F, 220.4F);
        path.arcTo(new RectF(384.69478694908F, 213.574945901935F, 397.99140302908F, 226.871561981935F), 361.2188752F, 87.56224953F);
        path.lineTo(344.4F, 230F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), 11.7413684F, 21.51726312F);

        // 45 deg
        path.lineTo(354.4F, 316.4F);
        path.arcTo(new RectF(342.95169196F, 314.35169196F, 356.24830804F, 327.64830804F), 316.2188752F, 87.56224953F);
        path.lineTo(325.6F, 354.4F);
        path.arcTo(new RectF(314.35169196F, 342.95169196F, 327.64830804F, 356.24830804F), 406.2188752F, 87.56224953F);
        path.lineTo(280.8F, 323.2F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), 56.7413684F, 21.51726312F);

        // 90 deg
        path.lineTo(226.8F, 391.6F);
        path.arcTo(new RectF(213.574945901935F, 384.69478694908F, 226.871561981935F, 397.99140302908F), 361.2188752F, 87.56224953F);
        path.lineTo(179.6F, 398F);
        path.arcTo(new RectF(173.128438018065F, 384.69478694908F, 186.425054098065F, 397.99140302908F), 451.2188752F, 87.56224953F);
        path.lineTo(170F, 344.4F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), 101.7413684F, 21.51726312F);

        // 135 deg
        path.lineTo(83.6F, 354.4F);
        path.arcTo(new RectF(72.35169196F, 342.95169196F, 85.64830804F, 356.24830804F), 406.2188752F, 87.56224953F);
        path.lineTo(45.6F, 325.6F);
        path.arcTo(new RectF(43.75169196F, 314.35169196F, 57.04830804F, 327.64830804F), 496.2188752F, 87.56224953F);
        path.lineTo(76.8F, 280.8F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), 146.7413684F, 21.51726312F);

        // 180 deg
        path.lineTo(8.4F, 226.8F);
        path.arcTo(new RectF(2.00859697092024F, 213.574945901935F, 15.3052130509202F, 226.871561981935F), 451.2188752F, 87.56224953F);
        path.lineTo(2F, 179.6F);
        path.arcTo(new RectF(2.00859697092019F, 173.128438018065F, 15.3052130509202F, 186.425054098065F), 541.2188752F, 87.56224953F);
        path.lineTo(55.6F, 170F);
        path.arcTo(new RectF(52.667451F, 52.667451F, 347.332549F, 347.332549F), 191.7413684F, 21.51726312F);

        path.close();

//        if (cutOutCenter) {
//            path.addCircle(ICON_CENTER_X, ICON_CENTER_Y, GEAR_CIRCLE_RADIUS, Path.Direction.CCW);
//        }

        return path;
    }
    private final static float GEAR_CIRCLE_RADIUS = 88;

}
