package com.hexinnovation.flashlight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class BrightnessControl extends View implements IAnimateIn {
    public BrightnessControl(Context context) {
        super(context);
        construct();
    }
    public BrightnessControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }
    public BrightnessControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    private boolean isRTL() {
        return ((MyActivity)getContext()).isRTL();
    }

    private void construct() {
        mBrightness = -1;
        mYellowBounds = new RectF();
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf"));
        mPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
        mPaint.setStrokeWidth(getResources().getDimension(R.dimen.line_thickness));

        mScreenBrightness = getResources().getString(R.string.screen_brightness);
        
        mScreenBrightnessPos = new PointF();
        
        mSliderHeight = getResources().getDimension(R.dimen.brightness_slider_height);
        mBrightnessVPadding = getResources().getDimension(R.dimen.brightness_v_padding);
        mPadding = getResources().getDimension(R.dimen.padding);
        
        mSliderRadius = mSliderHeight / 2;


        mSunShadow = ContextCompat.getDrawable(getContext(), R.drawable.sun_shadow);
        
        mBoundsAdjustment = getResources().getDimension(R.dimen.line_thickness) * 2;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        if (oldW == 0 && oldH == 0) {
            // This is the first time we've changed the size ...

            setBrightness(((Activity)getContext()).getWindow().getAttributes().screenBrightness);
        }

        float dX = getWidth() - mPadding*2 - mSliderRadius*2;
        float numRotations = (float)(dX / (2*Math.PI*mSliderRadius));

        // We rotate to the nearest 1/8 complete rotation so that the sun will be on-axis when the brightness is 100%.
        numRotations = Math.round(numRotations * 8) / 8F;

        mEndAngle = numRotations * 360;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        if (changed) {
            performLayout();
        }
    }
    
    private void performLayout() {
        Rect bounds = new Rect();
        mPaint.getTextBounds(mScreenBrightness, 0, mScreenBrightness.length(), bounds);
        int gap = (int)Math.floor((getHeight() - bounds.height() - mBrightnessVPadding - mSliderHeight) / 2f);
        
        mScreenBrightnessPos.x = (getWidth() - bounds.right) / 2;
        mScreenBrightnessPos.y = -mPaint.ascent() - mPaint.descent() + gap;
        
        mSliderY = mScreenBrightnessPos.y + mPaint.descent() + mBrightnessVPadding + mSliderRadius;

        mSunLogoBounds.set(0, (float) Math.floor(mSliderY - mSliderRadius), 0, (float) Math.ceil(mSliderY + mSliderRadius));
        mSunShadow.setBounds(0, (int)Math.floor(mSliderY - mSliderRadius - 12 * mSliderRadius * 2 / 194f), 0, (int)Math.floor(mSliderY + mSliderRadius + 24 * mSliderRadius * 2 / 194f));
        updateSunLocation();
    }
    private void updateSunLocation() {
        float percent = mBrightness;
        if (isRTL()) {
            percent = 1 - percent;
        }

        if (mSunLogoBounds == null) {
            return;
        }

        mSunLogoBounds.left = Math.round(mPadding + percent * (getWidth() - mPadding * 2 - mSliderHeight));
        mSunLogoBounds.right = mSunLogoBounds.left + mSunLogoBounds.height();

        mSunLogoCircle.set(mSunLogoBounds.left + 0.62F * mSliderRadius, mSunLogoBounds.top + 0.62F * mSliderRadius, mSunLogoBounds.right - 0.62F * mSliderRadius, mSunLogoBounds.bottom -0.62F * mSliderRadius);

        Rect sunShadowBounds = new Rect(mSunShadow.getBounds());
        sunShadowBounds.left = Math.round(mSunLogoBounds.left - 18 * mSunLogoBounds.height() / 194f);
        sunShadowBounds.right = Math.round(mSunLogoBounds.right + 18 * mSunLogoBounds.height() / 194f);
        mSunShadow.setBounds(sunShadowBounds);
        
        mYellowBounds = new RectF(mSunLogoBounds);
    }
    
    private float mBrightness = -1;

    public void setTheme(int theme) {
        switch (theme) {
            case 0:
                mGray = ContextCompat.getColor(getContext(), R.color.gray_light);
                mYellow = ContextCompat.getColor(getContext(), R.color.yellow_light);
                break;
            case 1:
                mGray = ContextCompat.getColor(getContext(), R.color.gray_dark);
                mYellow = ContextCompat.getColor(getContext(), R.color.yellow_dark);
                break;
        }
        invalidate();
    }

    
    private PointF mScreenBrightnessPos;
    private Drawable mSunShadow;
    private final RectF mSunLogoBounds = new RectF(), mSunLogoCircle = new RectF();
    private Paint mPaint;
    private int mGray, mYellow;
    private float mSliderHeight, mSliderRadius, mBrightnessVPadding, mPadding, mSliderY;
    private RectF mYellowBounds;
    
    private String mScreenBrightness;
    
    public final static float MIN_BRIGHTNESS = 0.01F;
    
    public boolean hasScreenBrightness() {
        return mBrightness >= 0;
    }
    
    public float getBrightness() {
        return Math.max(mBrightness, MIN_BRIGHTNESS);
    }
    private float mBoundsAdjustment;
    private Rect getSunLogoBoundsToInvalidate() {
        Rect retVal = new Rect((int)Math.floor(mSunLogoBounds.left), (int)Math.floor(mSunLogoBounds.top), (int)Math.ceil(mSunLogoBounds.right), (int)Math.ceil(mSunLogoBounds.bottom));
        
        retVal.left -= mBoundsAdjustment;
        retVal.top -= mBoundsAdjustment;
        retVal.bottom += mBoundsAdjustment;
        retVal.right += mBoundsAdjustment;
        
        return retVal;
    }
    public void setBrightness(float value) {
        mBrightness = value;
        
        
        invalidate(getSunLogoBoundsToInvalidate());

        updateSunLocation();
        invalidate(getSunLogoBoundsToInvalidate());

        if (mListener != null) {
            mListener.onBrightnessChanged(getBrightness());
        }
    }
    private int mTouchIndex = -1;
    private void setBrightnessTouchEvent(float x) {
        if (x < mPadding + mSliderRadius) {
            x = mPadding + mSliderRadius;
        } else if (x > getWidth() - mPadding - mSliderRadius) {
            x = getWidth() - mPadding - mSliderRadius;
        }
        
        x = (x - mPadding - mSliderRadius) / (getWidth() - (mPadding + mSliderRadius) * 2);

        if (isRTL()) {
            x = 1 - x;
        }

        setBrightness(x);
    }
    @SuppressLint("ClickableViewAccessibility") @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();

                if (mTouchIndex == -1 && Math.abs(mSliderY - y) < mSliderHeight) {
                    mTouchIndex = MotionEventCompat.getActionIndex(event);
                    setBrightnessTouchEvent(x);
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchIndex == MotionEventCompat.getActionIndex(event)) {
                    setBrightnessTouchEvent(event.getX());
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchIndex == MotionEventCompat.getActionIndex(event)) {
					getParent().requestDisallowInterceptTouchEvent(false);
                    mTouchIndex = -1;
                    return true;
                }
                break;
        }
        return false;
    }
    
    private Listener mListener;
    public void setListener(Listener value) {
        mListener = value;
    }
    public interface Listener {
        void onBrightnessChanged(float newValue);
    }


    public static final int START_ANGLE = 0;
    public float mEndAngle;

    public void animateIn(long startOffset) {
        setVisibility(View.INVISIBLE);
        setVisibility(View.VISIBLE);

        int rotateMultiplier = isRTL() ? -1 : 1;

        mSunRotation = new BezierComposition(startOffset, rotateMultiplier * -100, 776, rotateMultiplier * 40, 1352, rotateMultiplier * -25, 1663, 0, 1973);
        mSunScale = new BezierComposition(startOffset, 0, 776, 1.05F, 953, 0.98F, 1064, 1, 1175);
        mSunAlpha = BezierAnimation.easeOut(0, 255, startOffset + 865, 177);
        mTextAlpha = BezierAnimation.easeOut(0, 255, startOffset + 599, 222);
        mLineScale = BezierAnimation.easeOut(0, 1, startOffset + 798, 288);
        
        invalidate();
    }
    
    private BezierComposition mSunRotation, mSunScale;
    private BezierAnimation mSunAlpha, mTextAlpha, mLineScale;
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mTextAlpha == null) {
            return;
        }
        
        // Draw the label ...
        mPaint.setColor(mGray);
        
        mPaint.setAlpha((int) mTextAlpha.getCurrentValue());
        canvas.drawText(mScreenBrightness, mScreenBrightnessPos.x, mScreenBrightnessPos.y, mPaint);

        float cX = getWidth() / 2F,
              cY = mSliderY,
              scale = mLineScale.getCurrentValue();
        
        mPaint.setAlpha(255);
        canvas.save();
        canvas.scale(scale, 1, cX, cY);
        canvas.drawLine(mPadding + mPaint.getStrokeWidth() / 2, mSliderY, getWidth() - mPadding - mPaint.getStrokeWidth() / 2, mSliderY, mPaint);
        canvas.restore();
        
        int alpha = (int)mSunAlpha.getCurrentValue();
        mPaint.setColor(mYellow);
        
        float theta = START_ANGLE + mBrightness * (isRTL() ? -1 : 1) * (mEndAngle - START_ANGLE) + mSunRotation.getCurrentValue();
        cX = mSunLogoBounds.centerX();
        cY = mSunLogoBounds.centerY();
        scale = mSunScale.getCurrentValue();
        canvas.scale(scale, scale, cX, cY);
        mSunShadow.draw(canvas);
        canvas.drawRoundRect(mYellowBounds, mSliderRadius, mSliderRadius, mPaint);
        canvas.rotate(theta, cX, cY);
        final float strokeWidthMultiplier = 2;

        mPaint.setStrokeWidth(mPaint.getStrokeWidth() / strokeWidthMultiplier);
        mPaint.setColor(mGray);
        mPaint.setAlpha(alpha);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(mSunLogoCircle, mSunLogoCircle.width() / 2, mSunLogoCircle.height() / 2, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawLine(mSunLogoBounds.left + 1.476F * mSliderRadius, mSunLogoBounds.top + mSliderRadius, mSunLogoBounds.left + 1.62F * mSliderRadius, mSunLogoBounds.top + mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + 1.3365828278448F * mSliderRadius, mSunLogoBounds.top + 1.3365828278448F * mSliderRadius, mSunLogoBounds.left + 1.43840620433566F * mSliderRadius, mSunLogoBounds.top + 1.43840620433566F * mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + mSliderRadius, mSunLogoBounds.top + 1.476F * mSliderRadius, mSunLogoBounds.left + mSliderRadius, mSunLogoBounds.top + 1.62F * mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + 0.663417172155203F * mSliderRadius, mSunLogoBounds.top + 1.3365828278448F * mSliderRadius, mSunLogoBounds.left + 0.56159379566434F * mSliderRadius, mSunLogoBounds.top + 1.43840620433566F * mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + 0.524F * mSliderRadius, mSunLogoBounds.top + mSliderRadius, mSunLogoBounds.left + 0.38F * mSliderRadius, mSunLogoBounds.top + mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + 0.663417172155203F * mSliderRadius, mSunLogoBounds.top + 0.663417172155203F * mSliderRadius, mSunLogoBounds.left + 0.56159379566434F * mSliderRadius, mSunLogoBounds.top + 0.56159379566434F * mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + mSliderRadius, mSunLogoBounds.top + 0.524F * mSliderRadius, mSunLogoBounds.left + mSliderRadius, mSunLogoBounds.top + 0.38F * mSliderRadius, mPaint);
        canvas.drawLine(mSunLogoBounds.left + 1.3365828278448F * mSliderRadius, mSunLogoBounds.top + 0.663417172155203F * mSliderRadius, mSunLogoBounds.left + 1.43840620433566F * mSliderRadius, mSunLogoBounds.top + 0.56159379566434F * mSliderRadius, mPaint);

        mPaint.setStrokeWidth(mPaint.getStrokeWidth() * strokeWidthMultiplier);

        if (!mSunRotation.hasEnded()) {
            invalidate();
        }
    }
}