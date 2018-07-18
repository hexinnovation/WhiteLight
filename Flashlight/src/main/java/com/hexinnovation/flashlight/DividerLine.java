package com.hexinnovation.flashlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;


public class DividerLine extends View implements IAnimateIn {
    public DividerLine(Context context) {
        super(context);
        construct();
    }
    public DividerLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }
    public DividerLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct();
    }
    private void construct() {
        mPaint = new Paint();

        mDashWidth = getResources().getDimensionPixelSize(R.dimen.line_thickness) * 2;
        mStrokeWidth = mDashWidth * 2;
    }
    private Paint mPaint;
    private int mHeight, mWidth;
    private int mStrokeWidth, mDashWidth;

    public void setTheme(int theme) {
        switch (theme) {
            case 0:
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_light));
                break;
            case 1:
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
                break;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float maxX;
        
        if (mInAnimation == null) {
            return;
        } else {
            maxX = mInAnimation.getCurrentValue();
            
            if (!mInAnimation.hasEnded())
                invalidate();
        }

        for (int i = 0; i < maxX; i += mDashWidth + mStrokeWidth) {
            canvas.drawRect(i, 0, Math.min(i + mStrokeWidth, maxX), mHeight, mPaint);
        }
    }
    private BezierAnimation mInAnimation;
    public void animateIn(long startOffset) {
        setVisibility(View.INVISIBLE);
        setVisibility(View.VISIBLE);
        
        mInAnimation = BezierAnimation.easeOut(0, mWidth, startOffset + 221, 267);
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        
        if (mInAnimation != null) {
            mInAnimation.setEndValue(w);
        }
        
        mWidth = w;
        mHeight = h;
    }
}
