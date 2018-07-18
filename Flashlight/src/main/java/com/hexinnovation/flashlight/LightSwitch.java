package com.hexinnovation.flashlight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public final class LightSwitch extends View implements IAnimateIn {
    public LightSwitch(Context context) {
        super(context);
        construct();
    }
    public LightSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }
    public LightSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }

    private boolean isRTL() {
        return ((MyActivity)getContext()).isRTL();
    }

    @SuppressLint("ClickableViewAccessibility") 
    private void construct() {
        mTrack = new RectF();
        mSlider = new RectF();

        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf"));
        mTextPaint.setTextSize(getResources().getDimension(R.dimen.text_size));

        mTextPaint.setStrokeWidth(getResources().getDimension(R.dimen.arrow_thickness));
        mTextPaint.setStrokeCap(Cap.ROUND);

        mNotSupportedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | TextPaint.LINEAR_TEXT_FLAG | TextPaint.SUBPIXEL_TEXT_FLAG);

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float ratio = (velocityX / velocityY);
                if (Math.abs(ratio) >= VALID_FLING_DIRECTION_RATIO) {
                    if (Math.abs(velocityX) >= MIN_FLING_VELOCITY_X) {
                        animateValueTo(velocityX > 0 == isRTL() ? 0 : 1, Math.abs(velocityX));
                        mDragEventIndex = -1;
                        return true;
                    }
                }
                
                return false;
            }
        });

        mSliderShadow = ContextCompat.getDrawable(getContext(), R.drawable.slider_shadow);
        mTrackLeft = ContextCompat.getDrawable(getContext(), R.drawable.track_left);
        mTrackTop = ContextCompat.getDrawable(getContext(), R.drawable.track_top);
        mTrackRight = ContextCompat.getDrawable(getContext(), R.drawable.track_left);
        mTrackBottom = ContextCompat.getDrawable(getContext(), R.drawable.track_bottom);

        mSwipeOn = getResources().getString(R.string.swipe_on);
        mSwipeOff = getResources().getString(R.string.swipe_off);
        
        mSwipeOnPos = new PointF();
        mSwipeOffPos = new PointF();
        
        mArrowWidth = getResources().getDimension(R.dimen.arrow_width);
        mArrowHeight = getResources().getDimension(R.dimen.arrow_height);
        
        mSwipeOnArrow = new RectF();
        mSwipeOffArrow = new RectF();

        mFlashlightPath = createFlashlightPath();
    }

    public void setTheme(int theme) {
        switch (theme) {
            case 0:
                mNotSupportedPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_light));
                mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_light));
                mTrackPaint.setColor(ContextCompat.getColor(getContext(), R.color.track_light));
                break;
            case 1:
                mNotSupportedPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
                mTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_dark));
                mTrackPaint.setColor(ContextCompat.getColor(getContext(), R.color.track_dark));
                break;
        }
        invalidate();
    }
    
    public void turnLightOn() {
    	setValue(1);
    }
    public void turnLightOff() {
    	setValue(0);
    }

    @SuppressLint("ClickableViewAccessibility") @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mGestureDetector.onTouchEvent(event);
        
        float x;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mDragEventIndex == MotionEventCompat.getActionIndex(event)) {
                    x = event.getX();
                    
                    float dX = x - mLastDragX;

                    if (isRTL()) {
                        dX = -dX;
                    }
                    
                    if (dX != 0) {
                        float trackWidth = mTrack.width() - mTrack.height();
                        
                        float newValue = mValue + dX / trackWidth;
                        mLastDragX = x;
                        
                        setValue(Math.max(0, Math.min(1, newValue)));
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                x = event.getX();

                if (mSlider.contains(x, event.getY())) {
                    mDragEventIndex = MotionEventCompat.getActionIndex(event);
                    mInitialValue = mValue;
                    mLastDragX = x;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (mDragEventIndex == MotionEventCompat.getActionIndex(event)) {

                    getParent().requestDisallowInterceptTouchEvent(false);
                    mDragEventIndex = -1;
                    
                    if (mInitialValue == 1) {
                    	if (mValue < ROUND_DOWN_CUTOFF) {
                    		animateValueTo(0, 0);
                    	} else {
                    		animateValueTo(1, 0);
                    	}
                    } else {
                    	if (mValue > ROUND_UP_CUTOFF) {
                            animateValueTo(1, 0);
                        } else {
                            animateValueTo(0, 0);
                        }
                    }
                }
                
                break;
        }
        
        return result;
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && !mIsNotSupported) {
            refreshTrackAndSlider();
        }
        
        if (mIsNotSupported) {
        	measureNotSupportedText();
        }
    }
    
    private void refreshTrackAndSlider() {
        float height = getResources().getDimension(R.dimen.slider_height);

        mTrack.top = (getHeight() - height) / 2f;
        mTrack.bottom = mTrack.top + height;
        mTrack.left = getResources().getDimension(R.dimen.padding);
        mTrack.right = getWidth() - mTrack.left;
        
        mTrackRadius = height / 2f;

        mTrackLeft.setBounds(Math.round(mTrack.left - 39 * mTrack.height() / 272), Math.round(mTrack.top - 24 * mTrack.height() / 272), Math.round(mTrack.left + mTrack.height()), Math.round(mTrack.bottom + 56 * mTrack.height() / 272));
        mTrackTop.setBounds(Math.round(mTrack.left + mTrack.height()), Math.round(mTrack.top - 24 * mTrack.height() / 272), Math.round(mTrack.right - mTrack.height()), Math.round(mTrack.top));
        mTrackRight.setBounds(Math.round(mTrack.right - mTrack.height()), Math.round(mTrack.top - 24 * mTrack.height() / 272), Math.round(mTrack.right + 39 * mTrack.height() / 272), Math.round(mTrack.bottom + 56 * mTrack.height() / 272));
        mTrackBottom.setBounds(Math.round(mTrack.left + mTrack.height()), (int) Math.floor(mTrack.bottom), Math.round(mTrack.right - mTrack.height()), Math.round(mTrack.bottom + 56 * mTrack.height() / 272));
        refreshSlider();
    }
    public final static float SPACE_WIDTH_RATIO = 2;
    private void refreshSlider() {
        mSlider.top = mTrack.top;
        mSlider.bottom = mTrack.bottom;
        float val = mValue;
        if (isRTL()) {
            val = 1 - val;
        }
        mSlider.left = mTrack.left + val * (mTrack.width() - mTrack.height());
        mSlider.right = mSlider.left + mTrack.height();
        
        float shadowOffset = (getResources().getDimension(R.dimen.slider_height) * 308 / 272 - mSlider.height()) / 2;
        mSliderShadow.setBounds(Math.round(mSlider.left - shadowOffset), Math.round(mSlider.top - 5 * shadowOffset / 18), Math.round(mSlider.right + shadowOffset), Math.round(mSlider.bottom + 22 * shadowOffset / 18));

        Rect swipeOnBounds = new Rect(), swipeOffBounds = new Rect();

        if (isRTL()) {
            mTextPaint.getTextBounds(mSwipeOff, 0, mSwipeOff.length(), swipeOnBounds);
            mTextPaint.getTextBounds(mSwipeOn, 0, mSwipeOn.length(), swipeOffBounds);
        } else {
            mTextPaint.getTextBounds(mSwipeOn, 0, mSwipeOn.length(), swipeOnBounds);
            mTextPaint.getTextBounds(mSwipeOff, 0, mSwipeOff.length(), swipeOffBounds);
        }

        float[] spaceWidth = new float[1];
        mTextPaint.getTextWidths(" ", spaceWidth);
        
        float extraSpace = mTrack.width() - mTrack.height() - swipeOnBounds.width() - spaceWidth[0]*SPACE_WIDTH_RATIO - mArrowWidth;

        mSwipeOnArrow.right = mTrack.right - extraSpace / 2;
        mSwipeOnArrow.left = mSwipeOnArrow.right - mArrowWidth;
        
        mSwipeOnArrow.top = mSlider.top + (mSlider.height() - mArrowHeight) / 2f;
        mSwipeOnArrow.bottom = mSwipeOnArrow.top + mArrowHeight;

        mSwipeOnPos.x = mSwipeOnArrow.left - spaceWidth[0]*SPACE_WIDTH_RATIO - swipeOnBounds.width(); 
        mSwipeOnPos.y = mTrack.top + mTrackRadius - mTextPaint.ascent() * 0.35f;
        
        extraSpace = mTrack.width() - mTrack.height() - swipeOffBounds.width() - spaceWidth[0]*SPACE_WIDTH_RATIO - mArrowWidth;
        
        mSwipeOffArrow.left = mTrack.left + extraSpace / 2;
        mSwipeOffArrow.right = mSwipeOffArrow.left + mArrowWidth;
        
        mSwipeOffArrow.top = mSwipeOnArrow.top;
        mSwipeOffArrow.bottom = mSwipeOnArrow.bottom;
        
        mSwipeOffPos.x = mSwipeOffArrow.right + spaceWidth[0]*SPACE_WIDTH_RATIO;
        mSwipeOffPos.y = mSwipeOnPos.y;
        
        mSwipeOffWidths = new float[Math.max(mSwipeOff.length(), mSwipeOn.length())];
        
        mTextPaint.getTextWidths(isRTL() ? mSwipeOn : mSwipeOff, mSwipeOffWidths);
    }

    public void notifyTurnOnFailed() {
        if (mDragEventIndex < 0) {
            animateValueTo(0, 0);
        }
    }

    public void setValue(float newValue) {
        mValue = newValue;
        float adjustment = getResources().getDimension(R.dimen.line_thickness) * 2;

        invalidate((int)Math.floor(mSlider.left - adjustment), (int)Math.floor(mSlider.top - adjustment), (int)Math.ceil(mSlider.right + adjustment), (int)Math.ceil(mSlider.bottom + adjustment));
        
        refreshSlider();
        
        invalidate((int)Math.floor(mSlider.left - adjustment), (int)Math.floor(mSlider.top - adjustment), (int)Math.ceil(mSlider.right + adjustment), (int)Math.ceil(mSlider.bottom + adjustment));
    }
    private int mDragEventIndex = -1;
    private float mLastDragX;
    private GestureDetector mGestureDetector;
    private RectF mTrack, mSlider;
    private Paint mTrackPaint, mTextPaint;
    private Drawable mSliderShadow, mTrackTop, mTrackLeft, mTrackRight, mTrackBottom;
    private Path mFlashlightPath;
    private float mValue, mInitialValue, mTrackRadius;
    private String mSwipeOn, mSwipeOff;
    private PointF mSwipeOnPos, mSwipeOffPos;
    private RectF mSwipeOnArrow, mSwipeOffArrow;
    private float mArrowWidth, mArrowHeight;
    private float[] mSwipeOffWidths;

    private static final float[][] sRays = getRays();
    public static float[][] getRays() {
        return new float[][] {
                { 141.338F, 141.338F, 106.830F, 106.830F },
                { 178.834F, 108.034F, 166.202F,  60.898F },
                { 108.034F, 178.834F,  60.898F, 166.202F },
        };
    }

    private static final int START_ANGLE = 0;
    private static final int END_ANGLE = 450;
    
    private static final float ROUND_UP_CUTOFF = 0.5F;
    private static final float ROUND_DOWN_CUTOFF = 0.5F;
    private static final int ANIMATION_DURATION = 300;
    
    private static final float VALID_FLING_DIRECTION_RATIO = 1;
    private static final float MIN_FLING_VELOCITY_X = 1000;
    
    private Listener mFlashLightListener;
    public void setFlashLightListener(Listener listener) {
        mFlashLightListener = listener;
    }
    
    private IBezier mValueAnimation;
    private void animateValueTo(float value, float velocity0) {
        if (value != mValue) {
        	if (velocity0 == 0) {
        		mValueAnimation = BezierAnimation.easeInOut(mValue, value, 0, (long)(Math.abs(value - mValue) * ANIMATION_DURATION * 2));
        	} else {
            	mValueAnimation = BezierAnimation.easeOut(mValue, value, 0, (long)(Math.abs(value - mValue) * ANIMATION_DURATION));
        	}
            
            float adjustment = getResources().getDimension(R.dimen.line_thickness) * 2;
            invalidate((int)Math.floor(mSlider.left - adjustment), (int)Math.floor(mSlider.top - adjustment), (int)Math.floor(mSlider.right + adjustment), (int)Math.floor(mSlider.bottom + adjustment));
        }
        
        if (value == 1) {
            onFlashLightTurnedOn();
        } else if (value == 0) {
            onFlashLightTurnedOff();
        }
    }
    protected void onFlashLightTurnedOn() {
        if (mFlashLightListener != null)
            mFlashLightListener.onLightSwitched(true);
    }
    protected void onFlashLightTurnedOff() {
        if (mFlashLightListener != null)
            mFlashLightListener.onLightSwitched(false);
    }
    
    @Override
    public void animateIn(long startOffset) {
    	setVisibility(View.INVISIBLE);
    	setVisibility(View.VISIBLE);
    	
    	final float SPEED_RATIO = 1.33F;
    	
    	mScaleX = new BezierComposition(startOffset, 0F, 0, 0.8F, 100*SPEED_RATIO, 1.05F, 200*SPEED_RATIO, 0.98F, 283*SPEED_RATIO, 1F, 367*SPEED_RATIO);
    	mScaleY = new BezierComposition(startOffset, 0F, 0, 0.1F, 100*SPEED_RATIO, 1.05F, 200*SPEED_RATIO, 0.98F, 283*SPEED_RATIO, 1F, 367*SPEED_RATIO);
    	
    	mCircleScale = BezierAnimation.easeOut(0, 1, startOffset + 267, 200);
    	
    	mFlashlightScale = BezierAnimation.easeOut(0.4F, 1.0F, startOffset + 422, 222);

        if (isRTL()) {
            mFlashlightRotation = BezierAnimation.easeOut(END_ANGLE + 17F, END_ANGLE, startOffset + 533, 111);
        } else {
            mFlashlightRotation = BezierAnimation.easeOut(-17F, START_ANGLE, startOffset + 533, 111);
        }
    	mFlashlightOpacity = BezierAnimation.easeOut(0, 255, startOffset + 422, 222);
    	mTextAlpha = BezierAnimation.easeOut(0, 255, startOffset + 600, 222);
    	
    	mArrowHeadAngle = BezierAnimation.easeInOut(45, 0, startOffset + 680, 222);
    	
    	invalidate();
    }
    
    private BezierComposition mScaleX, mScaleY;
    private BezierAnimation mCircleScale;
    private BezierAnimation mFlashlightScale, mFlashlightRotation, mFlashlightOpacity;
    private BezierAnimation mTextAlpha, mArrowHeadAngle;
    
    
    public void setNotSupported() {
    	mIsNotSupported = true;
    	mTextPadding = getResources().getDimension(R.dimen.padding);
    	mNotSupportedMessage = getResources().getString(R.string.torch_not_supported);
    	mNotSupportedPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
    	mNotSupportedPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf"));
    	measureNotSupportedText();
    }
    private void measureNotSupportedText() {
    	mNotSupportedLayout = new StaticLayout(mNotSupportedMessage, mNotSupportedPaint, Math.max(0, getWidth() - Math.round(mTextPadding * 2)), Alignment.ALIGN_CENTER, 1, 0, false);
    }
    private float mTextPadding;
    private boolean mIsNotSupported;
    private TextPaint mNotSupportedPaint;
    private String mNotSupportedMessage;
    private StaticLayout mNotSupportedLayout;

    public static Path createFlashlightPath() {
        Path path = new Path();

        path.moveTo(123.63F, 190.03F);
        path.arcTo(new RectF(183.8168F, 121.716F, 209.8248F, 147.724F), -121.48F, 104.072F);
        path.lineTo(226.314F, 194.582F);
        path.arcTo(new RectF(282.658F, 267.8396F, 306.61F, 291.7916F), -42.02708F, 66.77598F);
        path.arcTo(new RectF(267.8396F, 282.658F, 291.7916F, 306.61F), 65.24784799F, 66.77598F);
        path.lineTo(194.582F, 226.314F);
        path.arcTo(new RectF(121.716F, 183.8168F, 147.724F, 209.8248F), 107.408F, 104.072F);
        path.close();

        path.addCircle(226.8F, 226.8F, 10, Path.Direction.CCW);

        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	if (mIsNotSupported) {
    		canvas.translate(mTextPadding, (getHeight() - mNotSupportedLayout.getHeight()) / 2);
    		mNotSupportedLayout.draw(canvas);
    		return;
    	}
    	
    	if (mScaleX == null) {
    		return;
    	}

        // Draw the track ...
		float sx = mScaleX.getCurrentValue();
    	float sy = mScaleY.getCurrentValue();

        int initialSave = canvas.save();
    	canvas.scale(sx, sy, mTrack.centerX(), mTrack.centerY());
    	
    	mTrackTop.draw(canvas);
        mTrackLeft.draw(canvas);
        mTrackBottom.draw(canvas);
        Rect bounds = mTrackRight.getBounds();
        float cX = bounds.exactCenterX();
        float cY = bounds.exactCenterY();
        canvas.rotate(-180, cX, cY);
        mTrackRight.draw(canvas);
        canvas.rotate(-180, cX, cY);
        canvas.drawRoundRect(mTrack, mTrackRadius, mTrackRadius, mTrackPaint);
        
        canvas.restore();

        // Draw "Swipe On -->"
        int a = (int)mTextAlpha.getCurrentValue();
        mTextPaint.setAlpha(a);
        
        canvas.drawText(isRTL() ? mSwipeOff : mSwipeOn, mSwipeOnPos.x, mSwipeOnPos.y, mTextPaint);
        
        canvas.drawLine(mSwipeOnArrow.left, mSwipeOnArrow.centerY(), mSwipeOnArrow.right, mSwipeOnArrow.centerY(), mTextPaint);
        cX = mSwipeOnArrow.right;
        cY = mSwipeOnArrow.centerY();
        float angle = mArrowHeadAngle.getCurrentValue();
        canvas.rotate(-angle, cX, cY);
        canvas.drawLine(mSwipeOnArrow.right - mSwipeOnArrow.height() / 2f, mSwipeOnArrow.top, mSwipeOnArrow.right, mSwipeOnArrow.centerY(), mTextPaint);
        canvas.rotate(angle*2, cX, cY);
        canvas.drawLine(mSwipeOnArrow.right - mSwipeOnArrow.height() / 2f, mSwipeOnArrow.bottom, mSwipeOnArrow.right, mSwipeOnArrow.centerY(), mTextPaint);
        canvas.rotate(-angle, cX, cY);
        
        canvas.scale(sx, sy, mTrack.centerX(), mTrack.centerY());
        canvas.drawRect(mTrack.left + mTrackRadius, mTrack.top, mSlider.centerX(), mTrack.bottom, mTrackPaint);// mSlider.centerX();
        canvas.restoreToCount(initialSave);

        // Draw "<-- Swipe Off"
        if (mSlider.left > mSwipeOffArrow.left) {
            float endOfArrow = mSwipeOffArrow.left + mSwipeOffArrow.height() / 2f;
            cX = mSwipeOffArrow.left;
            cY = mSwipeOffArrow.centerY();
            canvas.drawLine(mSwipeOffArrow.left, mSwipeOffArrow.centerY(), Math.min(mSwipeOffArrow.right, mSlider.centerX()), mSwipeOffArrow.centerY(), mTextPaint);
            canvas.rotate(angle, cX, cY);
            canvas.drawLine(endOfArrow, mSwipeOffArrow.top, mSwipeOffArrow.left, mSwipeOffArrow.centerY(), mTextPaint);
            canvas.rotate(-angle * 2, cX, cY);
            canvas.drawLine(endOfArrow, mSwipeOffArrow.bottom, mSwipeOffArrow.left, mSwipeOffArrow.centerY(), mTextPaint);
            canvas.rotate(angle, cX, cY);
        }
        int len = 0;
        float posX = mSwipeOffPos.x;
        while (posX < mSlider.centerX() && len < mSwipeOff.length()) {
        	posX += mSwipeOffWidths[len++];
        }
        if (len != 0) {
            String text = isRTL() ? mSwipeOn : mSwipeOff;
        	canvas.drawText(text.substring(0, Math.min(len, text.length())), mSwipeOffPos.x, mSwipeOffPos.y, mTextPaint);
        }
        
    	// Draw the slider icon
        cX = mSlider.centerX();
        cY = mSlider.centerY();
        mTextPaint.setAlpha(255);

        sx = mCircleScale.getCurrentValue();
        canvas.scale(sx, sx, cX, cY);

        // Draw slider shadow
        mSliderShadow.draw(canvas);
        canvas.drawRoundRect(mSlider, mTrackRadius, mTrackRadius, mTrackPaint);

        // Draw circle
        mTextPaint.setStyle(Style.STROKE);
        canvas.drawRoundRect(mSlider, mTrackRadius, mTrackRadius, mTextPaint);
        mTextPaint.setStyle(Style.FILL);


        // Fill in the slider
        canvas.restoreToCount(initialSave);

        canvas.translate(cX, cY);
        sx = mFlashlightScale.getCurrentValue();
        canvas.scale(sx * mSlider.width() / 400, sx * mSlider.height() / 400);
        canvas.rotate(mFlashlightRotation.getCurrentValue() + (isRTL() ? -1 : 1) * (END_ANGLE - START_ANGLE) * mValue);
        canvas.translate(-200, -200);

        if (mValue > 0) {
            mTextPaint.setAlpha(Math.round(mFlashlightOpacity.getCurrentValue() * mValue));
            float strokeWidth = mTextPaint.getStrokeWidth();
            mTextPaint.setStrokeWidth(5.656F);

            for (float[] ray : sRays) {
                canvas.drawLine(ray[0], ray[1], ray[0] + (ray[2] - ray[0]) * mValue, ray[1] + (ray[3] - ray[1]) * mValue, mTextPaint);
            }

            mTextPaint.setStrokeWidth(strokeWidth);
        }
        mTextPaint.setAlpha(Math.round(mFlashlightOpacity.getCurrentValue()));
        canvas.drawPath(mFlashlightPath, mTextPaint);

    	if (!mArrowHeadAngle.hasEnded()) {
    		invalidate();
    	}
        
        // If we're animating, continue animating ...
        if (mValueAnimation != null) {
            setValue(mValueAnimation.getCurrentValue());
            
            if (mValueAnimation.hasEnded()) {
            	mValueAnimation = null;
            }
        }
    }
    public interface Listener {
        void onLightSwitched(boolean turnedOn);
    }
}
