package ru.driics.playm8.components.viewpager.indicator;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Dimension;

import ru.driics.playm8.R;

public class ShapePageIndicator extends AbstractPageIndicator<View> {
    private static final int INDICATOR_INACTIVE = 0;
    private static final int INDICATOR_RECTANGLE = 1;
    private static final int INDICATOR_OVAL = 2;
    private static final IndicatorShape[] INDICATOR_SHAPES = {IndicatorShape.RECTANGLE, IndicatorShape.OVAL};
    private static final IndicatorStyle[] INDICATOR_STYLES = {IndicatorStyle.FILL, IndicatorStyle.STROKE};

    private final ArgbEvaluator evaluator;

    private IndicatorShape shape;
    private IndicatorStyle style;
    @Dimension
    private int shapeCornerRadius;
    @Dimension
    private int shapeStrokeSize;

    public enum IndicatorShape {
        RECTANGLE,
        OVAL
    }

    public enum IndicatorStyle {
        FILL,
        STROKE
    }

    public ShapePageIndicator(Context context) {
        this(context, null, 0);
    }

    public ShapePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapePageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.evaluator = new ArgbEvaluator();
        this.shape = IndicatorShape.OVAL;
        this.shapeCornerRadius = -2;
        this.style = IndicatorStyle.FILL;

        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ShapePageIndicator);

        int shape = styledAttributes.getInt(R.styleable.ShapePageIndicator_ds_indicatorShape, -1);
        if (shape >= 0) {
            IndicatorShape[] arr = INDICATOR_SHAPES;
            if (shape < arr.length) {
                this.shape = arr[shape];
            }
        }

        int style = styledAttributes.getInt(R.styleable.ShapePageIndicator_ds_indicatorShapeStyle, -1);
        if (style >= 0) {
            IndicatorStyle[] arr = INDICATOR_STYLES;
            if (style < arr.length) {
                this.style = arr[style];
            }
        }

        this.shapeCornerRadius = styledAttributes.getLayoutDimension(R.styleable.ShapePageIndicator_ds_indicatorShapeCornerRadius, -2);
        this.shapeStrokeSize = styledAttributes.getDimensionPixelSize(R.styleable.ShapePageIndicator_ds_indicatorStrokeSize, 0);
        styledAttributes.recycle();

        if (isInEditMode()) {
            invalidate();
            onPageScrolled(0, 0);
        }
    }

    @Override
    protected void onPageScrolled(int position, float positionOffset) {
        if (positionOffset == 0) {
            invalidateViews();
            return;
        }

        Object evaluateFromActive = this.evaluator.evaluate(positionOffset, getActiveIndicatorColor(), getIndicatorColor());
        int colorFromActive = (Integer) evaluateFromActive;

        Object evaluateToActive = this.evaluator.evaluate(positionOffset, getIndicatorColor(), getActiveIndicatorColor());
        int colorToActive = (Integer) evaluateToActive;

        View indicator = getByIndex(position);
        Drawable indicatorBackground = indicator != null ? indicator.getBackground() : null;
        GradientDrawable drawable = indicatorBackground instanceof GradientDrawable ? (GradientDrawable) indicatorBackground : null;
        if (drawable != null) {
            drawable.setTint(colorFromActive);
        }

        View nextIndicator = getByIndex(position + 1);
        Drawable nextIndicatorBackground = nextIndicator != null ? nextIndicator.getBackground() : null;
        GradientDrawable nextIndicatorBackgroundDrawable = nextIndicatorBackground instanceof GradientDrawable ? (GradientDrawable) nextIndicatorBackground : null;
        if (nextIndicatorBackgroundDrawable != null) {
            nextIndicatorBackgroundDrawable.setTint(colorToActive);
        }

    }

    @Override
    protected void setIndicatorBackground(int position, View view) {
        view.setBackground(buildDrawable(isItElement(position), view.getBackground()));
    }

    @Override
    protected View createIndicatorView(int position) {
        View view = new View(getContext());
        view.setBackground(createShapeIndicatorDrawable(this, isItElement(position), null, 2, null));
        return view;
    }

    public final ArgbEvaluator getEvaluator() {
        return this.evaluator;
    }

    public IndicatorShape getShape() {
        return this.shape;
    }

    public IndicatorStyle getStyle() {
        return this.style;
    }

    @Dimension
    public int getShapeCornerRadius() {
        return this.shapeCornerRadius;
    }

    @Dimension
    public int getShapeStrokeSize() {
        return this.shapeStrokeSize;
    }

    private final void setUpOvalDrawable(GradientDrawable drawable) {
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setCornerRadius(0);
    }

    private final void setUpRectangleDrawable(GradientDrawable drawable) {
        float cornerRadius = getIndicatorHeightValue() / 2f;
        if (getShapeCornerRadius() != -2) {
            cornerRadius = Math.min(getShapeCornerRadius(), cornerRadius);
        }
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(cornerRadius);
    }

    public static Drawable createShapeIndicatorDrawable(ShapePageIndicator shapePageIndicator, boolean isActiveIndicator, Drawable backgroundDrawable, int flags, Object extra) {
        if (extra == null) {
            if ((flags & 1) != 0) {
                isActiveIndicator = false;
            }
            if ((flags & 2) != 0) {
                backgroundDrawable = null;
            }
            return shapePageIndicator.buildDrawable(isActiveIndicator, backgroundDrawable);
        }
        throw new IllegalArgumentException();
    }

    protected final Drawable buildDrawable(boolean itActiveIndicator, Drawable drawable) {
        GradientDrawable gradientDrawable = drawable instanceof GradientDrawable ? (GradientDrawable) drawable : new GradientDrawable();
        int type = ShapeType.SHAPE_TYPE_ARRAY[getShape().ordinal()];

        if (type == INDICATOR_RECTANGLE) {
            setUpRectangleDrawable(gradientDrawable);
        } else if (type == INDICATOR_OVAL) {
            setUpOvalDrawable(gradientDrawable);
        }

        if (getStyle() == IndicatorStyle.STROKE) {
            gradientDrawable.setStroke(getShapeStrokeSize(), 0xFF000000);
            gradientDrawable.setColor(Color.TRANSPARENT);
        } else {
            gradientDrawable.setStroke(0, Color.TRANSPARENT);
            gradientDrawable.setColor(0xFF000000);
        }
        gradientDrawable.setTint(itActiveIndicator ? getActiveIndicatorColor() : getIndicatorColor());
        return gradientDrawable;
    }

    public static class ShapeType {
        public static final int[] SHAPE_TYPE_ARRAY;

        static {
            int[] arr = new int[IndicatorShape.values().length];
            arr[IndicatorShape.RECTANGLE.ordinal()] = INDICATOR_RECTANGLE;
            arr[IndicatorShape.OVAL.ordinal()] = INDICATOR_OVAL;
            SHAPE_TYPE_ARRAY = arr;
        }
    }
}
