
package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.renderer.LineChartRenderer;

import java.lang.ref.WeakReference;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Chart that draws lines, surfaces, circles, ...
 *
 * @author Philipp Jahoda
 */
public class LineChart extends BarLineChartBase<LineData> implements LineDataProvider {

    private static final boolean SHOW_LABELS_ON_START = true;
    
    private YAxisLabelView yAxisLabelView;

    public LineChart(Context context) {
        super(context);
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        mRenderer = new LineChartRenderer(this, mAnimator, mViewPortHandler);
    }

    public void setYAxisLabelView(@Nullable YAxisLabelView labelView) {
        yAxisLabelView = labelView;
    }

    @Override
    public LineData getLineData() {
        return mData;
    }

    @Override
    protected void onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
            ((LineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void renderYAxisLabels(@NonNull Canvas canvas) {

        if (yAxisLabelView == null) {
            super.renderYAxisLabels(canvas);
            return;
        } else if (!checkYAxisData()) {
            return;
        }

        float[] positions = mAxisRendererRight.getTransformedPositions();
        for (int i = 0; i < mAxisLeft.mEntryCount; i++) {
            yAxisLabelView.updateLabel(i);
            yAxisLabelView.resizeLabel();
            float x = SHOW_LABELS_ON_START ? 0 : mViewPortHandler.contentRight() - yAxisLabelView.getWidth();
            boolean bottomLabel = mAxisLeft.mAxisMinimum == mAxisLeft.mEntries[i];
            float yOffset = bottomLabel ? yAxisLabelView.getHeight() : yAxisLabelView.getHeight() / 2f;
            float y = positions[i * 2 + 1] - yOffset;
            yAxisLabelView.draw(canvas, x, y);
        }
    }

    private boolean checkYAxisData() {
        int enabledYAxis = 0;
        if (mAxisLeft.isEnabled() && mAxisLeft.isDrawLabelsEnabled()) {
            enabledYAxis++;
        }
        if (mAxisRight.isEnabled() && mAxisRight.isDrawLabelsEnabled()) {
            enabledYAxis++;
        }
        return mData != null && mData.getDataSetCount() == enabledYAxis;
    }

    public abstract static class YAxisLabelView extends FrameLayout {

        private WeakReference<LineChart> chartRef;

        public YAxisLabelView(@NonNull Context context, @LayoutRes int layoutId) {
            super(context);
            inflate(layoutId);
        }

        public YAxisLabelView(@NonNull Context context, @Nullable AttributeSet attrs, @LayoutRes int layoutId) {
            super(context, attrs);
            inflate(layoutId);
        }

        public YAxisLabelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                              @LayoutRes int layoutId) {
            super(context, attrs, defStyleAttr);
            inflate(layoutId);
        }

        public YAxisLabelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                              int defStyleRes, @LayoutRes int layoutId) {
            super(context, attrs, defStyleAttr, defStyleRes);
            inflate(layoutId);
        }

        private void inflate(@LayoutRes int layoutId) {
            View view = LayoutInflater.from(getContext()).inflate(layoutId, this);
            view.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        public abstract void updateLabel(int labelIndex);

        private void resizeLabel() {
            measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }

        public void draw(@NonNull Canvas canvas, float posX, float posY) {
            int saveId = canvas.save();
            canvas.translate(posX, posY);
            draw(canvas);
            canvas.restoreToCount(saveId);
        }

        @Nullable
        public LineChart getChart() {
            return chartRef != null ? chartRef.get() : null;
        }

        public void setChart(@NonNull LineChart chart) {
            chartRef = new WeakReference<>(chart);
        }
    }
}
