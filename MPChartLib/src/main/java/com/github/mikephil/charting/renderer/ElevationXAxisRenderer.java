package com.github.mikephil.charting.renderer;

import static com.github.mikephil.charting.charts.ElevationChart.GRID_LINE_LENGTH_X_AXIS_DP;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;

import com.github.mikephil.charting.charts.ElevationChart;
import com.github.mikephil.charting.components.XAxis;
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
		RectF rectF = new RectF(mViewPortHandler.contentLeft(), mViewPortHandler.contentTop(), mViewPortHandler.contentRight(),
				mViewPortHandler.contentBottom() + Utils.dpToPx(mChart.getContext(), GRID_LINE_LENGTH_X_AXIS_DP / 2));
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
		gridLinePath.moveTo(x, mViewPortHandler.contentBottom() + Utils.dpToPx(mChart.getContext(), 5f));
		gridLinePath.lineTo(x, mViewPortHandler.contentTop());
		c.drawPath(gridLinePath, mGridPaint);
		gridLinePath.reset();
	}
}
