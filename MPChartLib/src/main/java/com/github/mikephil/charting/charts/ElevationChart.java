package com.github.mikephil.charting.charts;

import static com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.renderer.ElevationXAxisRenderer;
import com.github.mikephil.charting.renderer.ElevationYAxisRenderer;
import com.github.mikephil.charting.utils.Utils;

import java.util.Iterator;

public class ElevationChart extends LineChart {
	private static final float PADDING_BETWEEN_LABELS_AND_CONTENT_DP = 6;
	public static final float GRID_LINE_LENGTH_X_AXIS_DP = 10;
	public static final int CHART_LABEL_COUNT = 3;

	private boolean showLastSet = true;

	public ElevationChart(Context context) {
		super(context);
	}

	public ElevationChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ElevationChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setShowLastSet(boolean shouldShowLastSet) {
		this.showLastSet = shouldShowLastSet;
	}

	public boolean shouldShowLastSet() {
		return showLastSet;
	}

	@Override
	protected void init() {
		super.init();
		setXAxisRenderer(new ElevationXAxisRenderer(this, getViewPortHandler(), getXAxis(), getTransformer(YAxis.AxisDependency.RIGHT)));
		setRendererRightYAxis(new ElevationYAxisRenderer(this, mViewPortHandler, mAxisRight, mRightAxisTransformer));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateDimens(w, h);
	}

	public void updateDimens() {
		updateDimens(getWidth(), getHeight());
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		updateDimens();
	}

	public void updateDimens(int width, int height) {
		float measureText = getMeasuredMaxLabel();
		Utils.init(getContext());
		mViewPortHandler.setChartDimens((float) width - measureText - Utils.dpToPx(getContext(), PADDING_BETWEEN_LABELS_AND_CONTENT_DP), (float) height);

		Iterator jobIterator = mJobs.iterator();
		while (jobIterator.hasNext()) {
			Runnable r = (Runnable) jobIterator.next();
			post(r);
		}

		mJobs.clear();
	}

	private float getMeasuredMaxLabel() {
		int from = mAxisRight.isDrawBottomYLabelEntryEnabled() ? 0 : 1;
		int to = mAxisRight.isDrawTopYLabelEntryEnabled() ? mAxisRight.mEntryCount : mAxisRight.mEntryCount - 1;

		LineData chartData = getLineData();
		int dataSetCount = chartData.getDataSetCount();
		LineDataSet lastDataSet = dataSetCount > 0 ? (LineDataSet) chartData.getDataSetByIndex(dataSetCount - 1) : null;
		if (lastDataSet != null && !shouldShowLastSet()) {
			dataSetCount--;
		}
		Paint paint = mAxisRendererRight.getPaintAxisLabels();
		float maxMeasuredWidth = 0;
		for (int i = from; i < to; ++i) {
			float measuredLabelWidth = 0;
			if (dataSetCount == 1) {
				String plainText = getAxisRight().getFormattedLabel(i);
				measuredLabelWidth = paint.measureText(plainText);
			} else {
				String leftText = getAxisLeft().getFormattedLabel(i);
				String rightText = getAxisRight().getFormattedLabel(i) + ", ";
				measuredLabelWidth = paint.measureText(rightText) + paint.measureText(leftText);

			}
			if (measuredLabelWidth > maxMeasuredWidth) {
				maxMeasuredWidth = measuredLabelWidth;
			}
		}
		return maxMeasuredWidth;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mData != null) {
			if (mAutoScaleMinMaxEnabled) {
				autoScale();
			}

			if (mAxisRight.isEnabled()) {
				mAxisRendererRight.computeAxis(mAxisRight.mAxisMinimum, mAxisRight.mAxisMaximum, mAxisRight.isInverted());
			}
			mAxisRendererRight.renderGridLines(canvas);

			if (mXAxis.isEnabled()) {
				mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false);
			}
			mXAxisRenderer.renderAxisLine(canvas);
			mXAxisRenderer.renderGridLines(canvas);

			int clipRestoreCount = canvas.save();
			if (isClipDataToContentEnabled()) {
				canvas.clipRect(mViewPortHandler.getContentRect());
			}

			mRenderer.drawData(canvas);
			if (valuesToHighlight()) {
				mRenderer.drawHighlighted(canvas, mIndicesToHighlight);
			}

			canvas.restoreToCount(clipRestoreCount);
			mRenderer.drawExtras(canvas);
			if (mXAxis.isEnabled() && !mXAxis.isDrawLimitLinesBehindDataEnabled()) {
				mXAxisRenderer.renderLimitLines(canvas);
			}

			mXAxisRenderer.renderAxisLabels(canvas);
			renderYAxisLabels(canvas);
			if (isClipValuesToContentEnabled()) {
				clipRestoreCount = canvas.save();
				canvas.clipRect(mViewPortHandler.getContentRect());
				mRenderer.drawValues(canvas);
				canvas.restoreToCount(clipRestoreCount);
			} else {
				mRenderer.drawValues(canvas);
			}

			mLegendRenderer.renderLegend(canvas);
			drawDescription(canvas);
			drawMarkers(canvas);
		}
	}

	public void setupGPXChart(@NonNull MarkerView markerView, float topOffset, float bottomOffset, int labelsColor, int yAxisGridColor, Typeface typeface, boolean useGesturesAndScale) {
		Context context = getContext();

		setExtraRightOffset(16);
		setExtraLeftOffset(16);
		setExtraTopOffset(topOffset);
		setExtraBottomOffset(bottomOffset);

		setHardwareAccelerationEnabled(true);
		setTouchEnabled(useGesturesAndScale);
		setDragEnabled(useGesturesAndScale);
		setScaleEnabled(useGesturesAndScale);
		setPinchZoom(useGesturesAndScale);
		setScaleYEnabled(false);
		setAutoScaleMinMaxEnabled(true);
		setDrawBorders(false);
		getDescription().setEnabled(false);
		setMaxVisibleValueCount(10);
		setMinOffset(0f);
		setDragDecelerationEnabled(false);

		markerView.setChartView(this);
		setMarker(markerView);
		setDrawMarkers(true);

		XAxis xAxis = getXAxis();
		xAxis.setDrawAxisLine(true);
		xAxis.setAxisLineWidth(1);
		xAxis.setAxisLineColor(labelsColor);
		xAxis.setDrawGridLines(true);
		xAxis.setGridLineWidth(1f);
		xAxis.setGridColor(labelsColor);
		xAxis.enableGridDashedLine(Utils.dpToPx(context, GRID_LINE_LENGTH_X_AXIS_DP), Float.MAX_VALUE, 0f);
		xAxis.setPosition(BOTTOM);
		xAxis.setTextColor(labelsColor);

		int dp4 = Utils.dpToPx(context, 4);

		YAxis leftYAxis = getAxisLeft();
		leftYAxis.setEnabled(false);

		YAxis rightYAxis = getAxisRight();
		rightYAxis.enableGridDashedLine(dp4, dp4, 0f);
		rightYAxis.setGridColor(yAxisGridColor);
		rightYAxis.setGridLineWidth(1f);
		rightYAxis.setDrawBottomYGridLine(false);
		rightYAxis.setDrawAxisLine(false);
		rightYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
		rightYAxis.setXOffset(-1f);
		rightYAxis.setYOffset(10.25f);
		rightYAxis.setTypeface(typeface);
		rightYAxis.setTextSize(10f);
		rightYAxis.setLabelCount(CHART_LABEL_COUNT, true);

		Legend legend = getLegend();
		legend.setEnabled(false);
	}
}
