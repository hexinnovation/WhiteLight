package com.hexinnovation.flashlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public abstract class MyActionBar extends View {
    public MyActionBar(Context context) {
        super(context);
        construct();
    }
    public MyActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }
    public MyActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    private boolean isRTL() {
        return ((MyActivity)getContext()).isRTL();
    }

    protected abstract float getIconWorldHeight();
    protected abstract String getTitle();
    protected abstract Path createIcon();
    protected abstract void drawCircle(Canvas canvas);
    protected abstract IBezier createCircleRadiusAnimation();
    protected abstract IBezier createInverseCircleRadiusAnimation();

    private void construct() {
        mIconPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        mPadding = getResources().getDimension(R.dimen.padding);
        mTitle = getTitle();
        mIconHeight = getResources().getDimension(R.dimen.gear_height);

        mPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Roboto-Regular.ttf"));
        mPaint.setTextSize(getResources().getDimension(R.dimen.title_size));
        mPaint.getTextBounds(mTitle, 0, mTitle.length(), mTitleBounds);
        mPaint.setStrokeCap(TextPaint.Cap.ROUND);
        mPaint.setStrokeWidth(5.656F);

        mIcon = createIcon();
    }

    public void setTheme(int theme) {
        switch (theme) {
            case 0:
                mFgColor = ActivityCompat.getColor(getContext(), R.color.blue_light);
                mBgColor = ActivityCompat.getColor(getContext(), R.color.track_light);
                break;
            case 1:
                mFgColor = ActivityCompat.getColor(getContext(), R.color.blue_dark);
                mBgColor = ActivityCompat.getColor(getContext(), R.color.track_dark);
                break;
        }
        invalidate();
    }

    protected Path mIcon;
    protected float mPadding, mIconHeight, mRight, mBottom, mIconPadding, mMaxCircleRadius;
    protected final TextPaint mPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.SUBPIXEL_TEXT_FLAG);
    protected int mFgColor, mBgColor, mTouchEventActionIndex = -1;
    protected final RectF mIconCenterCircle = new RectF();
    private final PointF mTextPosition = new PointF();
    private String mTitle;
    protected IBezier mCircleRadius;
    private final Rect mTitleBounds = new Rect();
    protected Listener mListener;
    private boolean mSolidForeground;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRight = right;
        mBottom = bottom;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        float w2 = w - mPadding - mIconHeight / 2;
        float h2 = h - mPadding - mIconHeight / 2;

        w2 *= 400 / mIconHeight;
        h2 *= 400 / mIconHeight;

        mMaxCircleRadius = (float)Math.sqrt(w2*w2 + h2*h2);


        mTextPosition.x = isRTL() ? getWidth() - mPadding - mPaint.measureText(mTitle, 0, mTitle.length()) : mPadding;
        mTextPosition.y = (h - (mTitleBounds.height() - mPaint.descent())) / 2 - mPaint.ascent() - mPaint.descent();
    }

    public void setSolidForeground(boolean value) {
        mSolidForeground = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSolidForeground) {
            mPaint.setColor(mFgColor);
            canvas.drawRect(0, 0, mRight, mBottom, mPaint);
            return;
        }

        mPaint.setColor(mBgColor);
        canvas.drawRect(0, 0, mRight, mBottom, mPaint);

        mPaint.setColor(mFgColor);
        canvas.drawText(mTitle, mTextPosition.x, mTextPosition.y, mPaint);

        mPaint.setColor(mFgColor);
        canvas.translate(isRTL() ? mPadding : (mRight - mIconHeight - mPadding), (mBottom - mIconHeight) / 2);
        canvas.scale(mIconHeight / 400, mIconHeight / 400);
        canvas.scale(400 / getIconWorldHeight(), 400 / getIconWorldHeight(), 200, 200);
        canvas.drawPath(mIcon, mPaint);

        drawCircle(canvas);
    }


    public void reverseAnimation() {
        mSolidForeground = false;

        mCircleRadius = createInverseCircleRadiusAnimation();

        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float r = isRTL() ? mPadding + mIconHeight + mIconPadding : (mRight - mPadding + mIconPadding);
        float l = isRTL() ? mPadding - mIconPadding : (mRight - mPadding - mIconHeight - mIconPadding);

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                if (x <= r && x >= l && y >= (mBottom - mIconHeight) / 2 - mIconPadding && y <= (mBottom + mIconHeight) / 2 + mIconPadding) {
                    mTouchEventActionIndex = MotionEventCompat.getActionIndex(event);
                    return true;
                } else {
                    mTouchEventActionIndex = -1;
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                if (MotionEventCompat.getActionIndex(event) == mTouchEventActionIndex) {
                    x = event.getX();
                    y = event.getY();
                    if (x <= r && x >= l && y >= (mBottom - mIconHeight) / 2 - mIconPadding && y <= (mBottom + mIconHeight) / 2 + mIconPadding) {
                        return true;
                    } else {
                        mTouchEventActionIndex = -1;
                        return false;
                    }
                }
            case MotionEvent.ACTION_UP:
                if (MotionEventCompat.getActionIndex(event) == mTouchEventActionIndex) {
                    x = event.getX();
                    y = event.getY();
                    if (x <= r && x >= l && y >= (mBottom - mIconHeight) / 2 - mIconPadding && y <= (mBottom + mIconHeight) / 2 + mIconPadding) {
                        performClick();
                        return true;
                    } else {
                        return false;
                    }
                }
        }

        return super.onTouchEvent(event);
    }
    @Override
    public boolean performClick() {
        if (mCircleRadius == null || mCircleRadius.hasEnded()) {
            mCircleRadius = createCircleRadiusAnimation();
            invalidate();
        }

        return super.performClick();
    }
    protected final static float ICON_CENTER_X = 200;
    protected final static float ICON_CENTER_Y = 200;

    public void setListener(Listener value) {
        mListener = value;
    }
    public interface Listener {
        void onIconPressed();
    }
}