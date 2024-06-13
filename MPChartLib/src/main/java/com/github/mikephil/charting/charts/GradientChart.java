package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.renderer.GradientXAxisRenderer;

import java.util.List;

public class GradientChart extends LineChart {
	private Paint paint;

	public GradientChart(Context context) {
		super(context);
	}

	public GradientChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GradientChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init() {
		super.init();
		setXAxisRenderer(new GradientXAxisRenderer(this, getViewPortHandler(), getXAxis(), getTransformer(YAxis.AxisDependency.RIGHT)));
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (mData != null) {
			List<Integer> dataColors = mData.getDataSetByIndex(0).getColors();
			int[] colors = new int[dataColors.size()];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = dataColors.get(i);
			}
			LinearGradient linearGradient = new LinearGradient(0, 0, mViewPortHandler.getChartWidth(), 0, colors, null, Shader.TileMode.CLAMP);
			paint = new Paint();
			paint.setDither(true);
			paint.setShader(linearGradient);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mData != null) {
			if (mAutoScaleMinMaxEnabled) {
				autoScale();
			}
			mXAxis.setEnabled(true);

			if (mXAxis.isEnabled()) {
				mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false);
			}

			int clipRestoreCount = canvas.save();
			if (isClipDataToContentEnabled()) {
				canvas.clipRect(mViewPortHandler.getContentRect());
			}

			if (paint != null) {
				canvas.drawRect(mViewPortHandler.getContentRect(), paint);
			}

			canvas.restoreToCount(clipRestoreCount);

			mXAxisRenderer.renderAxisLabels(canvas);
			if (isClipValuesToContentEnabled()) {
				clipRestoreCount = canvas.save();
				canvas.clipRect(mViewPortHandler.getContentRect());
				mRenderer.drawValues(canvas);
				canvas.restoreToCount(clipRestoreCount);
			} else {
				mRenderer.drawValues(canvas);
			}

			mXAxisRenderer.renderGridLines(canvas);
			mXAxisRenderer.renderAxisLine(canvas);
		}
	}
}
