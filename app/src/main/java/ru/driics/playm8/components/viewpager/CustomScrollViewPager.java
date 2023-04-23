package ru.driics.playm8.components.viewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import ru.driics.playm8.R;

public class CustomScrollViewPager extends ViewPager {
    private CustomScroller scroller;
    private boolean scrollEventEnabled;
    private float scrollDurationFactor;

    public CustomScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.scrollEventEnabled = true;
        init(attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.scrollEventEnabled && super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.scrollEventEnabled && super.onTouchEvent(motionEvent);
    }

    public void setScrollEventEnabled(boolean scrollEventEnabled) {
        this.scrollEventEnabled = scrollEventEnabled;
    }

    public void setScrollDurationFactor(float scrollDurationFactor) {
        this.scrollDurationFactor = scrollDurationFactor;
        this.scroller.setRatio(scrollDurationFactor);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs == null) return;

        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, new int[]{R.attr.ds_animationDurationFactor, R.attr.ds_scrollable}, 0, 0);
        try {
            this.scrollEventEnabled = styledAttributes.getBoolean(R.attr.ds_scrollable, true);
            this.scrollDurationFactor = styledAttributes.getFloat(R.attr.ds_animationDurationFactor, 1f);
            styledAttributes.recycle();
            if (Float.compare(this.scrollDurationFactor, 1f) != 0) {
                setScrollDurationFactor(this.scrollDurationFactor);
            }
        } catch (Throwable throwable) {
            styledAttributes.recycle();
            throw throwable;
        }
    }
}
