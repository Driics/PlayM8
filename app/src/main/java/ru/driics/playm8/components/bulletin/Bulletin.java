package ru.driics.playm8.components.bulletin;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Property;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.driics.playm8.R;
import ru.driics.playm8.utils.AndroidUtils;
import ru.driics.playm8.utils.AnimationProperties;
import ru.driics.playm8.utils.CubicBezierInterpolator;
import ru.driics.playm8.utils.LayoutHelper;
import ru.driics.playm8.utils.ViewHelper;
import ru.driics.playm8.utils.ViewUtils;

public class Bulletin {

    public static final int DURATION_SHORT = 1500;
    public static final int DURATION_LONG = 2750;
    public static final int DURATION_PROLONG = 5000;

    public static final int TYPE_STICKER = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_BIO_CHANGED = 2;
    public static final int TYPE_NAME_CHANGED = 3;
    public static final int TYPE_ERROR_SUBTITLE = 4;
    public static final int TYPE_APP_ICON = 5;
    public static final int TYPE_SUCCESS = 6;

    public int tag;
    public int hash;
    private View.OnLayoutChangeListener containerLayoutListener;
    private SpringAnimation bottomOffsetSpring;

    public static Bulletin make(@NonNull FrameLayout containerLayout, @NonNull Layout contentLayout, int duration) {
        return new Bulletin(null, containerLayout, contentLayout, duration);
    }

    @SuppressLint("RtlHardcoded")
    public static Bulletin make(@NonNull Fragment fragment, @NonNull Layout contentLayout, int duration) {
        return new Bulletin(fragment, (FrameLayout) fragment.requireView(), contentLayout, duration);
    }

    public static Bulletin find(@NonNull FrameLayout containerLayout) {
        for (int i = 0, size = containerLayout.getChildCount(); i < size; i++) {
            final View view = containerLayout.getChildAt(i);
            if (view instanceof Layout) {
                return ((Layout) view).bulletin;
            }
        }
        return null;
    }

    public static void hide(@NonNull FrameLayout containerLayout) {
        hide(containerLayout, true);
    }

    public static void hide(@NonNull FrameLayout containerLayout, boolean animated) {
        final Bulletin bulletin = find(containerLayout);
        if (bulletin != null) {
            bulletin.hide(animated && isTransitionsEnabled(), 0);
        }
    }

    private static final HashMap<FrameLayout, Delegate> delegates = new HashMap<>();
    private static final HashMap<Fragment, Delegate> fragmentDelegates = new HashMap<>();

    @SuppressLint("StaticFieldLeak")
    private static Bulletin visibleBulletin;

    private final Layout layout;
    private final ParentLayout parentLayout;
    private final Fragment containerFragment;
    private final FrameLayout containerLayout;
    private final Runnable hideRunnable = this::hide;
    private int duration;

    private boolean showing;
    private boolean canHide;
    private boolean loaded = true;
    public int currentBottomOffset;
    public int lastBottomOffset;
    private Delegate currentDelegate;
    private Layout.Transition layoutTransition;

    private Bulletin() {
        layout = null;
        parentLayout = null;
        containerFragment = null;
        containerLayout = null;
    }

    private Bulletin(Fragment fragment, @NonNull FrameLayout containerLayout, @NonNull Layout layout, int duration) {
        this.layout = layout;
        this.loaded = !(this.layout instanceof LoadingLayout);
        this.parentLayout = new ParentLayout(layout) {
            @Override
            protected void onPressedStateChanged(boolean pressed) {
                setCanHide(!pressed);
                if (containerLayout.getParent() != null) {
                    containerLayout.getParent().requestDisallowInterceptTouchEvent(pressed);
                }
            }

            @Override
            protected void onHide() {
                hide();
            }
        };
        this.containerFragment = fragment;
        this.containerLayout = containerLayout;
        this.duration = duration;
    }

    public static Bulletin getVisibleBulletin() {
        return visibleBulletin;
    }

    public static void hideVisible() {
        if (visibleBulletin != null) {
            visibleBulletin.hide();
        }
    }

    public Bulletin setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public Bulletin show() {
        return show(false);
    }

    public Bulletin show(boolean top) {
        if (!showing && containerLayout != null) {
            showing = true;
            layout.setTop(top);

            CharSequence text = layout.getAccessibilityText();
            if (text != null) {
                AndroidUtils.INSTANCE.makeAccessibilityAnnouncement(parentLayout.getContext(), text);
            }

            if (layout.getParent() != parentLayout) {
                throw new IllegalStateException("Layout has incorrect parent");
            }

            if (visibleBulletin != null) {
                visibleBulletin.hide();
            }
            visibleBulletin = this;
            layout.onAttach(this);

            containerLayout.addOnLayoutChangeListener(containerLayoutListener = (v, left, top1, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (currentDelegate != null && !currentDelegate.allowLayoutChanges()) {
                    return;
                }
                if (!top) {
                    int newOffset = currentDelegate != null ? currentDelegate.getBottomOffset(tag) : 0;
                    if (lastBottomOffset != newOffset) {
                        if (bottomOffsetSpring == null || !bottomOffsetSpring.isRunning()) {
                            bottomOffsetSpring = new SpringAnimation(new FloatValueHolder(lastBottomOffset))
                                    .setSpring(new SpringForce()
                                            .setFinalPosition(newOffset)
                                            .setStiffness(900f)
                                            .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY));
                            bottomOffsetSpring.addUpdateListener((animation, value, velocity) -> {
                                lastBottomOffset = (int) value;
                                updatePosition();
                            });
                            bottomOffsetSpring.addEndListener((animation, canceled, value, velocity) -> {
                                if (bottomOffsetSpring == animation) {
                                    bottomOffsetSpring = null;
                                }
                            });
                        } else {
                            bottomOffsetSpring.getSpring().setFinalPosition(newOffset);
                        }
                        bottomOffsetSpring.start();
                    }
                }
            });

            layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int t, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    layout.removeOnLayoutChangeListener(this);
                    if (showing) {
                        layout.onShow();
                        currentDelegate = findDelegate(containerFragment, containerLayout);
                        if (bottomOffsetSpring == null || !bottomOffsetSpring.isRunning()) {
                            lastBottomOffset = currentDelegate != null ? currentDelegate.getBottomOffset(tag) : 0;
                        }
                        if (currentDelegate != null) {
                            currentDelegate.onShow(Bulletin.this);
                        }
                        if (isTransitionsEnabled()) {
                            ensureLayoutTransitionCreated();
                            layout.transitionRunningEnter = true;
                            layout.delegate = currentDelegate;
                            layout.invalidate();
                            layoutTransition.animateEnter(layout, layout::onEnterTransitionStart, () -> {
                                layout.transitionRunningEnter = false;
                                layout.onEnterTransitionEnd();
                                setCanHide(true);
                            }, offset -> {
                                if (currentDelegate != null && !top) {
                                    currentDelegate.onBottomOffsetChange(layout.getHeight() - offset);
                                }
                            }, currentBottomOffset);
                        } else {
                            if (currentDelegate != null && !top) {
                                currentDelegate.onBottomOffsetChange(layout.getHeight() - currentBottomOffset);
                            }
                            updatePosition();
                            layout.onEnterTransitionStart();
                            layout.onEnterTransitionEnd();
                            setCanHide(true);
                        }
                    }
                }
            });

            layout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    layout.removeOnAttachStateChangeListener(this);
                    hide(false, 0);
                }
            });

            containerLayout.addView(parentLayout);
        }
        return this;
    }

    public void setCanHide(boolean canHide) {
        canHide = canHide && loaded;
        if (this.canHide != canHide && layout != null) {
            this.canHide = canHide;
            if (canHide) {
                layout.postDelayed(hideRunnable, duration);
            } else {
                layout.removeCallbacks(hideRunnable);
            }
        }
    }

    private Runnable onHideListener;

    public Bulletin setOnHideListener(Runnable listener) {
        this.onHideListener = listener;
        return this;
    }

    private void ensureLayoutTransitionCreated() {
        if (layout != null && layoutTransition == null) {
            layoutTransition = layout.createTransition();
        }
    }

    public void hide() {
        hide(isTransitionsEnabled(), 0);
    }

    public void hide(long duration) {
        hide(isTransitionsEnabled(), duration);
    }

    public void hide(boolean animated, long duration) {
        if (layout == null) {
            return;
        }
        if (showing) {
            showing = false;

            if (visibleBulletin == this) {
                visibleBulletin = null;
            }

            int bottomOffset = currentBottomOffset;
            currentBottomOffset = 0;

            if (ViewCompat.isLaidOut(layout)) {
                layout.removeCallbacks(hideRunnable);
                if (animated) {
                    layout.transitionRunningExit = true;
                    layout.delegate = currentDelegate;
                    layout.invalidate();
                    if (duration >= 0) {
                        Layout.DefaultTransition transition = new Layout.DefaultTransition();
                        transition.duration = duration;
                        layoutTransition = transition;
                    } else {
                        ensureLayoutTransitionCreated();
                    }
                    layoutTransition.animateExit(layout, layout::onExitTransitionStart, () -> {
                        if (currentDelegate != null && !layout.top) {
                            currentDelegate.onBottomOffsetChange(0);
                            currentDelegate.onHide(this);
                        }
                        layout.transitionRunningExit = false;
                        layout.onExitTransitionEnd();
                        layout.onHide();
                        containerLayout.removeView(parentLayout);
                        containerLayout.removeOnLayoutChangeListener(containerLayoutListener);
                        layout.onDetach();

                        if (onHideListener != null) {
                            onHideListener.run();
                        }
                    }, offset -> {
                        if (currentDelegate != null && !layout.top) {
                            currentDelegate.onBottomOffsetChange(layout.getHeight() - offset);
                        }
                    }, bottomOffset);
                    return;
                }
            }

            if (currentDelegate != null && !layout.top) {
                currentDelegate.onBottomOffsetChange(0);
                currentDelegate.onHide(this);
            }
            layout.onExitTransitionStart();
            layout.onExitTransitionEnd();
            layout.onHide();
            if (containerLayout != null) {
                AndroidUtils.INSTANCE.runOnUIThread(() -> {
                    containerLayout.removeView(parentLayout);
                    containerLayout.removeOnLayoutChangeListener(containerLayoutListener);
                });
            }
            layout.onDetach();

            if (onHideListener != null) {
                onHideListener.run();
            }
        }
    }

    public boolean isShowing() {
        return showing;
    }

    public Layout getLayout() {
        return layout;
    }

    private static boolean isTransitionsEnabled() {
        return true;
    }

    public void updatePosition() {
        if (layout != null) {
            layout.updatePosition();
        }
    }

    @Retention(SOURCE)
    @IntDef(value = {ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT})
    private @interface WidthDef {
    }

    @Retention(SOURCE)
    @SuppressLint("RtlHardcoded")
    @IntDef(value = {Gravity.LEFT, Gravity.RIGHT, Gravity.CENTER_HORIZONTAL, Gravity.NO_GRAVITY})
    private @interface GravityDef {
    }

    private static abstract class ParentLayout extends FrameLayout {

        private final Layout layout;
        private final Rect rect = new Rect();
        private final GestureDetector gestureDetector;

        private boolean pressed;
        private float translationX;
        private boolean hideAnimationRunning;
        private boolean needLeftAlphaAnimation;
        private boolean needRightAlphaAnimation;

        public ParentLayout(Layout layout) {
            super(layout.getContext());
            this.layout = layout;
            gestureDetector = new GestureDetector(layout.getContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onDown(MotionEvent e) {
                    if (!hideAnimationRunning) {
                        needLeftAlphaAnimation = layout.isNeedSwipeAlphaAnimation(true);
                        needRightAlphaAnimation = layout.isNeedSwipeAlphaAnimation(false);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    layout.setTranslationX(translationX -= distanceX);
                    if (translationX == 0 || (translationX < 0f && needLeftAlphaAnimation) || (translationX > 0f && needRightAlphaAnimation)) {
                        layout.setAlpha(1f - Math.abs(translationX) / layout.getWidth());
                    }
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(velocityX) > 2000f) {
                        final boolean needAlphaAnimation = (velocityX < 0f && needLeftAlphaAnimation) || (velocityX > 0f && needRightAlphaAnimation);

                        final SpringAnimation springAnimation = new SpringAnimation(layout, DynamicAnimation.TRANSLATION_X, Math.signum(velocityX) * layout.getWidth() * 2f);
                        if (!needAlphaAnimation) {
                            springAnimation.addEndListener((animation, canceled, value, velocity) -> onHide());
                            springAnimation.addUpdateListener(((animation, value, velocity) -> {
                                if (Math.abs(value) > layout.getWidth()) {
                                    animation.cancel();
                                }
                            }));
                        }
                        springAnimation.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
                        springAnimation.getSpring().setStiffness(100f);
                        springAnimation.setStartVelocity(velocityX);
                        springAnimation.start();

                        if (needAlphaAnimation) {
                            final SpringAnimation springAnimation2 = new SpringAnimation(layout, DynamicAnimation.ALPHA, 0f);
                            springAnimation2.addEndListener((animation, canceled, value, velocity) -> onHide());
                            springAnimation2.addUpdateListener(((animation, value, velocity) -> {
                                if (value <= 0f) {
                                    animation.cancel();
                                }
                            }));
                            springAnimation.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
                            springAnimation.getSpring().setStiffness(10f);
                            springAnimation.setStartVelocity(velocityX);
                            springAnimation2.start();
                        }

                        hideAnimationRunning = true;
                        return true;
                    }
                    return false;
                }
            });
            gestureDetector.setIsLongpressEnabled(false);
            addView(layout);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (pressed || inLayoutHitRect(event.getX(), event.getY())) {
                gestureDetector.onTouchEvent(event);
                final int actionMasked = event.getActionMasked();
                if (actionMasked == MotionEvent.ACTION_DOWN) {
                    if (!pressed && !hideAnimationRunning) {
                        layout.animate().cancel();
                        translationX = layout.getTranslationX();
                        onPressedStateChanged(pressed = true);
                    }
                } else if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
                    if (pressed) {
                        if (!hideAnimationRunning) {
                            if (Math.abs(translationX) > layout.getWidth() / 3f) {
                                final float tx = Math.signum(translationX) * layout.getWidth();
                                final boolean needAlphaAnimation = (translationX < 0f && needLeftAlphaAnimation) || (translationX > 0f && needRightAlphaAnimation);
                                layout.animate().translationX(tx).alpha(needAlphaAnimation ? 0f : 1f).setDuration(200).setInterpolator(new AccelerateInterpolator()).withEndAction(() -> {
                                    if (layout.getTranslationX() == tx) {
                                        onHide();
                                    }
                                }).start();
                            } else {
                                layout.animate().translationX(0).alpha(1f).setDuration(200).start();
                            }
                        }
                        onPressedStateChanged(pressed = false);
                    }
                }
                return true;
            }
            return false;
        }

        private boolean inLayoutHitRect(float x, float y) {
            layout.getHitRect(rect);
            return rect.contains((int) x, (int) y);
        }

        protected abstract void onPressedStateChanged(boolean pressed);

        protected abstract void onHide();
    }

    //region Offset Providers
    public static void addDelegate(@NonNull Fragment fragment, @NonNull Delegate delegate) {
        fragmentDelegates.put(fragment, delegate);
    }

    public static void addDelegate(@NonNull FrameLayout containerLayout, @NonNull Delegate delegate) {
        delegates.put(containerLayout, delegate);
    }

    private static Delegate findDelegate(Fragment probableFragment, FrameLayout probableContainer) {
        Delegate delegate;
        if ((delegate = fragmentDelegates.get(probableFragment)) != null) {
            return delegate;
        }
        if ((delegate = delegates.get(probableContainer)) != null) {
            return delegate;
        }
        return null;
    }

    public static void removeDelegate(@NonNull Fragment fragment) {
        fragmentDelegates.remove(fragment);
    }

    public static void removeDelegate(@NonNull FrameLayout containerLayout) {
        delegates.remove(containerLayout);
    }

    public interface Delegate {

        default int getBottomOffset(int tag) {
            return 0;
        }

        default int getTopOffset(int tag) {
            return 0;
        }

        default void onBottomOffsetChange(float offset) {
        }

        default void onShow(Bulletin bulletin) {
        }

        default void onHide(Bulletin bulletin) {
        }

        default boolean allowLayoutChanges() {
            return true;
        }
    }
    //endregion

    //region Layouts
    public abstract static class Layout extends FrameLayout {

        private final List<Callback> callbacks = new ArrayList<>();
        public boolean transitionRunningEnter;
        public boolean transitionRunningExit;
        Delegate delegate;
        public float inOutOffset;

        protected Bulletin bulletin;
        Drawable background;
        private boolean top;

        public boolean isTransitionRunning() {
            return transitionRunningEnter || transitionRunningExit;
        }

        @WidthDef
        private int wideScreenWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        @GravityDef
        private int wideScreenGravity = Gravity.CENTER_HORIZONTAL;

        public Layout(@NonNull Context context) {
            super(context);
            setMinimumHeight((int) ViewUtils.INSTANCE.getToPx(48));
            setBackground(Color.GRAY);
            updateSize();
            setPadding((int) ViewUtils.INSTANCE.getToPx(8), (int) ViewUtils.INSTANCE.getToPx(8), (int) ViewUtils.INSTANCE.getToPx(8), (int) ViewUtils.INSTANCE.getToPx(8));
            setWillNotDraw(false);
        }

        protected void setBackground(int color) {
            background = createRoundRectDrawable((int) ViewUtils.INSTANCE.getToPx(10), color);
        }

        private static Drawable createRoundRectDrawable(int rad, int defaultColor) {
            ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
            defaultDrawable.getPaint().setColor(defaultColor);
            return defaultDrawable;
        }

        public final static FloatPropertyCompat<Layout> IN_OUT_OFFSET_Y = new FloatPropertyCompat<Layout>("offsetY") {
            @Override
            public float getValue(Layout object) {
                return object.inOutOffset;
            }

            @Override
            public void setValue(Layout object, float value) {
                object.setInOutOffset(value);
            }
        };

        public final static Property<Layout, Float> IN_OUT_OFFSET_Y2 = new AnimationProperties.FloatProperty<Layout>("offsetY") {

            @Override
            public Float get(Layout layout) {
                return layout.inOutOffset;
            }

            @Override
            public void setValue(Layout object, float value) {
                object.setInOutOffset(value);
            }
        };

        @Override
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateSize();
        }

        private void setTop(boolean top) {
            this.top = top;
            updateSize();
        }

        private void updateSize() {
            setLayoutParams(LayoutHelper.INSTANCE.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (top ? Gravity.TOP : Gravity.BOTTOM)));
        }

        @SuppressLint("RtlHardcoded")
        private boolean isNeedSwipeAlphaAnimation(boolean swipeLeft) {
            if (wideScreenWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
                return false;
            }
            if (wideScreenGravity == Gravity.CENTER_HORIZONTAL) {
                return true;
            }
            if (swipeLeft) {
                return wideScreenGravity == Gravity.RIGHT;
            } else {
                return wideScreenGravity != Gravity.RIGHT;
            }
        }

        protected CharSequence getAccessibilityText() {
            return null;
        }

        public Bulletin getBulletin() {
            return bulletin;
        }

        public boolean isAttachedToBulletin() {
            return bulletin != null;
        }

        @CallSuper
        protected void onAttach(@NonNull Bulletin bulletin) {
            this.bulletin = bulletin;
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onAttach(this, bulletin);
            }
        }

        @CallSuper
        protected void onDetach() {
            this.bulletin = null;
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onDetach(this);
            }
        }

        @CallSuper
        protected void onShow() {
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onShow(this);
            }
        }

        @CallSuper
        protected void onHide() {
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onHide(this);
            }
        }

        @CallSuper
        protected void onEnterTransitionStart() {
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onEnterTransitionStart(this);
            }
        }

        @CallSuper
        protected void onEnterTransitionEnd() {
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onEnterTransitionEnd(this);
            }
        }

        @CallSuper
        protected void onExitTransitionStart() {
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onExitTransitionStart(this);
            }
        }

        @CallSuper
        protected void onExitTransitionEnd() {
            for (int i = 0, size = callbacks.size(); i < size; i++) {
                callbacks.get(i).onExitTransitionEnd(this);
            }
        }

        //region Callbacks
        public void addCallback(@NonNull Callback callback) {
            callbacks.add(callback);
        }

        public void removeCallback(@NonNull Callback callback) {
            callbacks.remove(callback);
        }

        public void updatePosition() {
            float translation = 0;
            if (delegate != null) {
                if (top) {
                    translation -= delegate.getTopOffset(bulletin != null ? bulletin.tag : 0);
                } else {
                    translation += getBottomOffset();
                }
            }
            setTranslationY(-translation + inOutOffset * (top ? -1 : 1));
        }

        public float getBottomOffset() {
            if (bulletin != null && bulletin.bottomOffsetSpring != null && bulletin.bottomOffsetSpring.isRunning()) {
                return bulletin.lastBottomOffset;
            }
            return delegate.getBottomOffset(bulletin != null ? bulletin.tag : 0);
        }

        public interface Callback {
            default void onAttach(@NonNull Layout layout, @NonNull Bulletin bulletin) {
            }

            default void onDetach(@NonNull Layout layout) {
            }

            default void onShow(@NonNull Layout layout) {
            }

            default void onHide(@NonNull Layout layout) {
            }

            default void onEnterTransitionStart(@NonNull Layout layout) {
            }

            default void onEnterTransitionEnd(@NonNull Layout layout) {
            }

            default void onExitTransitionStart(@NonNull Layout layout) {
            }

            default void onExitTransitionEnd(@NonNull Layout layout) {
            }
        }
        //endregion

        //region Transitions
        @NonNull
        public Transition createTransition() {
            return new SpringTransition();
        }

        public interface Transition {
            void animateEnter(@NonNull Layout layout, @Nullable Runnable startAction, @Nullable Runnable endAction, @Nullable Consumer<Float> onUpdate, int bottomOffset);

            void animateExit(@NonNull Layout layout, @Nullable Runnable startAction, @Nullable Runnable endAction, @Nullable Consumer<Float> onUpdate, int bottomOffset);
        }

        public static class DefaultTransition implements Transition {

            long duration = 255;

            @Override
            public void animateEnter(@NonNull Layout layout, @Nullable Runnable startAction, @Nullable Runnable endAction, @Nullable Consumer<Float> onUpdate, int bottomOffset) {
                layout.setInOutOffset(layout.getMeasuredHeight());
                if (onUpdate != null) {
                    onUpdate.accept(layout.getTranslationY());
                }
                final ObjectAnimator animator = ObjectAnimator.ofFloat(layout, IN_OUT_OFFSET_Y2, 0);
                animator.setDuration(duration);
                animator.setInterpolator(CubicBezierInterpolator.Companion.getEASE_OUT_QUAD());
                if (startAction != null || endAction != null) {
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (startAction != null) {
                                startAction.run();
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (endAction != null) {
                                endAction.run();
                            }
                        }
                    });
                }
                if (onUpdate != null) {
                    animator.addUpdateListener(a -> onUpdate.accept(layout.getTranslationY()));
                }
                animator.start();
            }

            @Override
            public void animateExit(@NonNull Layout layout, @Nullable Runnable startAction, @Nullable Runnable endAction, @Nullable Consumer<Float> onUpdate, int bottomOffset) {
                final ObjectAnimator animator = ObjectAnimator.ofFloat(layout, IN_OUT_OFFSET_Y2, layout.getHeight());
                animator.setDuration(175);
                animator.setInterpolator(CubicBezierInterpolator.Companion.getEASE_IN_QUAD());
                if (startAction != null || endAction != null) {
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (startAction != null) {
                                startAction.run();
                            }
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (endAction != null) {
                                endAction.run();
                            }
                        }
                    });
                }
                if (onUpdate != null) {
                    animator.addUpdateListener(a -> onUpdate.accept(layout.getTranslationY()));
                }
                animator.start();
            }
        }

        public static class SpringTransition implements Transition {

            private static final float DAMPING_RATIO = 0.8f;
            private static final float STIFFNESS = 400f;

            @Override
            public void animateEnter(@NonNull Layout layout, @Nullable Runnable startAction, @Nullable Runnable endAction, @Nullable Consumer<Float> onUpdate, int bottomOffset) {
                layout.setInOutOffset(layout.getMeasuredHeight());
                if (onUpdate != null) {
                    onUpdate.accept(layout.getTranslationY());
                }
                final SpringAnimation springAnimation = new SpringAnimation(layout, IN_OUT_OFFSET_Y, 0);
                springAnimation.getSpring().setDampingRatio(DAMPING_RATIO);
                springAnimation.getSpring().setStiffness(STIFFNESS);
                if (endAction != null) {
                    springAnimation.addEndListener((animation, canceled, value, velocity) -> {
                        layout.setInOutOffset(0);
                        if (!canceled) {
                            endAction.run();
                        }
                    });
                }
                if (onUpdate != null) {
                    springAnimation.addUpdateListener((animation, value, velocity) -> onUpdate.accept(layout.getTranslationY()));
                }
                springAnimation.start();
                if (startAction != null) {
                    startAction.run();
                }
            }

            @Override
            public void animateExit(@NonNull Layout layout, @Nullable Runnable startAction, @Nullable Runnable endAction, @Nullable Consumer<Float> onUpdate, int bottomOffset) {
                final SpringAnimation springAnimation = new SpringAnimation(layout, IN_OUT_OFFSET_Y, layout.getHeight());
                springAnimation.getSpring().setDampingRatio(DAMPING_RATIO);
                springAnimation.getSpring().setStiffness(STIFFNESS);
                if (endAction != null) {
                    springAnimation.addEndListener((animation, canceled, value, velocity) -> {
                        if (!canceled) {
                            endAction.run();
                        }
                    });
                }
                if (onUpdate != null) {
                    springAnimation.addUpdateListener((animation, value, velocity) -> onUpdate.accept(layout.getTranslationY()));
                }
                springAnimation.start();
                if (startAction != null) {
                    startAction.run();
                }
            }
        }

        private void setInOutOffset(float offset) {
            inOutOffset = offset;
            updatePosition();
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            if (bulletin == null) {
                return;
            }
            background.setBounds((int) ViewUtils.INSTANCE.getToPx(8), (int) ViewUtils.INSTANCE.getToPx(8), getMeasuredWidth() - (int) ViewUtils.INSTANCE.getToPx(8), getMeasuredHeight() - (int) ViewUtils.INSTANCE.getToPx(8));
            if (isTransitionRunning() && delegate != null) {
                canvas.save();
                canvas.clipRect(
                        0,
                        delegate.getTopOffset(bulletin.tag) - getY(),
                        getMeasuredWidth(),
                        ((View) getParent()).getMeasuredHeight() - getBottomOffset() - getY()
                );
                background.draw(canvas);
                super.dispatchDraw(canvas);
                canvas.restore();
                invalidate();
            } else {
                background.draw(canvas);
                super.dispatchDraw(canvas);
            }
        }
        //endregion
    }

    @SuppressLint("ViewConstructor")
    public static class ButtonLayout extends Layout {

        private Button button;
        public TimerView timerView;

        private int childrenMeasuredWidth;

        public ButtonLayout(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            childrenMeasuredWidth = 0;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (button != null && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
                setMeasuredDimension(childrenMeasuredWidth + button.getMeasuredWidth(), getMeasuredHeight());
            }
        }

        @Override
        protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            if (button != null && child != button) {
                widthUsed += button.getMeasuredWidth() - (int) ViewUtils.INSTANCE.getToPx(12);
            }
            super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
            if (child != button) {
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                childrenMeasuredWidth = Math.max(childrenMeasuredWidth, lp.leftMargin + lp.rightMargin + child.getMeasuredWidth());
            }
        }

        public Button getButton() {
            return button;
        }

        public void setButton(Button button) {
            if (this.button != null) {
                removeCallback(this.button);
                removeView(this.button);
            }
            this.button = button;
            if (button != null) {
                addCallback(button);
                addView(button, 0, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL));
            }
        }

        public void setTimer() {
            timerView = new TimerView(getContext());
            timerView.timeLeft = 5000;
            addView(timerView, LayoutHelper.INSTANCE.createFrameRelatively(20, 20, Gravity.START | Gravity.CENTER_VERTICAL, 21, 0, 21, 0));
        }
    }

    public static class SimpleLayout extends ButtonLayout {

        public final ImageView imageView;
        public final TextView textView;

        public SimpleLayout(@NonNull Context context) {
            super(context);

            final int undoInfoColor = 0xFFFFFFFF;

            imageView = new ImageView(context);
            imageView.setColorFilter(new PorterDuffColorFilter(undoInfoColor, PorterDuff.Mode.MULTIPLY));
            addView(imageView, LayoutHelper.INSTANCE.createFrameRelatively(24, 24, Gravity.START | Gravity.CENTER_VERTICAL, 16, 12, 16, 12));

            textView = new TextView(context);
            textView.setSingleLine();
            textView.setTextColor(undoInfoColor);
            textView.setTypeface(Typeface.SANS_SERIF);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            addView(textView, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 56, 0, 16, 0));
        }

        public CharSequence getAccessibilityText() {
            return textView.getText();
        }
    }

    @SuppressLint("ViewConstructor")
    public static class MultiLineLayout extends ButtonLayout {

        public final ImageView imageView = new ImageView(getContext());
        public final TextView textView = new TextView(getContext());

        public MultiLineLayout(@NonNull Context context) {
            super(context);
            addView(imageView, LayoutHelper.INSTANCE.createFrameRelatively(30, 30, Gravity.START | Gravity.CENTER_VERTICAL, 12, 8, 12, 8));

            textView.setGravity(Gravity.START);
            textView.setPadding(0, (int) ViewUtils.INSTANCE.getToPx(8), 0, (int) ViewUtils.INSTANCE.getToPx(8));
            textView.setTextColor(0xFFFFFFFF);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTypeface(Typeface.SANS_SERIF);
            addView(textView, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 56, 0, 16, 0));
        }

        public CharSequence getAccessibilityText() {
            return textView.getText();
        }
    }

    @SuppressLint("ViewConstructor")
    public static class TwoLineLayout extends ButtonLayout {

        public final ImageView imageView;
        public final TextView titleTextView;
        public final TextView subtitleTextView;
        private final LinearLayout linearLayout;

        public TwoLineLayout(@NonNull Context context) {
            super(context);

            final int undoInfoColor = 0xFFFFFFFF;

            addView(imageView = new ImageView(context), LayoutHelper.INSTANCE.createFrameRelatively(29, 29, Gravity.START | Gravity.CENTER_VERTICAL, 12, 12, 12, 12));

            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            addView(linearLayout, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 54, 8, 12, 8));

            titleTextView = new TextView(context);
            titleTextView.setSingleLine();
            titleTextView.setTextColor(undoInfoColor);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            linearLayout.addView(titleTextView);

            subtitleTextView = new TextView(context);
            subtitleTextView.setMaxLines(2);
            subtitleTextView.setTextColor(undoInfoColor);
            subtitleTextView.setLinkTextColor(0x20FFFFFF);
            subtitleTextView.setMovementMethod(new LinkMovementMethod());
            subtitleTextView.setTypeface(Typeface.SANS_SERIF);
            subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            linearLayout.addView(subtitleTextView);
        }

        public CharSequence getAccessibilityText() {
            return titleTextView.getText() + ".\n" + subtitleTextView.getText();
        }

        public void hideImage() {
            imageView.setVisibility(GONE);
            ((MarginLayoutParams) linearLayout.getLayoutParams()).setMarginStart((int) ViewUtils.INSTANCE.getToPx(12));
        }
    }

    public static class TwoLineLottieLayout extends ButtonLayout {

        public final LottieAnimationView imageView;
        public final TextView titleTextView;
        public final TextView subtitleTextView;
        private final LinearLayout linearLayout;

        private final int textColor;

        public TwoLineLottieLayout(@NonNull Context context) {
            super(context);
            this.textColor = 0xFFFFFFFF;
            setBackground(0xea06070A);

            imageView = new LottieAnimationView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            addView(imageView, LayoutHelper.INSTANCE.createFrameRelatively(56, 48, Gravity.START | Gravity.CENTER_VERTICAL));

            final int undoInfoColor = 0xffffffff;
            final int undoLinkColor = 0xff85caff;

            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            addView(linearLayout, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 52, 8, 8, 8));

            titleTextView = new TextView(context);
            titleTextView.setPadding((int) ViewUtils.INSTANCE.getToPx(4), 0, (int) ViewUtils.INSTANCE.getToPx(4), 0);
            titleTextView.setSingleLine();
            titleTextView.setTextColor(undoInfoColor);
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            linearLayout.addView(titleTextView);

            subtitleTextView = new TextView(context);
            subtitleTextView.setPadding((int) ViewUtils.INSTANCE.getToPx(4), 0, (int) ViewUtils.INSTANCE.getToPx(4), 0);
            subtitleTextView.setTextColor(undoInfoColor);
            subtitleTextView.setLinkTextColor(undoLinkColor);
            subtitleTextView.setTypeface(Typeface.SANS_SERIF);
            subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            linearLayout.addView(subtitleTextView);
        }

        @Override
        protected void onShow() {
            super.onShow();
            imageView.playAnimation();
        }

        public void setAnimation(int resId) {
            imageView.setAnimation(resId);
        }

        public CharSequence getAccessibilityText() {
            return titleTextView.getText() + ".\n" + subtitleTextView.getText();
        }

        public void hideImage() {
            imageView.setVisibility(GONE);
            ((MarginLayoutParams) linearLayout.getLayoutParams()).setMarginStart((int) ViewUtils.INSTANCE.getToPx(10));
        }
    }

    public static class LottieLayout extends ButtonLayout {

        public LottieAnimationView imageView;
        public TextView textView;

        public LottieLayout(@NonNull Context context) {
            super(context);

            imageView = new LottieAnimationView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setScaleX(0.5f);
            imageView.setScaleY(0.5f);
            addView(imageView, LayoutHelper.INSTANCE.createFrameRelatively(56, 48, Gravity.START | Gravity.CENTER_VERTICAL));

            textView = new TextView(context);
            textView.setSingleLine();
            textView.setTypeface(Typeface.SANS_SERIF);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setPadding(0, (int) ViewUtils.INSTANCE.getToPx(8), 0, (int) ViewUtils.INSTANCE.getToPx(8));
            addView(textView, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 56, 0, 8, 0));

            textView.setLinkTextColor(0xff85caff);
            setTextColor(0xFFFFFFFF);
            setBackground(0xea06070A);
        }

        public LottieLayout(@NonNull Context context, int backgroundColor, int textColor) {
            this(context);
            setBackground(backgroundColor);
            setTextColor(textColor);
        }

        public void setTextColor(int textColor) {
            textView.setTextColor(textColor);
        }

        @Override
        protected void onShow() {
            super.onShow();
            imageView.playAnimation();
        }

        public void setAnimation(int resId) {
            imageView.setAnimation(resId);
        }

        public void setIconPaddingBottom(int paddingBottom) {
            imageView.setLayoutParams(LayoutHelper.INSTANCE.createFrameRelatively(56, 48 - paddingBottom, Gravity.START | Gravity.CENTER_VERTICAL, 0, 0, 0, paddingBottom));
        }

        public CharSequence getAccessibilityText() {
            return textView.getText();
        }
    }

    public static interface LoadingLayout {
        void onTextLoaded(CharSequence text);
    }

    public static class LoadingLottieLayout extends LottieLayout implements LoadingLayout {

        public TextView textLoadingView;

        public LoadingLottieLayout(@NonNull Context context) {
            super(context);

            textLoadingView = new TextView(context);
            textLoadingView.setSingleLine();
            textLoadingView.setTypeface(Typeface.SANS_SERIF);
            textLoadingView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textLoadingView.setEllipsize(TextUtils.TruncateAt.END);
            textLoadingView.setPadding(0, (int) ViewUtils.INSTANCE.getToPx(8), 0, (int) ViewUtils.INSTANCE.getToPx(8));
            textView.setVisibility(View.GONE);
            addView(textLoadingView, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 56, 0, 8, 0));

            setTextColor(0xffffffff);
        }

        public LoadingLottieLayout(@NonNull Context context, int backgroundColor, int textColor) {
            this(context);
            setBackground(backgroundColor);
            setTextColor(textColor);
        }

        @Override
        public void setTextColor(int textColor) {
            super.setTextColor(textColor);
            if (textLoadingView != null) {
                textLoadingView.setTextColor(textColor);
            }
        }

        public void onTextLoaded(CharSequence text) {
            textView.setText(text);
            AndroidUtils.INSTANCE.updateViewShow(textLoadingView, false, false, true);
            AndroidUtils.INSTANCE.updateViewShow(textView, true, false, true);
        }
    }
    //endregion

    //region Buttons
    @SuppressLint("ViewConstructor")
    public abstract static class Button extends FrameLayout implements Layout.Callback {

        public Button(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onAttach(@NonNull Layout layout, @NonNull Bulletin bulletin) {
        }

        @Override
        public void onDetach(@NonNull Layout layout) {
        }

        @Override
        public void onShow(@NonNull Layout layout) {
        }

        @Override
        public void onHide(@NonNull Layout layout) {
        }

        @Override
        public void onEnterTransitionStart(@NonNull Layout layout) {
        }

        @Override
        public void onEnterTransitionEnd(@NonNull Layout layout) {
        }

        @Override
        public void onExitTransitionStart(@NonNull Layout layout) {
        }

        @Override
        public void onExitTransitionEnd(@NonNull Layout layout) {
        }
    }

    @SuppressLint("ViewConstructor")
    public static final class UndoButton extends Button {

        private Runnable undoAction;
        private Runnable delayedAction;

        private Bulletin bulletin;
        private TextView undoTextView;
        private boolean isUndone;

        public UndoButton(@NonNull Context context, boolean text) {
            super(context);

            final int undoCancelColor = 0xff85caff;

            if (text) {
                undoTextView = new TextView(context);
                undoTextView.setOnClickListener(v -> undo());
                undoTextView.setBackground(ViewUtils.INSTANCE.createSelectorDrawable((undoCancelColor & 0x00ffffff) | 0x19000000, ViewUtils.RIPPLE_MASK_ROUNDRECT_6DP));
                undoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                undoTextView.setTextColor(undoCancelColor);
                undoTextView.setText("Undo");
                undoTextView.setGravity(Gravity.CENTER_VERTICAL);
                ViewHelper.INSTANCE.setPaddingRelative(undoTextView, 12, 8, 12, 8);
                addView(undoTextView, LayoutHelper.INSTANCE.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 8, 0, 8, 0));
            } else {
                final ImageView undoImageView = new ImageView(getContext());
                undoImageView.setOnClickListener(v -> undo());
                undoImageView.setImageResource(R.drawable.ic_arrow_12);
                undoImageView.setColorFilter(new PorterDuffColorFilter(undoCancelColor, PorterDuff.Mode.MULTIPLY));
                undoImageView.setBackground(ViewUtils.INSTANCE.createSelectorDrawable((undoCancelColor & 0x00ffffff) | 0x19000000));
                ViewHelper.INSTANCE.setPaddingRelative(undoImageView, 0, 12, 0, 12);
                addView(undoImageView, LayoutHelper.INSTANCE.createFrameRelatively(56, 48, Gravity.CENTER_VERTICAL));
            }
        }

        public UndoButton setText(CharSequence text) {
            if (undoTextView != null) {
                undoTextView.setText(text);
            }
            return this;
        }

        public void undo() {
            if (bulletin != null) {
                isUndone = true;
                if (undoAction != null) {
                    undoAction.run();
                }
                bulletin.hide();
            }
        }

        @Override
        public void onAttach(@NonNull Layout layout, @NonNull Bulletin bulletin) {
            this.bulletin = bulletin;
        }

        @Override
        public void onDetach(@NonNull Layout layout) {
            this.bulletin = null;
            if (delayedAction != null && !isUndone) {
                delayedAction.run();
            }
        }

        public UndoButton setUndoAction(Runnable undoAction) {
            this.undoAction = undoAction;
            return this;
        }

        public UndoButton setDelayedAction(Runnable delayedAction) {
            this.delayedAction = delayedAction;
            return this;
        }
    }

    // TODO: possibility of loading icon as well
    public void onLoaded(CharSequence text) {
        loaded = true;
        if (layout instanceof LoadingLayout) {
            ((LoadingLayout) layout).onTextLoaded(text);
        }
        setCanHide(true);
    }

    //endregion

    public static class EmptyBulletin extends Bulletin {

        public EmptyBulletin() {
            super();
        }

        @Override
        public Bulletin show() {
            return this;
        }
    }

    private static class TimerView extends View {

        private final Paint progressPaint;
        private long timeLeft;
        private int prevSeconds;
        private String timeLeftString;
        private int textWidth;

        StaticLayout timeLayout;
        StaticLayout timeLayoutOut;
        int textWidthOut;

        float timeReplaceProgress = 1f;

        private TextPaint textPaint;
        private long lastUpdateTime;
        RectF rect = new RectF();

        public TimerView(Context context) {
            super(context);

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize((int) ViewUtils.INSTANCE.getToPx(12));
            textPaint.setColor(0xFFFFFFFF);

            progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setStrokeWidth((int) ViewUtils.INSTANCE.getToPx(2));
            progressPaint.setStrokeCap(Paint.Cap.ROUND);
            progressPaint.setColor(0xFFFFFFFF);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int newSeconds = timeLeft > 0 ? (int) Math.ceil(timeLeft / 1000.0f) : 0;
            rect.set((int) ViewUtils.INSTANCE.getToPx(1), (int) ViewUtils.INSTANCE.getToPx(1), getMeasuredWidth() - (int) ViewUtils.INSTANCE.getToPx(1), getMeasuredHeight() - (int) ViewUtils.INSTANCE.getToPx(1));
            if (prevSeconds != newSeconds) {
                prevSeconds = newSeconds;
                timeLeftString = String.valueOf(Math.max(0, newSeconds));
                if (timeLayout != null) {
                    timeLayoutOut = timeLayout;
                    timeReplaceProgress = 0;
                    textWidthOut = textWidth;
                }
                textWidth = (int) Math.ceil(textPaint.measureText(timeLeftString));
                timeLayout = new StaticLayout(timeLeftString, textPaint, Integer.MAX_VALUE, android.text.Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }

            if (timeReplaceProgress < 1f) {
                timeReplaceProgress += 16f / 150f;
                if (timeReplaceProgress > 1f) {
                    timeReplaceProgress = 1f;
                } else {
                    invalidate();
                }
            }

            int alpha = textPaint.getAlpha();

            if (timeLayoutOut != null && timeReplaceProgress < 1f) {
                textPaint.setAlpha((int) (alpha * (1f - timeReplaceProgress)));
                canvas.save();
                canvas.translate(rect.centerX() - textWidthOut / 2f, rect.centerY() - timeLayoutOut.getHeight() / 2f + (int) ViewUtils.INSTANCE.getToPx(10) * timeReplaceProgress);
                timeLayoutOut.draw(canvas);
                textPaint.setAlpha(alpha);
                canvas.restore();
            }

            if (timeLayout != null) {
                if (timeReplaceProgress != 1f) {
                    textPaint.setAlpha((int) (alpha * timeReplaceProgress));
                }
                canvas.save();
                canvas.translate(rect.centerX() - textWidth / 2f, rect.centerY() - timeLayout.getHeight() / 2f - (int) ViewUtils.INSTANCE.getToPx(10) * (1f - timeReplaceProgress));
                timeLayout.draw(canvas);
                if (timeReplaceProgress != 1f) {
                    textPaint.setAlpha(alpha);
                }
                canvas.restore();
            }

            canvas.drawArc(rect, -90, -360 * (Math.max(0, timeLeft) / 5000.0f), false, progressPaint);

            if (lastUpdateTime != 0) {
                long newTime = System.currentTimeMillis();
                long dt = newTime - lastUpdateTime;
                timeLeft -= dt;
                lastUpdateTime = newTime;
            } else {
                lastUpdateTime = System.currentTimeMillis();
            }
            invalidate();
        }
    }

    // to make bulletin above everything
    // use as BulletinFactory.of(BulletinWindow.make(context), resourcesProvider)...
    public static class BulletinWindow extends Dialog {
        public static FrameLayout make(Context context) {
            return new BulletinWindow(context).container;
        }

        private final FrameLayout container;

        private BulletinWindow(Context context) {
            super(context);
            setContentView(
                    container = new FrameLayout(context) {
                        @Override
                        public void addView(View child) {
                            super.addView(child);
                            BulletinWindow.this.show();
                        }

                        @Override
                        public void removeView(View child) {
                            super.removeView(child);
                            try {
                                BulletinWindow.this.dismiss();
                            } catch (Exception ignore) {

                            }
                            removeDelegate(container);
                        }
                    },
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            );
            container.setFitsSystemWindows(true);
            container.setOnApplyWindowInsetsListener((v, insets) -> {
                applyInsets(insets);
                v.requestLayout();
                if (Build.VERSION.SDK_INT >= 30) {
                    return WindowInsets.CONSUMED;
                } else {
                    return insets.consumeSystemWindowInsets();
                }
            });
            if (Build.VERSION.SDK_INT >= 30) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            } else {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            addDelegate(container, new Delegate() {
                @Override
                public int getBottomOffset(int tag) {
                    return 0;
                }

                @Override
                public int getTopOffset(int tag) {
                    return 0;
                }
            });

            try {
                Window window = getWindow();
                window.setBackgroundDrawable(null);
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.dimAmount = 0;
                params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
                params.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                if (Build.VERSION.SDK_INT >= 28) {
                    params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                }
                window.setAttributes(params);
            } catch (Exception ignore) {
            }
        }

        private void applyInsets(WindowInsets insets) {
            if (container != null) {
                container.setPadding(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom()
                );
            }
        }
    }
}