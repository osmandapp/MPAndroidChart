package com.github.mikephil.charting.renderer;

import static com.github.mikephil.charting.charts.ElevationChart.GRID_LINE_LENGTH_X_AXIS_DP;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;

import com.github.mikephil.charting.charts.ElevationChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ElevationXAxisRenderer extends XAxisRenderer {
	private final ElevationChart mChart;

	public ElevationXAxisRenderer(ElevationChart mChart, ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
		super(viewPortHandler, xAxis, trans);
		this.mChart = mChart;
	}

	@Override
	public RectF getGridClippingRect() {
		RectF rectF = new RectF(mViewPortHandler.contentLeft(), mViewPortHandler.contentTop(), mViewPortHandler.contentRight(), mViewPortHandler.contentBottom() + Utils.dpToPx(mChart.getContext(), GRID_LINE_LENGTH_X_AXIS_DP / 2));
		mGridClippingRect.set(mViewPortHandler.getContentRect());
		mGridClippingRect.inset(-mAxis.getGridLineWidth(), 0.0F);
		return rectF;
	}

	@Override
	public void renderAxisLine(Canvas c) {
		if (mXAxis.isDrawAxisLineEnabled() && mXAxis.isEnabled()) {
			mAxisLinePaint.setColor(mXAxis.getAxisLineColor());
			mAxisLinePaint.setStrokeWidth(mXAxis.getAxisLineWidth());
			mAxisLinePaint.setPathEffect(mXAxis.getAxisLineDashPathEffect());

			c.drawLine(mChart.getExtraLeftOffset(), mViewPortHandler.contentBottom(), c.getWidth() - mChart.getExtraRightOffset(), mViewPortHandler.contentBottom(), mAxisLinePaint);
		}
	}

	@Override
	protected void drawGridLine(Canvas c, float x, float y, Path gridLinePath) {
		gridLinePath.moveTo(x, mViewPortHandler.contentBottom() + Utils.dpToPx(mChart.getContext(), GRID_LINE_LENGTH_X_AXIS_DP / 2));
		gridLinePath.lineTo(x, mViewPortHandler.contentTop());
		c.drawPath(gridLinePath, mGridPaint);
		gridLinePath.reset();
	}

	@Override
	protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
		float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
		boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();
		float[] positions = new float[mXAxis.mEntryCount * 2];
		int i;
		for (i = 0; i < positions.length; i += 2) {
			if (centeringEnabled) {
				positions[i] = mXAxis.mCenteredEntries[i / 2];
			} else {
				positions[i] = mXAxis.mEntries[i / 2];
			}
		}

		this.mTrans.pointValuesToPixel(positions);

		for (i = 0; i < positions.length; i += 2) {
			float x = positions[i];
			if (mViewPortHandler.isInBoundsX(x)) {
				String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[i / 2], mXAxis);
				if (mXAxis.isAvoidFirstLastClippingEnabled()) {
					float width;
					if (i / 2 == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
						width = Utils.calcTextWidth(mAxisLabelPaint, label);
						x -= width / 2.0F;
					} else if (i == 0) {
						width = Utils.calcTextWidth(mAxisLabelPaint, label);
						x += width / 2.0F;
					}
				}
				drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees);
			}
		}
	}

	@Override
	protected void computeAxisValues(float min, float max) {
		int labelCount = mAxis.getLabelCount();
		double range = Math.abs(max - min);
		if (labelCount != 0 && !(range <= 0.0) && !Double.isInfinite(range)) {
			double interval = range / labelCount;
			if (mAxis.isGranularityEnabled()) {
				interval = interval < mAxis.getGranularity() ? mAxis.getGranularity() : interval;
			}

			double intervalMagnitude = Utils.roundToNextSignificant(Math.pow(10.0, ((int) Math.log10(interval))));
			int intervalSigDigit = (int) (interval / intervalMagnitude);
			if (intervalSigDigit > 5) {
				interval = Math.floor(10.0 * intervalMagnitude) == 0.0 ? interval : Math.floor(10.0 * intervalMagnitude);
			}

			int n = mAxis.isCenterAxisLabelsEnabled() ? 1 : 0;
			float offset;
			int i;
			if (mAxis.isForceLabelsEnabled()) {
				interval = ((float) range / (float) (labelCount - 1));
				this.mAxis.mEntryCount = labelCount;
				if (mAxis.mEntries.length < labelCount) {
					mAxis.mEntries = new float[labelCount];
				}

				offset = min;

				for (i = 0; i < labelCount; ++i) {
					mAxis.mEntries[i] = offset;
					offset += interval;
				}

				n = labelCount;
			} else {
				double first = interval == 0.0 ? 0.0 : Math.ceil(min / interval) * interval;
				if (mAxis.isCenterAxisLabelsEnabled()) {
					first -= interval;
				}
				double last = interval == 0.0 ? 0.0 : max;
				double f;
				if (interval != 0.0 && last != first) {
					for (f = first; f <= last; f += interval) {
						++n;
					}
				} else if (last == first && n == 0) {
					n = 1;
				}

				mAxis.mEntryCount = n;
				if (mAxis.mEntries.length < n) {
					mAxis.mEntries = new float[n];
				}

				f = first;

				for (int j = 0; j < n; ++j) {
					if (f == 0.0) {
						f = 0.0;
					}

					mAxis.mEntries[j] = (float) f;
					f += interval;
				}
			}

			if (interval < 1.0) {
				mAxis.mDecimals = (int) Math.ceil(-Math.log10(interval));
			} else {
				mAxis.mDecimals = 0;
			}

			if (mAxis.isCenterAxisLabelsEnabled()) {
				if (this.mAxis.mCenteredEntries.length < n) {
					mAxis.mCenteredEntries = new float[n];
				}

				offset = (float) interval / 2.0F;

				for (i = 0; i < n; ++i) {
					mAxis.mCenteredEntries[i] = mAxis.mEntries[i] + offset;
				}
			}

		} else {
			mAxis.mEntries = new float[0];
			mAxis.mCenteredEntries = new float[0];
			mAxis.mEntryCount = 0;
		}

		this.computeSize();
	}

	@Override
	public void renderGridLines(Canvas c) {
		if (mXAxis.isDrawGridLinesEnabled() && mXAxis.isEnabled()) {
			int clipRestoreCount = c.save();
			c.clipRect(getGridClippingRect());
			if (mRenderGridLinesBuffer.length != mAxis.mEntryCount * 2) {
				mRenderGridLinesBuffer = new float[mXAxis.mEntryCount * 2];
			}

			float[] positions = mRenderGridLinesBuffer;

			for (int i = 0; i < positions.length; i += 2) {
				positions[i] = mXAxis.mEntries[i / 2];
				positions[i + 1] = mXAxis.mEntries[i / 2];
			}

			mTrans.pointValuesToPixel(positions);
			setupGridPaint();
			Path gridLinePath = mRenderGridLinesPath;
			gridLinePath.reset();

			for (int i = 0; i < positions.length; i += 2) {
				float x = positions[i];
				if (i / 2 == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
					x = x - mXAxis.getGridLineWidth() / 2;
				} else if (i == 0) {
					x = x + mXAxis.getGridLineWidth() / 2;
				}
				drawGridLine(c, x, positions[i + 1], gridLinePath);
			}
			c.restoreToCount(clipRestoreCount);
		}
	}
}
