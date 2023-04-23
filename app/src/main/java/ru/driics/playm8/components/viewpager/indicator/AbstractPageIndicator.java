package ru.driics.playm8.components.viewpager.indicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import ru.driics.playm8.R;

public abstract class AbstractPageIndicator<I extends View> extends ViewGroup {
    @ColorInt
    protected int indicatorColor;

    @ColorInt
    protected int activeIndicatorColor;

    @Dimension
    protected int indicatorHeight;

    @Dimension
    protected int indicatorWidth;

    @Dimension
    protected int indicatorSpacing;

    protected int gravity;

    private ViewPagerAdapter adapter;

    private final List<I> views;
    private final Rect rect;


    public AbstractPageIndicator(Context context) {
        this(context, null);
    }

    public AbstractPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.gravity = Gravity.CENTER;
        this.adapter = new DefaultAdapterRealisation();
        this.views = new ArrayList<>();

        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.BasePageIndicator);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.styleable.BasePageIndicator_ds_iconSecondary, typedValue, true);
        int iconSecondaryColor = typedValue.data;

        this.indicatorColor = styledAttributes.getColor(R.styleable.BasePageIndicator_ds_indicatorColor, iconSecondaryColor);
        this.activeIndicatorColor = styledAttributes.getColor(R.styleable.BasePageIndicator_ds_activeIndicatorColor, iconSecondaryColor);
        this.indicatorHeight = styledAttributes.getLayoutDimension(R.styleable.BasePageIndicator_ds_indicatorSizeHeight, -1);
        this.indicatorWidth = styledAttributes.getLayoutDimension(R.styleable.BasePageIndicator_ds_indicatorSizeWidth, -1);
        this.indicatorSpacing = styledAttributes.getLayoutDimension(R.styleable.BasePageIndicator_ds_indicatorSpacing, -1);

        int gravity = styledAttributes.getInt(R.styleable.BasePageIndicator_android_layout_gravity, -1);
        if (gravity >= 0) {
            this.gravity = gravity;
        }

        int itemCount = styledAttributes.getInt(R.styleable.BasePageIndicator_ds_itemCount, isInEditMode() ? 2 : -1);

        if (itemCount >= 0) {
            ViewPagerAdapter adapter = this.adapter;
            DefaultAdapterRealisation adapterRealisation = adapter instanceof DefaultAdapterRealisation ? (DefaultAdapterRealisation) adapter : null;

            if (adapterRealisation != null) {
                adapterRealisation.setCurrentItem(0);
                adapterRealisation.setItemCount(itemCount);
            }
        }

        if (this.indicatorHeight == -1 && this.indicatorWidth == -1)
            throw new IllegalArgumentException("Args indicatorHeight and indicatorWidth has SAME_SIZE value");

        styledAttributes.recycle();

        this.rect = new Rect();
    }

    private final class Impl implements ViewPagerAdapter {
        private final RecyclerView recyclerView;
        private final CustomOnScrollListener listener;

        private final CustomScrollDispatcher CustomScrollDispatcher;
        private final DataObserver dataObserver;

        private final class DataObserver extends RecyclerView.AdapterDataObserver {
            public DataObserver() {

            }

            @Override
            public void onChanged() {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }
        }

        @Override
        public int getCurrentItem() {
            LinearLayoutManager layoutManager = (LinearLayoutManager) this.recyclerView.getLayoutManager();
            if (layoutManager != null) {
                return layoutManager.findFirstCompletelyVisibleItemPosition();
            }
            return 0;
        }

        @Override
        public int getItemsCount() {
            RecyclerView.Adapter adapter = this.recyclerView.getAdapter();
            if (adapter != null) {
                return adapter.getItemCount();
            }
            return 0;
        }

        @Override
        public void destroy() {
            this.recyclerView.removeOnScrollListener(this.listener);
            RecyclerView.Adapter adapter = this.recyclerView.getAdapter();
            if (adapter != null) {
                adapter.unregisterAdapterDataObserver(this.dataObserver);
            }
        }

        private final class Adapter extends RecyclerView.AdapterDataObserver {
            public Adapter() {

            }

            @Override
            public void onChanged() {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                AbstractPageIndicator.this.invalidate();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                AbstractPageIndicator.this.invalidate();
            }
        }

        private final class CustomScrollDispatcher extends CustomOnScrollListener.ScrollDispatcher {
            public CustomScrollDispatcher() {
            }

            @Override
            public void dispatchScroll(int i, float f, int px) {
                AbstractPageIndicator.this.onPageScrolled(i, f);
            }
        }

        public Impl(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            this.listener = new CustomOnScrollListener(recyclerView);
            this.CustomScrollDispatcher = new CustomScrollDispatcher();
            this.dataObserver = new DataObserver();

            recyclerView.addOnScrollListener(listener);
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null) {
                adapter.registerAdapterDataObserver(dataObserver);
            }

            listener.setDispatcher(CustomScrollDispatcher);
        }
    }

    public interface ViewPagerAdapter {
        int getCurrentItem();

        int getItemsCount();

        void destroy();
    }

    private final class Observer extends DataSetObserver implements ViewPagerAdapter, ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {
        private final ViewPager viewPager;

        public Observer(ViewPager viewPager) {
            this.viewPager = viewPager;
            viewPager.addOnPageChangeListener(this);
            viewPager.addOnAdapterChangeListener(this);
            PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                adapter.registerDataSetObserver(this);
            }
        }

        @Override
        public int getCurrentItem() {
            return this.viewPager.getCurrentItem();
        }

        @Override
        public int getItemsCount() {
            PagerAdapter adapter = this.viewPager.getAdapter();
            if (adapter != null) {
                return adapter.getCount();
            }
            return 0;
        }

        @Override
        public void destroy() {
            this.viewPager.removeOnPageChangeListener(this);
            this.viewPager.removeOnAdapterChangeListener(this);

            PagerAdapter adapter = this.viewPager.getAdapter();
            if (adapter != null) {
                adapter.unregisterDataSetObserver(this);
            }
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (oldAdapter != null) {
                oldAdapter.unregisterDataSetObserver(this);
            }

            if (newAdapter != null) {
                newAdapter.registerDataSetObserver(this);
            }

            AbstractPageIndicator.this.invalidate();
        }

        @Override
        public void onChanged() {
            AbstractPageIndicator.this.invalidate();
        }

        @Override
        public void onInvalidated() {
            AbstractPageIndicator.this.invalidate();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            AbstractPageIndicator.this.onPageScrolled(position, positionOffset);
        }

        @Override
        public void onPageSelected(int position) {

        }
    }

    protected abstract void onPageScrolled(int position, float positionOffset);

    protected abstract void setIndicatorBackground(int position, I view);

    protected abstract I createIndicatorView(int position);

    public final void invalidate() {
        int itemsCount = this.adapter.getItemsCount();
        int viewsCount = this.views.size();

        int diff = itemsCount - viewsCount;
        int count = 0;

        if (diff > 0) {
            while (count < diff) {
                I el = createIndicatorView(viewsCount + count);
                this.views.add(el);
                addView(el);
                count++;
            }
        } else if (diff < 0) {
            int abs = Math.abs(diff);
            while (count < abs) {
                List<I> views = this.views;
                removeView(views.remove(views.size() - 1));
                count++;
            }
        }

        if (diff != 0) {
            requestLayout();
        }
    }

    @ColorInt
    public int getIndicatorColor() {
        return this.indicatorColor;
    }

    @ColorInt
    public int getActiveIndicatorColor() {
        return this.activeIndicatorColor;
    }

    @Dimension
    public int getIndicatorHeight() {
        return this.indicatorHeight;
    }

    @Dimension
    public final int getIndicatorHeightValue() {
        return getIndicatorHeight() == -1 ? getIndicatorWidth() : getIndicatorHeight();
    }

    @Dimension
    public int getIndicatorWidth() {
        return this.indicatorWidth;
    }

    @Dimension
    public int getIndicatorSpacing() {
        return this.indicatorSpacing;
    }

    protected final int getIndicatorWidthValue() {
        return getIndicatorWidth() == -1 ? getIndicatorHeight() : getIndicatorWidth();
    }

    public int getGravity() {
        return this.gravity;
    }

    public final ViewPagerAdapter getAdapter() {
        return this.adapter;
    }

    public float getItemCount() {
        return this.adapter.getItemsCount();
    }

    protected float getSpacesCountForMeasure() {
        return this.adapter.getItemsCount() - 1;
    }

    public int getCurrentItem() {
        return this.adapter.getCurrentItem();
    }

    public boolean isItElement(int id) {
        return this.adapter.getCurrentItem() == id;
    }

    public void invalidateViews() {
        for (int i = 0; i < this.views.size(); i++) {
            setIndicatorBackground(i, this.views.get(i));
        }
    }

    public static abstract class AbstractViewPagerAdapter implements ViewPagerAdapter {
        private AbstractPageIndicator<?> abstractPageIndicator;

        public final void reset() {
            AbstractPageIndicator<?> indicator = this.abstractPageIndicator;
            if (indicator != null) {
                indicator.invalidate();
            }
        }

        public final void notifyPageScrolled(int position, float positionOffset) {
            AbstractPageIndicator<?> pageIndicator = this.abstractPageIndicator;
            if (pageIndicator != null) {
                pageIndicator.onPageScrolled(position, positionOffset);
            }
        }

        @Override
        public void destroy() {

        }

        public final void setBasePageIndicator(AbstractPageIndicator<?> abstractPageIndicator) {
            this.abstractPageIndicator = abstractPageIndicator;
        }
    }

    public void setActiveIndicatorColor(@ColorInt int activeIndicatorColor) {
        if (this.activeIndicatorColor != activeIndicatorColor) {
            this.activeIndicatorColor = activeIndicatorColor;
            invalidateViews();
        }
    }

    public void setIndicatorColor(@ColorInt int indicatorColor) {
        if (this.indicatorColor != indicatorColor) {
            this.indicatorColor = indicatorColor;
            invalidateViews();
        }
    }

    public void setIndicatorHeight(@Dimension int indicatorHeight) {
        if (this.indicatorHeight != indicatorHeight) {
            this.indicatorHeight = indicatorHeight;
            requestLayout();
        }
    }

    public void setIndicatorWidth(@Dimension int indicatorWidth) {
        if (this.indicatorWidth != indicatorWidth) {
            this.indicatorWidth = indicatorWidth;
            requestLayout();
        }
    }

    public void setIndicatorSpacing(@Dimension int indicatorSpacing) {
        if (this.indicatorSpacing != indicatorSpacing) {
            this.indicatorSpacing = indicatorSpacing;
            requestLayout();
        }
    }

    public void setGravity(int gravity) {
        if (this.gravity != gravity) {
            if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.START;
            }
            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }
            this.gravity = gravity;
            requestLayout();
        }
    }

    public final void setCustomViewPagerAdapter(AbstractViewPagerAdapter adapter) {
        if (adapter == null) {
            setViewPagerAdapter(new DefaultAdapterRealisation());
            return;
        }
        adapter.setBasePageIndicator(this);
        setViewPagerAdapter(adapter);
    }

    public final void setRecyclerView(RecyclerView recyclerView) {
        if (recyclerView == null) {
            setViewPagerAdapter(new DefaultAdapterRealisation());
        } else {
            setViewPagerAdapter(new Impl(recyclerView));
        }
    }

    public final void setViewPager(ViewPager viewPager) {
        if (viewPager == null) {
            setViewPagerAdapter(new DefaultAdapterRealisation());
        } else {
            setViewPagerAdapter(new Observer(viewPager));
        }
    }

    public final I getByIndex(int i) {
        if (i < 0 || i >= this.views.size()) {
            return null;
        }
        return this.views.get(i);
    }

    protected final void setViewPagerAdapter(ViewPagerAdapter adapter) {
        this.adapter.destroy();
        this.adapter = adapter;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.views.size() <= 1)
            return;

        int paddingTop = getPaddingTop();
        int height = getHeight() - getPaddingBottom();
        int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;

        if (gravity == Gravity.CENTER_VERTICAL) {
            int padding = getPaddingTop() + (((getHeight() - getPaddingTop()) - getPaddingBottom()) / 2);
            int indicatorHeightValue = getIndicatorHeightValue() / 2;
            int newPadding = padding - indicatorHeightValue;

            height = indicatorHeightValue + padding;
            paddingTop = newPadding;
        } else if (gravity == Gravity.TOP) {
            height = Math.min(getIndicatorHeightValue() + paddingTop, getHeight() - getPaddingBottom());
        } else if (gravity == Gravity.BOTTOM) {
            paddingTop = Math.max(height - getIndicatorHeightValue(), getPaddingTop());
        }

        float itemCount = getItemCount();
        int indicatorWidthValue = getIndicatorWidthValue();
        int indicatorSpacing = getIndicatorSpacing();

        boolean isIndicatorWidthWrapContent = indicatorWidthValue == LayoutParams.WRAP_CONTENT;
        boolean isIndicatorSpacingMatchParent = indicatorSpacing == LayoutParams.MATCH_PARENT;

        int width = (getWidth() - getPaddingStart()) - getPaddingEnd();

        if (isIndicatorWidthWrapContent && isIndicatorSpacingMatchParent) {
            indicatorWidthValue = (int) (width / (getSpacesCountForMeasure() + itemCount));
            indicatorSpacing = indicatorWidthValue;
        } else if (isIndicatorSpacingMatchParent) {
            indicatorSpacing = (int) ((width - (indicatorWidthValue * itemCount)) / getSpacesCountForMeasure());
        } else if (isIndicatorWidthWrapContent) {
            indicatorWidthValue = (int) ((width - (indicatorSpacing * getSpacesCountForMeasure())) / itemCount);
        }

        int paddingStart = getPaddingStart();
        int mGravity = getGravity() & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;

        if (mGravity == Gravity.CENTER_HORIZONTAL) {
            paddingStart = (int) ((getPaddingStart() + (width / 2)) - (((indicatorWidthValue * itemCount) + (indicatorSpacing * getSpacesCountForMeasure())) / 2));
        } else if (mGravity == Gravity.END) {
            paddingStart = (int) (getWidth() - ((indicatorWidthValue * itemCount) + (indicatorSpacing * getSpacesCountForMeasure())));
        }

        int totalIndicatorWidth = indicatorWidthValue + indicatorSpacing;
        if ((this.views.size() - 1) * totalIndicatorWidth > (getWidth() - getPaddingStart()) - getPaddingEnd()) {
            int padding = getPaddingStart();
            int total = 0;
            int id = 0;
            for (I view : this.views) {
                total += totalIndicatorWidth;
                if (total > getWidth()) {
                    id = this.views.indexOf(view) - 1;
                    total = 0;
                }
                if (this.views.indexOf(view) == getCurrentItem()) {
                    break;
                }
            }

            int indicatorIndex = 0;
            for (I view : this.views) {
                if (indicatorIndex < 0) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                if (indicatorIndex > id) {
                    view.setVisibility(VISIBLE);
                    this.rect.set(
                            padding, paddingTop,
                            padding + indicatorWidthValue, height
                    );
                    padding += calculateIndicatorPadding(view, indicatorIndex, this.rect, indicatorSpacing);
                } else {
                    view.setVisibility(GONE);
                }

                indicatorIndex += 1;
            }
            return;
        }
        int counter = 0;
        for (I view : this.views) {
            if (counter < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            this.rect.set(paddingStart, paddingTop, paddingStart + indicatorWidthValue, height);
            paddingStart += calculateIndicatorPadding(view, counter, this.rect, indicatorSpacing);
            counter += 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int indicatorHeightValue = getIndicatorHeightValue();

        if (heightMode != MeasureSpec.EXACTLY && (indicatorHeightValue != LayoutParams.WRAP_CONTENT || heightSize == 0)) {
            int max = Math.max(getIndicatorHeightValue() + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
            heightSize = heightMode == MeasureSpec.AT_MOST ? Math.min(max, heightSize) : max;
        }

        if (indicatorHeightValue != LayoutParams.WRAP_CONTENT) {
            indicatorHeightValue = heightSize;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int indicatorWidthValue = getIndicatorWidthValue();
        float itemCount = getItemCount();
        float indicatorSpacing = getIndicatorSpacing() > 0 ? getIndicatorSpacing() * (itemCount - 1) : 0f;

        if (widthMode != MeasureSpec.EXACTLY && ((indicatorWidthValue != LayoutParams.WRAP_CONTENT || widthSize == 0) && (getIndicatorSpacing() != LayoutParams.MATCH_PARENT || widthSize == 0))) {
            int widthMax = Math.max((int) ((getIndicatorWidthValue() * itemCount) + indicatorSpacing + getPaddingStart() + getPaddingEnd()), getSuggestedMinimumWidth());
            widthSize = widthMode == MeasureSpec.AT_MOST ? Math.min(widthMax, widthSize) : widthMax;
        }

        if (indicatorWidthValue == LayoutParams.WRAP_CONTENT) {
            indicatorWidthValue = (int) ((((widthSize - getPaddingStart()) - getPaddingEnd()) - indicatorSpacing) / itemCount);
        }

        for (I view : this.views) {
            view.measure(
                    MeasureSpec.makeMeasureSpec(indicatorWidthValue, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(indicatorHeightValue, MeasureSpec.AT_MOST)
            );
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    public int calculateIndicatorPadding(I indicatorView, int position, Rect indicatorRect, int indicatorSpacing) {
        indicatorView.layout(indicatorRect.left, indicatorRect.top, indicatorRect.right, indicatorRect.bottom);
        return indicatorRect.width() + indicatorSpacing;
    }

    private static final class DefaultAdapterRealisation implements ViewPagerAdapter {
        private int itemCount, currentItem;

        public DefaultAdapterRealisation() {
            /*this(0, 0, 3, null);*/
            this(0, 0);
        }

        public DefaultAdapterRealisation(int itemCount, int currentItem) {
            this.itemCount = itemCount;
            this.currentItem = currentItem;
        }
/*
        public DefaultAdapterRealisation(int i, int i2, int i3, DefaultConstructorMarker defaultConstructorMarker) {
            this((i3 & 1) != 0 ? 0 : i, (i3 & 2) != 0 ? 0 : i2);
        }*/

        @Override
        public int getCurrentItem() {
            return this.currentItem;
        }

        @Override
        public int getItemsCount() {
            return this.itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        public void setCurrentItem(int currentItem) {
            this.currentItem = currentItem;
        }

        @Override
        public void destroy() {

        }
    }
}
