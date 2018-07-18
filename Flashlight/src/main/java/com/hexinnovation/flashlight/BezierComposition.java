package com.hexinnovation.flashlight;

public class BezierComposition implements IBezier {
	private BezierAnimation[] mAnimations;
	
	public BezierComposition(long offset, float... valuesAndTimes) {
		if (valuesAndTimes.length % 2 == 1) {
			throw new RuntimeException("Mismatched values and times counts.");
		}
		
		float[] values = new float[valuesAndTimes.length / 2];
		float[] times = new float[valuesAndTimes.length / 2];
		
		for (int i = 0; i < values.length; i++) {
			values[i] = valuesAndTimes[i*2];
			times[i] = valuesAndTimes[i*2+1];
		}
		
		construct(offset, values, times);
	}
//	public BezierComposition(long offset, float[] values, float[] times) {
//		construct(offset, values, times);
//	}
	
	private void construct(long offset, float[] values, float[] times) {
		if (values.length != times.length) {
			throw new RuntimeException("Mismatched values and times counts.");
		}
		
		mAnimations = new BezierAnimation[values.length - 1];
		
		for (int i = 0; i < mAnimations.length; i++) {
			mAnimations[i] = BezierAnimation.easeInOut(values[i], values[i+1], offset + (long)times[i], (long)(times[i+1]-times[i]));
		}
	}
	
	private int mCurrentAnimation = -1;
	public float getCurrentValue() {
		if (mCurrentAnimation == -1) {
			mCurrentAnimation = 0;
			long startTime = System.currentTimeMillis();
			for (BezierAnimation mAnimation : mAnimations) {
				mAnimation.startAnimation(startTime);
			}
		}
		
		BezierAnimation currentAnimation = mAnimations[mCurrentAnimation]; 
		
		float val = currentAnimation.getCurrentValue();
		
		while (currentAnimation.hasEnded() && mCurrentAnimation < mAnimations.length - 1){
			val = (currentAnimation = mAnimations[++mCurrentAnimation]).getCurrentValue();
		}
		
		return val;
	}
	public boolean hasEnded() {
		return mCurrentAnimation == mAnimations.length - 1 && mCurrentAnimation >= 0 && mAnimations[mCurrentAnimation].hasEnded();
	}
}
