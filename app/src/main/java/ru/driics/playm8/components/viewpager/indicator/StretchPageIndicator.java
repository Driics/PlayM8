package ru.driics.playm8.components.viewpager.indicator;

import android.animation.IntEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import ru.driics.playm8.R;

public class StretchPageIndicator extends ShapePageIndicator {
    private final IntEvaluator evaluator;

    private float activeIndicatorScale;
    private boolean changeIndicatorsColor;

    private int currentPageIndex;
    private float currentPageOffset;

    public StretchPageIndicator(Context context) {
        this(context, null, 0);
    }

    public StretchPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StretchPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.evaluator = new IntEvaluator();
        this.activeIndicatorScale = 1f;

        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.StretchPageIndicator);

        this.activeIndicatorScale = styledAttributes.getFloat(R.styleable.StretchPageIndicator_ds_activeIndicatorScale, 1f);
        this.changeIndicatorsColor = styledAttributes.getBoolean(R.styleable.StretchPageIndicator_ds_changeIndicatorsColor, false);

        styledAttributes.recycle();
    }

    @Override
    protected void onPageScrolled(int position, float positionOffset) {
        super.onPageScrolled(position, positionOffset);
        this.currentPageIndex = position;
        this.currentPageOffset = positionOffset;
        requestLayout();
    }

    @Override
    public int getActiveIndicatorColor() {
        return isChangeIndicatorsColor() ? super.getActiveIndicatorColor() : getIndicatorColor();
    }

    @Override
    public int calculateIndicatorPadding(View indicatorView, int position, Rect indicatorRect, int indicatorSpacing) {
        int indicatorWidth;
        int width = indicatorRect.width();
        int scale = (int) (width * getActiveIndicatorScale());
        int currentPageIndex = this.currentPageIndex;
        if (position == currentPageIndex) {
            indicatorWidth = this.evaluator.evaluate(this.currentPageOffset, scale, width);
        } else if (position == currentPageIndex + 1) {
            indicatorWidth = this.evaluator.evaluate(this.currentPageOffset, width, scale);
        } else {
            indicatorWidth = width;
        }
        int start = indicatorRect.left;
        indicatorRect.right = start + indicatorWidth;
        return super.calculateIndicatorPadding(indicatorView, position, indicatorRect, indicatorSpacing);
    }

    @Override
    public float getItemCount() {
        return (super.getItemCount() + getActiveIndicatorScale()) - 1;
    }

    @Override
    public void setActiveIndicatorColor(int activeIndicatorColor) {
        super.setActiveIndicatorColor(activeIndicatorColor);
    }

    public float getActiveIndicatorScale() {
        return this.activeIndicatorScale;
    }

    public void setActiveIndicatorScale(float activeIndicatorScale) {
        if (this.activeIndicatorScale == activeIndicatorScale) {
            return;
        }

        this.activeIndicatorScale = activeIndicatorScale;
        requestLayout();
    }

    public boolean isChangeIndicatorsColor() {
        return this.changeIndicatorsColor;
    }

    public void setChangeIndicatorsColor(boolean changeIndicatorsColor) {
        if (this.changeIndicatorsColor != changeIndicatorsColor) {
            this.changeIndicatorsColor = changeIndicatorsColor;
            requestLayout();
        }
    }
}
