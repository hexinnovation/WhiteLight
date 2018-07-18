package com.hexinnovation.flashlight;

public class BezierAnimation implements IBezier {
	public BezierAnimation(float x1, float y1, float x2, float y2, float initialValue, float endValue, long startOffset, long duration) {
		mX1 = x1;
		mY1 = y1;
		mX2 = x2;
		mY2 = y2;
		
		mStartOffset = startOffset;
		mDuration = duration;
		mInitialValue = initialValue;
		mEndValue = endValue;
		refreshChangeOfValue();
	}
	public static BezierAnimation easeIn(float initialValue, float endValue, long startOffset, long duration) {
		return new BezierAnimation(0.42F, 0, 1, 1, initialValue, endValue, startOffset, duration);
	}
	public static BezierAnimation easeOut(float initialValue, float endValue, long startOffset, long duration) {
		return new BezierAnimation(0, 0, 0.58F, 1, initialValue, endValue, startOffset, duration);
	}
	public static BezierAnimation easeInOut(float initialValue, float endValue, long startOffset, long duration) {
		return new BezierAnimation(0.42F, 0, 0.58F, 1, initialValue, endValue, startOffset, duration);
	}
	public void startAnimation(long time) {
		mAnimationStartTime = time;
	}
	public void setEndValue(float value) {
		mEndValue = value;
		refreshChangeOfValue();
	}
	private void refreshChangeOfValue() {
		mChangeOfValue = mEndValue - mInitialValue;
	}
	public float getCurrentValue() {
		if (mHasEnded) {
			return mEndValue;
		}
		
		if (mAnimationStartTime == null) {
			mAnimationStartTime = System.currentTimeMillis();
			return mInitialValue;
		}
		
		long currentTime = System.currentTimeMillis() - mAnimationStartTime;
		if (currentTime < mStartOffset) {
			return mInitialValue;
		}
		
		if (currentTime > mStartOffset + mDuration) {
			mHasEnded = true;
			return mEndValue;
		}
		
		// What percent of the time through are we?
		float percentTimeElapsed = (currentTime - mStartOffset) / (float)mDuration;
		
		float t = percentTimeElapsed;
		float denominator = 6F;

		while (denominator <= 162) {
			float current = getX(t);
			float last = current;
			float lastDir;

			if (current == percentTimeElapsed) {
				break;
			}

			if (current < percentTimeElapsed) {
				while (current < percentTimeElapsed) {
					last = current;
					current = getX(t += 1 / denominator);
				}
				lastDir = -1;
			} else {
				while (current > percentTimeElapsed) {
					last = current;
					current = getX(t -= 1 / denominator);
				}
				lastDir = 1;
			}

			if (Math.abs(last - percentTimeElapsed) < Math.abs(current - percentTimeElapsed)) {
				t += lastDir / denominator;
			}

			denominator *= 3;
		}

		return mInitialValue + getY(t) * mChangeOfValue;
	}
	private float getX(float t) {
		return getValue(t, mX1, mX2);
	}
	private float getY(float t) {
		return getValue(t, mY1, mY2);
	}
	private float getValue(float t, float p1, float p2) {
		return t*t*t*(1-3*p2+3*p1) + t*t*(3*p2-6*p1) + t*(3*p1);
	}

	
	public boolean hasEnded() {
		return mHasEnded;
	}
	private boolean mHasEnded;
	
	
	private Long mAnimationStartTime;
	private final long mStartOffset;
	private final long mDuration;
	private float mEndValue, mChangeOfValue;
	private final float mInitialValue, mX1, mX2, mY1, mY2;
}
