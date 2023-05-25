package ru.driics.playm8.core.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.util.StateSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import kotlin.math.ceil
import kotlin.math.sqrt


object ViewUtils {
    /**
     * Shortcut for [View.setOnClickListener]
     * This also allow us to pass a function KProperty argument
     * e.g. `` view.onClick(::rickRoll) ``
     */
    inline fun View.onClick(crossinline block: () -> Unit) = setOnClickListener { block() }

    inline fun <VB : ViewBinding> Activity.viewBinding(crossinline inflater: (LayoutInflater) -> VB) =
        lazy(LazyThreadSafetyMode.NONE) { inflater(layoutInflater) }

    val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    fun createSelectorDrawable(color: Int): Drawable? {
        return createSelectorDrawable(color, RIPPLE_MASK_CIRCLE_20DP, -1)
    }

    fun createSelectorDrawable(color: Int, maskType: Int): Drawable? {
        return createSelectorDrawable(color, maskType, -1)
    }

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    const val RIPPLE_MASK_CIRCLE_20DP = 1
    const val RIPPLE_MASK_ALL = 2
    const val RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE = 3
    const val RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4
    const val RIPPLE_MASK_CIRCLE_AUTO = 5
    const val RIPPLE_MASK_ROUNDRECT_6DP = 7

    fun createSelectorDrawable(color: Int, maskType: Int, radius: Int): Drawable? {
        return if (Build.VERSION.SDK_INT >= 21) {
            var maskDrawable: Drawable? = null
            if ((maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 5)) {
                maskDrawable = null
            } else if (maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE || maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER || maskType == RIPPLE_MASK_CIRCLE_AUTO || maskType == 6 || maskType == RIPPLE_MASK_ROUNDRECT_6DP) {
                maskPaint.color = -0x1
                maskDrawable = object : Drawable() {
                    var rect: RectF? = null
                    override fun draw(canvas: Canvas) {
                        val bounds = bounds
                        if (maskType == RIPPLE_MASK_ROUNDRECT_6DP) {
                            if (rect == null) {
                                rect = RectF()
                            }
                            rect!!.set(bounds)
                            val rad = if (radius <= 0) (6).toPx else radius.toFloat()
                            canvas.drawRoundRect(rect!!, rad, rad, maskPaint)
                        } else {
                            val rad: Int =
                                if (maskType == RIPPLE_MASK_CIRCLE_20DP || maskType == 6) {
                                    if (radius <= 0) (20).toPx.toInt() else radius
                                } else if (maskType == RIPPLE_MASK_CIRCLE_TO_BOUND_EDGE) {
                                    bounds.width().coerceAtLeast(bounds.height()) / 2
                                } else {
                                    // RIPPLE_MASK_CIRCLE_AUTO = 5
                                    // RIPPLE_MASK_CIRCLE_TO_BOUND_CORNER = 4
                                    ceil(sqrt(((bounds.left - bounds.centerX()) * (bounds.left - bounds.centerX()) + (bounds.top - bounds.centerY()) * (bounds.top - bounds.centerY())).toDouble()))
                                        .toInt()
                                }
                            canvas.drawCircle(
                                bounds.centerX().toFloat(),
                                bounds.centerY().toFloat(),
                                rad.toFloat(),
                                maskPaint
                            )
                        }
                    }

                    override fun setAlpha(alpha: Int) {}
                    override fun setColorFilter(colorFilter: ColorFilter?) {}
                    override fun getOpacity(): Int {
                        return PixelFormat.UNKNOWN
                    }
                }
            } else if (maskType == RIPPLE_MASK_ALL) {
                maskDrawable = ColorDrawable(-0x1)
            }
            val colorStateList =
                ColorStateList(arrayOf<IntArray>(StateSet.WILD_CARD), intArrayOf(color))
            val rippleDrawable = RippleDrawable(colorStateList, null, maskDrawable)
            if (maskType == RIPPLE_MASK_CIRCLE_20DP) {
                rippleDrawable.radius = if (radius <= 0) (20).toPx.toInt() else radius
            } else if (maskType == RIPPLE_MASK_CIRCLE_AUTO) {
                rippleDrawable.radius = RippleDrawable.RADIUS_AUTO
            }
            rippleDrawable
        } else {
            val stateListDrawable = StateListDrawable()
            stateListDrawable.addState(
                intArrayOf(android.R.attr.state_pressed),
                ColorDrawable(color)
            )
            stateListDrawable.addState(
                intArrayOf(android.R.attr.state_selected),
                ColorDrawable(color)
            )
            stateListDrawable.addState(StateSet.WILD_CARD, ColorDrawable(0x00000000))
            stateListDrawable
        }
    }
}