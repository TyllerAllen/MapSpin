package com.wlu.cp470.group12.mapspin;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class IntervalSeekBar extends LinearLayout {
    private LinearLayout mLinearLayout = null;
    private SeekBar mSeekBar = null;

    private int WidthMeasureSpec = 0;
    private int HeightMeasureSpec = 0;

    public IntervalSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        getActivity().getLayoutInflater().inflate(R.layout.seekbar_with_intervals, this);
    }

    private Activity getActivity() {
        return (Activity) getContext();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed) {

            // We've changed the intervals layout, we need to refresh.
            mLinearLayout.measure(WidthMeasureSpec, HeightMeasureSpec);
            mLinearLayout.layout(mLinearLayout.getLeft(), mLinearLayout.getTop(), mLinearLayout.getRight(), mLinearLayout.getBottom());
        }
    }

    protected synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        WidthMeasureSpec = widthMeasureSpec;
        HeightMeasureSpec = heightMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getProgress() {
        return getSeekBar().getProgress();
    }

    public void setProgress(int progress) {
        getSeekBar().setProgress(progress);
    }

    public void setIntervals(List<String> intervals) {
        displayIntervals(intervals);
        getSeekBar().setMax(intervals.size() - 1);
    }

    private void displayIntervals(List<String> intervals) {
        if (getLinearLayout().getChildCount() == 0) {
            for (String interval : intervals) {
                TextView textViewInterval = createInterval(interval);

                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                param.weight = 1;

                getLinearLayout().addView(textViewInterval, param);
            }
        }
    }

    private TextView createInterval(String interval) {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.seekbar_with_interval_labels, null);

        TextView textView = (TextView) layout.findViewById(R.id.textViewInterval);
        textView.setText(interval);

        return textView;
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        getSeekBar().setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private LinearLayout getLinearLayout() {
        if (mLinearLayout == null) {
            mLinearLayout = (LinearLayout) findViewById(R.id.intervals);
        }

        return mLinearLayout;
    }

    private SeekBar getSeekBar() {
        if (mSeekBar == null) {
            mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        }

        return mSeekBar;
    }
}