package ru.driics.playm8.components.viewpager.indicator;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public final class ViewPageAdapter extends AbstractPageIndicator.AbstractViewPagerAdapter {
    private final ViewPager2 pager;
    private final PageChangeCallback callback;
    private final AdapterDataObserver observer;


    public final class AdapterDataObserver extends RecyclerView.AdapterDataObserver {
        AdapterDataObserver() {
        }

        @Override
        public void onChanged() {
            ViewPageAdapter.this.reset();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            ViewPageAdapter.this.reset();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            ViewPageAdapter.this.reset();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            ViewPageAdapter.this.reset();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            ViewPageAdapter.this.reset();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            ViewPageAdapter.this.reset();
        }
    }

    public final class PageChangeCallback extends ViewPager2.OnPageChangeCallback {
        PageChangeCallback() {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            ViewPageAdapter.this.notifyPageScrolled(position, positionOffset);
        }
    }

    public ViewPageAdapter(ViewPager2 viewPager) {
        this.pager = viewPager;
        this.observer = new AdapterDataObserver();
        this.callback = new PageChangeCallback();

        viewPager.registerOnPageChangeCallback(this.callback);
        RecyclerView.Adapter adapter = viewPager.getAdapter();
        if (adapter != null) {
            adapter.registerAdapterDataObserver(this.observer);
        }
    }

    @Override
    public int getCurrentItem() {
        return this.pager.getCurrentItem();
    }

    @Override
    public int getItemsCount() {
        RecyclerView.Adapter adapter = this.pager.getAdapter();
        if (adapter != null) {
            return adapter.getItemCount();
        }
        return 0;
    }

    @Override
    public void destroy() {
        this.pager.unregisterOnPageChangeCallback(this.callback);
        RecyclerView.Adapter adapter = this.pager.getAdapter();
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(this.observer);
        }
    }
}
