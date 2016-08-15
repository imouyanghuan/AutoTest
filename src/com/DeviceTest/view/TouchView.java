package com.DeviceTest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Arrays;

public class TouchView extends View {
	private static final String TAG = "TouchView";
	private Paint mPaint[] = new Paint[2];
	private RectF mScratchRect;
	private Grid mGrid;

	class Grid {
		static final int GRID_SIZE = 120;

		private final int mXNum;
		private final int mYNum;
		private int mGridData[];
		private int mXOffset[];
		private int mYOffset[];

		Grid(int canvasWidth, int canvasHeight) {
			mXNum = canvasWidth / GRID_SIZE;
			mYNum = canvasHeight / GRID_SIZE;

			int remain, k;
			int i;

			// set X offset
			remain = canvasWidth - getXNum() * GRID_SIZE;
			mXOffset = new int[getXNum() + 1];
			k = Math.max(remain / getXNum(), 1);
			for (i = 1; i < getXNum(); i++) {
				mXOffset[i] = mXOffset[i - 1] + GRID_SIZE;
				if (remain > 0) {
					mXOffset[i] += k;
					remain -= k;
				}
			}
			mXOffset[getXNum()] = canvasWidth;

			// set Y offset
			remain = canvasHeight - mYNum * GRID_SIZE;
			k = Math.max(remain / mYNum, 1);
			mYOffset = new int[mYNum + 1];
			for (i = 1; i < mYNum; i++) {
				mYOffset[i] = mYOffset[i - 1] + GRID_SIZE;
				if (remain > 0) {
					mYOffset[i] += k;
					remain -= k;
				}
			}
			mYOffset[mYNum] = canvasHeight;

			mGridData = new int[getXNum() * mYNum];
		}

		void reset() {
			Arrays.fill(mGridData, 0);
		}

		int setState(float pixelX, float pixelY, int newState) {
			int i;
			int x = -1, y = -1;
			for (i = 0; i < getXNum(); i++) {
				if (pixelX >= mXOffset[i] && pixelX < mXOffset[i + 1]) {
					x = i;
					break;
				}
			}
			for (i = 0; i < mYNum; i++) {
				if (pixelY >= mYOffset[i] && pixelY < mYOffset[i + 1]) {
					y = i;
					break;
				}
			}
			if (x >= 0 && y >= 0)
				return setState(x, y, newState);
			else
				return -1;
		}

		int setState(int gridX, int gridY, int newState) {
			int oldState;
			int idx;
			if (gridX >= getXNum())
				gridX = getXNum() - 1;
			if (gridY >= mYNum)
				gridY = mYNum - 1;
			idx = gridY * getXNum() + gridX;
			oldState = mGridData[idx];
			mGridData[idx] = newState;
			return oldState;
		}

		int getState(int gridX, int gridY) {
			return mGridData[gridY * getXNum() + gridX];
		}

		void setRect(int gridX, int gridY, RectF rect) {
			rect.set(mXOffset[gridX], mYOffset[gridY], mXOffset[gridX + 1] - 1,
					mYOffset[gridY + 1] - 1);
		}

		public int getXNum() {
			return mXNum;
		}

		public int getYNum() {
			return mYNum;
		}
	}

	public interface OnRectangleChangeListener {
		public void onRectangleChange(int newRectangleCount);
	}

	private OnRectangleChangeListener onRectangleChangeListener;

	public void setOnRectangleChangeListener(
			OnRectangleChangeListener onRectangleChangeListener) {
		this.onRectangleChangeListener = onRectangleChangeListener;
	}

	public TouchView(Context context) {
		super(context);
		initView();
	}

	public TouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	protected void initView() {
		Paint paint;
		mPaint[0] = paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);

		mPaint[1] = paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		// paint.setAlpha(0x80);

		mScratchRect = new RectF(0, 0, 0, 0);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawARGB(0xff, 0xd3, 0xd3, 0xd3);
		if (mGrid == null)
			return;

		int countTouchRectangle = 0;
		int state;
		for (int gridY = 0; gridY < mGrid.getYNum(); gridY++) {
			for (int gridX = 0; gridX < mGrid.getXNum(); gridX++) {
				mGrid.setRect(gridX, gridY, mScratchRect);
				state = mGrid.getState(gridX, gridY);
				canvas.drawRect(mScratchRect, mPaint[state]);
				countTouchRectangle = countTouchRectangle + state;
			}
		}
		if (onRectangleChangeListener != null) {
			onRectangleChangeListener.onRectangleChange(countTouchRectangle);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			result = 320;
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}

		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text (beware: ascent is a negative number)
			result = 240;
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mGrid == null)
			return false;
		boolean doInvalidate = false;
		final int pointerCount = ev.getPointerCount();

		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			final int historySize = ev.getHistorySize();
			for (int h = 0; h < historySize; h++) {
				for (int p = 0; p < pointerCount; p++) {
					if (mGrid.setState(ev.getHistoricalX(p, h),
							ev.getHistoricalY(p, h), 1) == 0)
						doInvalidate = true;
				}
			}
			// fall through
		case MotionEvent.ACTION_DOWN:
			if (mGrid.setState(ev.getX(), ev.getY(), 1) == 0)
				doInvalidate = true;
			// if (btns != null)
			// btns.setVisibility(View.GONE);
			break;
		case MotionEvent.ACTION_UP:
			// if (btns != null)
			// btns.setVisibility(View.VISIBLE);
			break;
		default:
			return false;
		}
		if (doInvalidate)
			invalidate();
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		Log.e(TAG, "onLayout " + changed + ", " + left + ", " + top + ", "
				+ right + ", " + bottom);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.e(TAG, "onSizeChanged" + w + ", " + h + ", " + oldw + ", " + oldh);
		if (w > 0 && h > 0) {
			mGrid = new Grid(w, h);
		}
	}

	public void reset() {
		mGrid.reset();
		invalidate();
	}

	private LinearLayout btns;

	public void setBtnsLinearLayout(LinearLayout btns) {
		this.btns = btns;
	}
}
