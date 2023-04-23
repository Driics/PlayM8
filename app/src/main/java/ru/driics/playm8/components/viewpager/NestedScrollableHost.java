package ru.driics.playm8.components.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

public class NestedScrollableHost extends FrameLayout {
    private int scaledTouchSlop;
    private float motionEventX, motionEventY;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        this.scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private final void handleMotionEvent(MotionEvent motionEvent) {

    }

    private final View getChild() {
        if (getChildCount() > 0) {
            return getChildAt(0);
        }
        return null;
    }

    private final boolean canScroll(int orientation, float x) {
        int direction = -((int) Math.signum(x));
        if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            View child = getChild();
            if (child != null) {
                return child.canScrollHorizontally(direction);
            }
            return false;
        } else if (orientation == ViewPager2.ORIENTATION_VERTICAL) {
            View child = getChild();
            if (child != null) {
                return child.canScrollVertically(direction);
            }
            return false;
        } else throw new IllegalArgumentException();
    }
}
