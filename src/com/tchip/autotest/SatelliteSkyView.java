package com.tchip.autotest;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.tchip.autotest.R;
import com.mediatek.xlog.Xlog;


public class SatelliteSkyView extends View {

	private static final double RIGHT_ANGLE_D = 90.0;
	private static final double STRAIGHT_ANGLE_D = 180.0;
	private static final int RIGHT_ANGLE = 90;
	private static final int DRAW_MARGIN = 12;
	private static final float GRID_WIDTH = 1.0f;
	private static final float TEXT_SIZE = 15.0f;
	private static final float THREE_QUARTER = 0.75f;
	public static final String TAG = "YGPS/SatelliteSkyView";
	private Paint mGridPaint = null;
	private Paint mTextPaint = null;
	private Paint mBackground = null;
	private Bitmap mSatelliteBitmapUsed = null;
	private Bitmap mSatelliteBitmapUnused = null;
	private Bitmap mSatelliteBitmapNoFix = null;

	private float mBitmapAdjustment = 0;

	private SatelliteDataProvider mProvider = null;
	private int mSatellites = 0;
	private int[] mPrns = new int[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
	private float[] mElevation = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
	private float[] mAzimuth = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
	private float[] mSnrs = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];

	private float[] mX = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
	private float[] mY = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
	private int[] mUsedInFixMask = new int[SatelliteDataProvider.SATELLITES_MASK_SIZE];

	/**
	 * Convert x-ordinate and y-ordinate
	 */
	private void computeXY() {
		for (int i = 0; i < mSatellites; ++i) {
			double theta = -(mAzimuth[i] - RIGHT_ANGLE);
			double rad = theta * Math.PI / STRAIGHT_ANGLE_D;
			mX[i] = (float) Math.cos(rad);
			mY[i] = -(float) Math.sin(rad);

			mElevation[i] = RIGHT_ANGLE - mElevation[i];
		}
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The Context the view is running in, through which it can
	 *            access the current theme, resources, etc.
	 */
	public SatelliteSkyView(Context context) {
		this(context, null);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The Context the view is running in, through which it can
	 *            access the current theme, resources, etc.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the view.
	 */
	public SatelliteSkyView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The Context the view is running in, through which it can
	 *            access the current theme, resources, etc.
	 * @param attrs
	 *            The attributes of the XML tag that is inflating the view.
	 * @param defStyle
	 *            The default style to apply to this view. If 0, no style will
	 *            be applied (beyond what is included in the theme). This may
	 *            either be an attribute resource, whose value will be retrieved
	 *            from the current theme, or an explicit style resource.
	 */
	public SatelliteSkyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources res = getResources();
		if (null != res) {
			mGridPaint = new Paint();
			mGridPaint.setColor(res.getColor(R.color.grid));
			mGridPaint.setAntiAlias(true);
			mGridPaint.setStyle(Style.STROKE);
			mGridPaint.setStrokeWidth(GRID_WIDTH);
			mBackground = new Paint();
			mBackground.setColor(res.getColor(R.color.skyview_background));

			mTextPaint = new Paint();
			mTextPaint.setColor(res.getColor(R.color.skyview_text_color));
			mTextPaint.setTextSize(TEXT_SIZE);
			mTextPaint.setTextAlign(Align.CENTER);

			BitmapDrawable satgreen = (BitmapDrawable) res
					.getDrawable(R.drawable.satgreen);
			if (null != satgreen) {
				mSatelliteBitmapUsed = satgreen.getBitmap();
			} else {
				Xlog.i(TAG,
						"get BitmapDrawable getDrawable(R.drawable.satgreen) failed");
			}
			BitmapDrawable satyellow = (BitmapDrawable) res
					.getDrawable(R.drawable.satyellow);
			if (null != satyellow) {
				mSatelliteBitmapUnused = satyellow.getBitmap();
			} else {
				Xlog.i(TAG,
						"get BitmapDrawable getDrawable(R.drawable.satyellow)) failed");
			}
			BitmapDrawable satred = (BitmapDrawable) res
					.getDrawable(R.drawable.satred);
			if (null != satred) {
				mSatelliteBitmapNoFix = satred.getBitmap();
			} else {
				Xlog.i(TAG, "get BitmapDrawable getDrawable(xxx) failed");
			}

			// mSatelliteBitmapUsed =
			// ((BitmapDrawable)res.getDrawable(R.drawable.satgreen)).getBitmap();
			// mSatelliteBitmapUnused =
			// ((BitmapDrawable)res.getDrawable(R.drawable.satyellow)).getBitmap();
			// mSatelliteBitmapNoFix =
			// ((BitmapDrawable)res.getDrawable(R.drawable.satred)).getBitmap();
			if (null != mSatelliteBitmapUsed) {
				mBitmapAdjustment = mSatelliteBitmapUsed.getHeight() / 2;
			}
		}
	}

	/**
	 * Set data provider
	 * 
	 * @param provider
	 *            Class that implement interface #SatelliteDataProvider
	 * @see #SatelliteDataProvider
	 */
	void setDataProvider(SatelliteDataProvider provider) {
		mProvider = provider;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float centerY = getHeight() / 2;
		float centerX = getWidth() / 2;
		int radius;
		if (centerX > centerY) {
			radius = (int) (getHeight() / 2) - DRAW_MARGIN;
		} else {
			radius = (int) (getWidth() / 2) - DRAW_MARGIN;
		}
		final Paint gridPaint = mGridPaint;
		final Paint textPaint = mTextPaint;
		canvas.drawPaint(mBackground);
		canvas.drawCircle(centerX, centerY, radius, gridPaint);
		canvas.drawCircle(centerX, centerY, radius * THREE_QUARTER, gridPaint);
		canvas.drawCircle(centerX, centerY, radius >> 1, gridPaint);
		canvas.drawCircle(centerX, centerY, radius >> 2, gridPaint);
		canvas.drawLine(centerX, centerY - (radius >> 2), centerX, centerY
				- radius, gridPaint);
		canvas.drawLine(centerX, centerY + (radius >> 2), centerX, centerY
				+ radius, gridPaint);
		canvas.drawLine(centerX - (radius >> 2), centerY, centerX - radius,
				centerY, gridPaint);
		canvas.drawLine(centerX + (radius >> 2), centerY, centerX + radius,
				centerY, gridPaint);
		double scale = radius / RIGHT_ANGLE_D;
		if (mProvider != null) {
			mSatellites = mProvider.getSatelliteStatus(mPrns, mSnrs,
					mElevation, mAzimuth, 0, 0, mUsedInFixMask);
			computeXY();
		}
		for (int i = 0; i < mSatellites; ++i) {
			if (mElevation[i] >= RIGHT_ANGLE || mAzimuth[i] < 0
					|| mPrns[i] <= 0) {
				continue;
			}
			double a = mElevation[i] * scale;
			int x = (int) Math.round(centerX + (mX[i] * a) - mBitmapAdjustment);
			int y = (int) Math.round(centerY + (mY[i] * a) - mBitmapAdjustment);
			// if (0 == (mUsedInFixMask[0]) || mSnrs[i] <= 0) { // red
			if (!isUsedInFix(0) || mSnrs[i] <= 0) {
				canvas.drawBitmap(mSatelliteBitmapNoFix, x, y, gridPaint);
				// } else if (0 != (mUsedInFixMask[0] & (1<<(32-mPnrs[i])))){
				// } else if (0 != (mUsedInFixMask[0] & (1 << (mPrns[i] - 1))))
				// { // green
			} else if (isUsedInFix(mPrns[i])) {
				canvas.drawBitmap(mSatelliteBitmapUsed, x, y, gridPaint);
			} else { // yellow
				canvas.drawBitmap(mSatelliteBitmapUnused, x, y, gridPaint);
			}
			canvas.drawText(Integer.toString(mPrns[i]), x, y, textPaint);
		}
	}

	/**
	 * Check whether the satellite is used in fix
	 * 
	 * @param prn
	 *            PRN of the satellite
	 * @return True if the satellite is used in fix, or false
	 */
	private boolean isUsedInFix(int prn) {
		int innerPrn = prn;
		boolean result = false;
		if (0 >= innerPrn) {
			for (int mask : mUsedInFixMask) {
				if (0 != mask) {
					result = true;
					break;
				}
			}
		} else {
			innerPrn = innerPrn - 1;
			int index = innerPrn
					/ SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH;
			int bit = innerPrn
					% SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH;
			result = (0 != (mUsedInFixMask[index] & (1 << bit)));
		}
		return result;
	}
}
