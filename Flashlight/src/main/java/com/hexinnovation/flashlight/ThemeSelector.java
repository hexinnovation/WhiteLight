package com.hexinnovation.flashlight;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class ThemeSelector extends View {
    public ThemeSelector(Context context) {
        super(context);
        construct();
    }
    public ThemeSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }
    public ThemeSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }
    private void construct() {
        Context context = getContext();
        Resources res = getResources();
        mPadding = res.getDimensionPixelOffset(R.dimen.padding);
        for (int i = 0; i < mThemes.length; i++) {
            mThemes[i] = new RectF();
            for (int j = 0; j < mThemeColors[i].length; j++) {
                mThemeColors[i][j] = ContextCompat.getColor(context, mThemeColors[i][j]);
            }
        }

        mPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf"));

        mSliderShadow = ContextCompat.getDrawable(getContext(), R.drawable.slider_shadow);
        mTrackLeft = ContextCompat.getDrawable(getContext(), R.drawable.track_left);
        mTrackTop = ContextCompat.getDrawable(getContext(), R.drawable.track_top);
        mTrackRight = ContextCompat.getDrawable(getContext(), R.drawable.track_left);
        mTrackBottom = ContextCompat.getDrawable(getContext(), R.drawable.track_bottom);
        mSunShadow = ContextCompat.getDrawable(getContext(), R.drawable.sun_shadow);
        mBackgroundLeft = ContextCompat.getDrawable(getContext(), R.drawable.theme_left);
        mBackgroundCenter = ContextCompat.getDrawable(getContext(), R.drawable.theme_center);

        mSwipeOn = getResources().getString(R.string.swipe_on);
        mScreenBrightness = getResources().getString(R.string.screen_brightness);

        mArrowWidth = getResources().getDimension(R.dimen.arrow_width);
        mArrowHeight = getResources().getDimension(R.dimen.arrow_height);
    }
    private int mPadding, mTouchActionIndex, mTouchTheme;
    private float mThemePadding, mTrackHeight, mArrowWidth, mArrowHeight, mShadowOffset, mThemeCenterLineY, mSliderHeight, mSliderY;
    private final static float SIZE_RATIO = 1.33333f;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable mSliderShadow, mTrackLeft, mTrackTop, mTrackRight, mTrackBottom, mSunShadow, mBackgroundLeft, mBackgroundCenter;

    private final Path mFlashlightPath = LightSwitch.createFlashlightPath();

    private final RectF mTmpRectF = new RectF();
    private final RectF mYellowBounds = new RectF();
    private final RectF mSwipeOnArrow = new RectF();

    private final PointF mSwipeOnPos = new PointF();
    private final PointF mScreenBrightnessPos = new PointF();

    private String mSwipeOn, mScreenBrightness;

    private Listener mListener;

    public void setListener(Listener value) {
        mListener = value;
    }

    private Point getDisplaySize() {
        Point size = new Point();
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else {
            //noinspection deprecation
            size.y = display.getHeight();
            //noinspection deprecation
            size.x = display.getWidth();
        }
        return size;
    }
    private int getDisplayHeight() {
        return getDisplaySize().y;
    }

    private final int[][] mThemeColors = {
            { R.color.bg_light, R.color.gray_light, R.color.track_light, R.color.blue_light, R.color.gray_light, R.color.yellow_light },
            { R.color.bg_dark, R.color.gray_dark, R.color.track_dark, R.color.blue_dark, R.color.gray_dark, R.color.yellow_dark },
    };
    private final RectF[] mThemes = new RectF[mThemeColors.length];

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawRect(mThemes[0], mPaint);
//        canvas.drawRect(mThemes[1], mPaint);

        for (int i = 0; i < mThemes.length; i++) {
            // Draw shadow ...
            mBackgroundLeft.setBounds(Math.round(mThemes[i].left - 24 * mThemes[i].width() / 576), Math.round(mThemes[i].top - 20 * mThemes[i].height() / 1024), Math.round(mThemes[i].left + 29 * mThemes[i].width() / 576), Math.round(mThemes[i].bottom + 34 * mThemes[i].height() / 1024));
            mBackgroundLeft.draw(canvas);

            mBackgroundCenter.setBounds(Math.round(mThemes[i].left + 29 * mThemes[i].width() / 576), Math.round(mThemes[i].top - 20 * mThemes[i].height() / 1024), Math.round(mThemes[i].right - 29 * mThemes[i].width() / 576), Math.round(mThemes[i].bottom + 34 * mThemes[i].height() / 1024));
            mBackgroundCenter.draw(canvas);

            mBackgroundLeft.setBounds(Math.round(mThemes[i].right - 29 * mThemes[i].width() / 576), Math.round(mThemes[i].top - 20 * mThemes[i].height() / 1024), Math.round(mThemes[i].right + 24 * mThemes[i].width() / 576), Math.round(mThemes[i].bottom + 34 * mThemes[i].height() / 1024));
            Rect bounds = mBackgroundLeft.getBounds();
            canvas.scale(-1, 1, bounds.exactCenterX(), bounds.exactCenterY());
            mBackgroundLeft.draw(canvas);
            canvas.scale(-1, 1, bounds.exactCenterX(), bounds.exactCenterY());

            // Draw the background
            mPaint.setColor(mThemeColors[i][0]);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(mThemes[i], mPaint);

            // Draw divider line ...
            mPaint.setStrokeCap(Paint.Cap.BUTT);
            mPaint.setColor(mThemeColors[i][1]);
            // Draw the divider line ...
            for (float x = mThemes[i].left; x < mThemes[i].right; x += mPaint.getStrokeWidth() * 6) {
                canvas.drawLine(x, mThemeCenterLineY, Math.min(mThemes[i].right, x + mPaint.getStrokeWidth() * 4), mThemeCenterLineY, mPaint);
            }

            float topAndBottomHeight = mThemeCenterLineY - mPaint.getStrokeWidth()/2-mThemes[i].top;

            // Draw the track ...
            mPaint.setColor(mThemeColors[i][2]);
            mPaint.setStyle(Paint.Style.FILL);
            mTmpRectF.set(mThemes[i].left + mThemePadding, mThemes[i].top + (topAndBottomHeight - mTrackHeight) / 2, mThemes[i].right - mThemePadding, mThemes[i].top + (topAndBottomHeight + mTrackHeight) / 2);
            mTrackLeft.setBounds(Math.round(mTmpRectF.left - 39 * mTrackHeight / 272), Math.round(mTmpRectF.top - 24 * mTrackHeight / 272), Math.round(mTmpRectF.left + mTrackHeight), Math.round(mTmpRectF.bottom + 56 * mTrackHeight / 272));
            mTrackTop.setBounds(Math.round(mTmpRectF.left + mTrackHeight), Math.round(mTmpRectF.top - 24 * mTrackHeight / 272), Math.round(mTmpRectF.right - mTrackHeight), Math.round(mTmpRectF.top));
            mTrackRight.setBounds(Math.round(mTmpRectF.right - mTrackHeight), Math.round(mTmpRectF.top - 24 * mTrackHeight / 272), Math.round(mTmpRectF.right + 39 * mTrackHeight / 272), Math.round(mTmpRectF.bottom + 56 * mTrackHeight / 272));
            mTrackBottom.setBounds(Math.round(mTmpRectF.left + mTrackHeight), (int)Math.floor(mTmpRectF.bottom), Math.round(mTmpRectF.right - mTrackHeight), Math.round(mTmpRectF.bottom + 56 * mTrackHeight / 272));
            mTrackLeft.draw(canvas);
            mTrackTop.draw(canvas);
            mTrackBottom.draw(canvas);
            bounds = mTrackRight.getBounds();
            float cX = bounds.exactCenterX();
            float cY = bounds.exactCenterY();
            canvas.rotate(-180, cX, cY);
            mTrackRight.draw(canvas);
            canvas.rotate(-180, cX, cY);
            canvas.drawRoundRect(mTmpRectF, mTrackHeight / 2, mTrackHeight / 2, mPaint);

            // Draw the slider
            mTmpRectF.right = mTmpRectF.left + mTrackHeight;
            mSliderShadow.setBounds(Math.round(mTmpRectF.left - mShadowOffset), Math.round(mTmpRectF.top - 5 * mShadowOffset / 18), Math.round(mTmpRectF.right + mShadowOffset), Math.round(mTmpRectF.bottom + 22 * mShadowOffset / 18));
            mSliderShadow.draw(canvas);
            canvas.drawRoundRect(mTmpRectF, mTrackHeight / 2, mTrackHeight / 2, mPaint);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mThemeColors[i][3]);
            canvas.drawRoundRect(mTmpRectF, mTrackHeight / 2, mTrackHeight / 2, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            int initialState = canvas.save();
            canvas.translate(mTmpRectF.centerX(), mTmpRectF.centerY());
            canvas.scale(mTrackHeight / 400, mTrackHeight / 400);
            canvas.translate(-200, -200);
            canvas.drawPath(mFlashlightPath, mPaint);
            canvas.restoreToCount(initialState);

            // Swipe On -->
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(mSwipeOn, mTmpRectF.left + mSwipeOnPos.x, mTmpRectF.top + mSwipeOnPos.y, mPaint);
            float y = mTmpRectF.top + mSwipeOnArrow.centerY(), right = mTmpRectF.left + mSwipeOnArrow.right;
            canvas.drawLine(mTmpRectF.left + mSwipeOnArrow.left, y, mTmpRectF.left + mSwipeOnArrow.right, y, mPaint);
            canvas.drawLine(right - mSwipeOnArrow.height() / 2f, mTmpRectF.top + mSwipeOnArrow.top, right, y, mPaint);
            canvas.drawLine(right - mSwipeOnArrow.height() / 2f, mTmpRectF.top + mSwipeOnArrow.bottom, right, y, mPaint);

            mPaint.setColor(mThemeColors[i][4]);
            canvas.drawText(mScreenBrightness, mThemes[i].left + mScreenBrightnessPos.x, mScreenBrightnessPos.y, mPaint);

            canvas.drawLine(mThemes[i].left + mThemePadding, mSliderY, mThemes[i].right - mThemePadding, mSliderY, mPaint);

            mPaint.setColor(mThemeColors[i][5]);
            mSunShadow.draw(canvas);

            mTmpRectF.set(mYellowBounds);
            mTmpRectF.offset(mThemes[i].left, 0);
            canvas.drawRoundRect(mTmpRectF, mSliderHeight / 2, mSliderHeight / 2, mPaint);

            float theta = (BrightnessControl.START_ANGLE + 630) * 0.5F;
            cX = mTmpRectF.centerX();
            cY = mTmpRectF.centerY();
            canvas.save();
            canvas.rotate(theta, cX, cY);
            final float strokeWidthMultiplier = 2;
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() / strokeWidthMultiplier);
            mPaint.setColor(mThemeColors[i][4]);

            canvas.drawLine(mTmpRectF.left + 1.476F * mSliderHeight / 2, mTmpRectF.top + mSliderHeight / 2, mTmpRectF.left + 1.62F * mSliderHeight / 2, mTmpRectF.top + mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + 1.3365828278448F * mSliderHeight / 2, mTmpRectF.top + 1.3365828278448F * mSliderHeight / 2, mTmpRectF.left + 1.43840620433566F * mSliderHeight / 2, mTmpRectF.top + 1.43840620433566F * mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + mSliderHeight / 2, mTmpRectF.top + 1.476F * mSliderHeight / 2, mTmpRectF.left + mSliderHeight / 2, mTmpRectF.top + 1.62F * mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + 0.663417172155203F * mSliderHeight / 2, mTmpRectF.top + 1.3365828278448F * mSliderHeight / 2, mTmpRectF.left + 0.56159379566434F * mSliderHeight / 2, mTmpRectF.top + 1.43840620433566F * mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + 0.524F * mSliderHeight / 2, mTmpRectF.top + mSliderHeight / 2, mTmpRectF.left + 0.38F * mSliderHeight / 2, mTmpRectF.top + mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + 0.663417172155203F * mSliderHeight / 2, mTmpRectF.top + 0.663417172155203F * mSliderHeight / 2, mTmpRectF.left + 0.56159379566434F * mSliderHeight / 2, mTmpRectF.top + 0.56159379566434F * mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + mSliderHeight / 2, mTmpRectF.top + 0.524F * mSliderHeight / 2, mTmpRectF.left + mSliderHeight / 2, mTmpRectF.top + 0.38F * mSliderHeight / 2, mPaint);
            canvas.drawLine(mTmpRectF.left + 1.3365828278448F * mSliderHeight / 2, mTmpRectF.top + 0.663417172155203F * mSliderHeight / 2, mTmpRectF.left + 1.43840620433566F * mSliderHeight / 2, mTmpRectF.top + 0.56159379566434F * mSliderHeight / 2, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mTmpRectF.set(mTmpRectF.left + 0.31F * mSliderHeight, mTmpRectF.top + 0.31F * mSliderHeight, mTmpRectF.right - 0.31F * mSliderHeight, mTmpRectF.bottom - 0.31F * mSliderHeight);
            canvas.drawRoundRect(mTmpRectF, mTmpRectF.width() / 2, mTmpRectF.height() / 2, mPaint);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.restore();
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() * strokeWidthMultiplier);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float x = mPadding;
        float themeWidth = getThemeWidth(w);
        float themeHeight = h - mPadding * 2;
        mThemeCenterLineY = mPadding + themeHeight / 2;

        for (RectF mTheme : mThemes) {
            mTheme.set(x, mPadding, x + themeWidth, mPadding + themeHeight);
            x += themeWidth + mPadding;
        }

        float sizeRatio = themeHeight / getDisplayHeight();

        mPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.line_thickness) * sizeRatio);
        mThemePadding = mPadding * sizeRatio;
        mTrackHeight = getResources().getDimension(R.dimen.slider_height) * sizeRatio;

        mPaint.setTextSize(getResources().getDimension(R.dimen.text_size) * sizeRatio);

        Rect swipeOnBounds = new Rect();
        mPaint.getTextBounds(mSwipeOn, 0, mSwipeOn.length(), swipeOnBounds);

        float[] spaceWidth = new float[1];
        mPaint.getTextWidths(" ", spaceWidth);

        float extraSpace = themeWidth - mPadding - mTrackHeight - swipeOnBounds.width() - spaceWidth[0]*LightSwitch.SPACE_WIDTH_RATIO - mArrowWidth * sizeRatio;

        mSwipeOnArrow.right = themeWidth - mPadding - extraSpace / 2;
        mSwipeOnArrow.left = mSwipeOnArrow.right - mArrowWidth * sizeRatio;

        mSwipeOnArrow.top = (mTrackHeight - mArrowHeight * sizeRatio) / 2f;
        mSwipeOnArrow.bottom = mSwipeOnArrow.top + mArrowHeight * sizeRatio;

        mSwipeOnPos.x = mSwipeOnArrow.left - spaceWidth[0]*LightSwitch.SPACE_WIDTH_RATIO - swipeOnBounds.width();
        mSwipeOnPos.y = mTrackHeight / 2 - mPaint.ascent() * 0.35f;

        mShadowOffset = (getResources().getDimension(R.dimen.slider_height) * 308 / 272 * sizeRatio - mTrackHeight) / 2;
        float mBrightnessVPadding = getResources().getDimension(R.dimen.brightness_v_padding) * sizeRatio;
        mSliderHeight = getResources().getDimension(R.dimen.brightness_slider_height) * sizeRatio;

        float topAndBottomHeight = (themeHeight - mPaint.getStrokeWidth())/2;

        Rect bounds = new Rect();
        mPaint.getTextBounds(mScreenBrightness, 0, mScreenBrightness.length(), bounds);

        int gap = (int)Math.floor(mPadding + (themeHeight + topAndBottomHeight - bounds.height() - mBrightnessVPadding - mSliderHeight) / 2f);

        mScreenBrightnessPos.x = (themeWidth - bounds.right) / 2;
        mScreenBrightnessPos.y = -mPaint.ascent() - mPaint.descent() + gap;

        mSliderY = mScreenBrightnessPos.y + mPaint.descent() + mBrightnessVPadding + mSliderHeight / 2;

        mYellowBounds.set(0.50F * (themeWidth - mSliderHeight), (float)Math.floor(mSliderY - mSliderHeight / 2), 0.50F * (themeWidth + mSliderHeight), (float) Math.ceil(mSliderY + mSliderHeight / 2));
        mSunShadow.setBounds(0, (int) Math.floor(mSliderY - mSliderHeight / 2 - 12 * mSliderHeight / 194f), 0, (int) Math.floor(mSliderY + mSliderHeight / 2 + 24 * mSliderHeight / 194f));
    }
    private float getThemeWidth(int totalWidth) {
        return (totalWidth - (float)mPadding*(mThemes.length+1))/mThemes.length;
    }
    private int getIdealHeight(int width) {
        Point size = getDisplaySize();
        float sizeRatio = size.y >= size.x ? SIZE_RATIO : (1/SIZE_RATIO);

        return (int)Math.ceil(getThemeWidth(width) * sizeRatio) + mPadding * 2;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new RuntimeException("Width should be set to an exact value.");
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                setMeasuredDimension(width, Math.min(getIdealHeight(width), MeasureSpec.getSize(heightMeasureSpec)));
                break;
            case MeasureSpec.EXACTLY:
                setMeasuredDimension(width, MeasureSpec.getSize(heightMeasureSpec));
                break;
            case MeasureSpec.UNSPECIFIED:
                setMeasuredDimension(width, getIdealHeight(width));
                break;
            default:
                throw new RuntimeException("Unsupported MeasureSpec");
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mThemes.length; i++) {
                    if (mThemes[i].contains(event.getX(), event.getY())) {
                        mTouchActionIndex = MotionEventCompat.getActionIndex(event);
                        mTouchTheme = i;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchActionIndex == MotionEventCompat.getActionIndex(event)) {
                    if (mThemes[mTouchTheme].contains(event.getX(), event.getY())) {
                        return true;
                    } else {
                        mTouchActionIndex = -1;
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchActionIndex == MotionEventCompat.getActionIndex(event)) {
                    mTouchActionIndex = -1;
                    if (mThemes[mTouchTheme].contains(event.getX(), event.getY())) {
                        Preferences.setTheme(mTouchTheme);

                        if (mListener != null) {
                            mListener.onThemeSelected(mTouchTheme, event);
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
    public interface Listener {
        void onThemeSelected(int theme, MotionEvent event);
    }
}
