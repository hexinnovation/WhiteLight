package com.hexinnovation.flashlight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    public OverlayView(Context context) {
        super(context);
        construct();
    }
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }
    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }
    private void construct() {
        mPaint = new Paint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
    private Paint mPaint;
    private Bitmap mBitmap;
    private final PointF mCenter = new PointF();
    private Path mClipPath;
    private float mRadius;

    public void setBitmap(Bitmap value) {
        mBitmap = value;
        invalidate();
    }
    public void setCenter(float x, float y) {
        mCenter.x = x;
        mCenter.y = y;
        mRadius = Float.MAX_VALUE;
        invalidateClipPath();
    }
    public void setRadius(float value) {
        mRadius = value;
        invalidateClipPath();
    }
    public void clearClipPath() {
        mClipPath = null;
        invalidate();
    }
    private void invalidateClipPath() {
        mClipPath = new Path();
        mClipPath.addCircle(mCenter.x, mCenter.y, mRadius, Path.Direction.CCW);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mClipPath != null) {
            try {
                canvas.clipPath(mClipPath);
            } catch (UnsupportedOperationException ex) {
                // We just won't animate if this operation is not supported.
            }
        }

        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }
}
