package ru.driics.playm8.components.viewpager.indicator;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public final class CustomOnScrollListener extends RecyclerView.OnScrollListener {
    private final RecyclerView recyclerView;
    private final LinearLayoutManager layoutManager;
    private ScrollDispatcher dispatcher;
    private final ScrollTargetState scrollTargetState;
    private int mScrollState, mPreviousScrollState;
    private int mTargetPos, mPendingTargetPosition;
    private boolean mIsRecyclerViewIdle, mIsScrollTriggered, mIsFirstScroll;

    public CustomOnScrollListener(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        Intrinsics.checkNotNull(layoutManager);
        this.layoutManager = layoutManager;
        this.scrollTargetState = new ScrollTargetState();
        resetState();
    }

    public static abstract class ScrollDispatcher {
        public void dispatchStateChanged(int i) {

        }

        public void dispatchScroll(int i, float f, @Px int px) {

        }

        public void dispatchSelected(int i) {

        }
    }

    private void initState() {
        this.mScrollState = 1;
        int i = this.mPendingTargetPosition;
        if (i != -1) {
            this.mTargetPos = i;
            this.mPendingTargetPosition = -1;
        } else if (this.mTargetPos == -1) {
            this.mTargetPos = getPosition();
        }
        dispatchStateChanged(1);
    }

    private void dispatchScrolled(int scrollX, float scrollOffset, int scrollY) {
        ScrollDispatcher dispatcher = this.dispatcher;
        if (dispatcher != null) {
            dispatcher.dispatchScroll(scrollX, scrollOffset, scrollY);
        }
    }

    private void dispatchSelected(int position) {
        ScrollDispatcher dispatcher = this.dispatcher;
        if (dispatcher != null) {
            dispatcher.dispatchSelected(position);
        }
    }

    private void dispatchStateChanged(int newState) {
        int e = this.mPreviousScrollState;
        if (e != newState) {
            if (this.mScrollState == 3 && e == 0) {
                return;
            }
            this.mPreviousScrollState = newState;
            ScrollDispatcher dispatcher = this.dispatcher;
            if (dispatcher != null) {
                dispatcher.dispatchStateChanged(newState);
            }
        }
    }

    private int getPosition() {
        return this.layoutManager.findFirstVisibleItemPosition();
    }

    private boolean isInAnyDraggingState() {
        return this.mScrollState == 1;
    }

    private void resetState() {
        this.mScrollState = 0;
        this.mPreviousScrollState = 0;
        this.mTargetPos = -1;
        this.mPendingTargetPosition = -1;
        this.mIsRecyclerViewIdle = false;
        this.mIsScrollTriggered = false;
        this.mIsFirstScroll = false;
    }

    public void setDispatcher(ScrollDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    private void updateScrollEventValues() {
        int top;
        ScrollTargetState f = this.scrollTargetState;
        f.setItemPosition(this.layoutManager.findFirstVisibleItemPosition());
        if (f.getItemPosition() == -1) {
            f.reset();
            return;
        }
        View view = this.layoutManager.findViewByPosition(f.getItemPosition());
        if (view == null) {
            f.reset();
            return;
        }

        int leftDecorationWidth = this.layoutManager.getLeftDecorationWidth(view);
        int rightDecorationWidth = this.layoutManager.getRightDecorationWidth(view);
        int topDecorationHeight = this.layoutManager.getTopDecorationHeight(view);
        int bottomDecorationHeight = this.layoutManager.getBottomDecorationHeight(view);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;

            leftDecorationWidth += marginLayoutParams.leftMargin;
            rightDecorationWidth += marginLayoutParams.rightMargin;
            topDecorationHeight += marginLayoutParams.topMargin;
            bottomDecorationHeight += marginLayoutParams.bottomMargin;
        }

        int height = view.getHeight() + topDecorationHeight + bottomDecorationHeight;
        int width = view.getWidth() + leftDecorationWidth + rightDecorationWidth;

        if (this.layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
            top = (view.getLeft() - leftDecorationWidth) - this.recyclerView.getPaddingStart();
            height = width;
        } else {
            top = (view.getTop() - topDecorationHeight) - this.recyclerView.getPaddingTop();
        }
        f.setScrollState(Math.max(0, -top));
        f.setScrollOffset(height == 0 ? 0f : (float) (f.getScrollState() / height));
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        boolean isStateChanged = true;
        if ((this.mScrollState != 1 || this.mPreviousScrollState != 1) && newState == 1) {
            initState();
        } else if (isInAnyDraggingState() && newState == 2) {
            if (this.mIsScrollTriggered) {
                dispatchStateChanged(2);
                this.mIsRecyclerViewIdle = true;
            }
        } else {
            if (isInAnyDraggingState() && newState == 0) {
                updateScrollEventValues();
                if (!this.mIsScrollTriggered) {
                    if (this.scrollTargetState.getItemPosition() != -1) {
                        dispatchScrolled(this.scrollTargetState.getItemPosition(), 0f, 0);
                    }
                } else if (this.scrollTargetState.getScrollState() != 0) {
                    isStateChanged = false;
                } else if (this.mTargetPos != this.scrollTargetState.getItemPosition()) {
                    dispatchSelected(this.scrollTargetState.getItemPosition());
                }

                if (isStateChanged) {
                    dispatchStateChanged(0);
                    resetState();
                }
            }

            if (this.mScrollState == 2 && newState == 0 && this.mIsFirstScroll) {
                updateScrollEventValues();
                if (this.scrollTargetState.getScrollState() == 0) {
                    if (this.mPendingTargetPosition != this.scrollTargetState.getItemPosition()) {
                        dispatchSelected(this.scrollTargetState.getItemPosition() == -1 ? 0 : this.scrollTargetState.getItemPosition());
                    }
                    dispatchStateChanged(0);
                    resetState();
                }
            }
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        this.mIsScrollTriggered = true;
        updateScrollEventValues();
        if (this.mIsRecyclerViewIdle) {
            this.mIsRecyclerViewIdle = false;
            int i1 = (!(dy > 0) || this.scrollTargetState.getScrollState() == 0) ? this.scrollTargetState.getItemPosition() : this.scrollTargetState.getItemPosition() + 1;
            this.mPendingTargetPosition = i1;
            if (this.mTargetPos != i1) {
                dispatchSelected(i1);
            }
        } else if (this.mScrollState == 0) {
            int itemPosition = this.scrollTargetState.getItemPosition();
            if (itemPosition == -1) {
                itemPosition = 0;
            }
            dispatchSelected(itemPosition);
        }
        dispatchScrolled(this.scrollTargetState.getItemPosition() == -1 ? 0 : this.scrollTargetState.getItemPosition(), this.scrollTargetState.getScrollOffset(), this.scrollTargetState.getScrollState());
        int itemPosition = this.scrollTargetState.getItemPosition();
        int h = this.mPendingTargetPosition;
        if ((itemPosition == h || h == -1) && this.scrollTargetState.getScrollState() == 0 && this.mPreviousScrollState != 1) {
            dispatchStateChanged(0);
            resetState();
        }
    }

    public static final class ScrollTargetState {
        private int itemPosition;
        private float scrollOffset;
        private int scrollState;

        public ScrollTargetState() {
            this(0, 0f, 0, 7, null);
        }

        public ScrollTargetState(int pos, float offset, int state) {
            this.itemPosition = pos;
            this.scrollOffset = offset;
            this.scrollState = state;
        }

        public void reset() {
            this.itemPosition = -1;
            this.scrollOffset = 0f;
            this.scrollState = 0;
        }

        public int getItemPosition() {
            return itemPosition;
        }

        public void setItemPosition(int itemPosition) {
            this.itemPosition = itemPosition;
        }

        public float getScrollOffset() {
            return scrollOffset;
        }

        public void setScrollOffset(float scrollOffset) {
            this.scrollOffset = scrollOffset;
        }

        public int getScrollState() {
            return scrollState;
        }

        public void setScrollState(int scrollState) {
            this.scrollState = scrollState;
        }

        public ScrollTargetState(int i, float f, int i2, int i3, DefaultConstructorMarker defaultConstructorMarker) {
            this((i3 & 1) != 0 ? 0 : i, (i3 & 2) != 0 ? 0 : f, (i3 & 4) != 0 ? 0 : i2);
        }
    }
}
