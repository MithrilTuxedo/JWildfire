package org.jwildfire.headless;

import org.jwildfire.create.tina.render.ProgressUpdater;

public class CliUpdater implements ProgressUpdater {

	private float mMaxSteps;
	private String mFilename;
	private boolean mShowProgress;
	
	public void setVolume(int v) {
		if (v > 1) {
			mShowProgress = true;
		} else {
			mShowProgress = false;
		}
	}

	@Override
	public void initProgress(int pMaxSteps) {
		mMaxSteps = (float) pMaxSteps + 1;
	}

	@Override
	public void updateProgress(int pStep) {
		if (!mShowProgress) {
			return;
		}
		System.out.print("\r");
		if (mFilename != null) {
			System.out.printf("%s: ", mFilename); 
		}
		System.out.print('|');
		float percent = ((pStep * 100f) / mMaxSteps) - 0.99f;
		for (int x = 0; x < percent; x++) {
			if (x % 10 == 9) {
				System.out.print('o');
			} else if (x % 5 == 4) {
				System.out.print('~');
			} else {
				System.out.print('-');
			}
		}
		if (percent < 0) {
			System.out.print(' ');
		}
		for (int x = (int) percent; x < 98; x++) {
			if (x % 10 == 8) {
				System.out.print('+');
			} else {
				System.out.print(' ');
			}
		}
		System.out.printf("| [%3.1f%%]", pStep * 100.0 / mMaxSteps);
	}

	public void setName(String filename) {
		mFilename = filename;
	}
}
