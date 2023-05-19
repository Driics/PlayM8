package ru.driics.playm8.core.utils

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.StateSet
import ru.driics.playm8.core.utils.ViewUtils.toPx
import ru.driics.playm8.core.utils.random.Xoroshiro128PlusRandom
import java.security.SecureRandom
import java.util.Random


object Utils {
    @JvmField
    var random = SecureRandom()

    @JvmField
    var fastRandom: Random = Xoroshiro128PlusRandom(random.nextLong())

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    class RippleRadMaskDrawable : Drawable {
        private val path: Path = Path()
        private val rectTmp = RectF()
        private val radii = FloatArray(8)
        var invalidatePath = true

        constructor(top: Float, bottom: Float) {
            radii[3] = (top).toPx
            radii[2] = radii[3]
            radii[1] = radii[2]
            radii[0] = radii[1]
            radii[7] = (bottom).toPx
            radii[6] = radii[7]
            radii[5] = radii[6]
            radii[4] = radii[5]
        }

        constructor(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
            radii[1] = (topLeft).toPx
            radii[0] = radii[1]
            radii[3] = (topRight).toPx
            radii[2] = radii[3]
            radii[5] = (bottomRight).toPx
            radii[4] = radii[5]
            radii[7] = (bottomLeft).toPx
            radii[6] = radii[7]
        }

        fun setRadius(top: Float, bottom: Float) {
            radii[3] = (top).toPx
            radii[2] = radii[3]
            radii[1] = radii[2]
            radii[0] = radii[1]
            radii[7] = (bottom).toPx
            radii[6] = radii[7]
            radii[5] = radii[6]
            radii[4] = radii[5]
            invalidatePath = true
            invalidateSelf()
        }

        fun setRadius(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
            radii[1] = (topLeft).toPx
            radii[0] = radii[1]
            radii[3] = (topRight).toPx
            radii[2] = radii[3]
            radii[5] = (bottomRight).toPx
            radii[4] = radii[5]
            radii[7] = (bottomLeft).toPx
            radii[6] = radii[7]
            invalidatePath = true
            invalidateSelf()
        }

        override fun onBoundsChange(bounds: Rect) {
            invalidatePath = true
        }

        override fun draw(canvas: Canvas) {
            if (invalidatePath) {
                invalidatePath = false
                path.reset()
                rectTmp.set(bounds)
                path.addRoundRect(rectTmp, radii, Path.Direction.CW)
            }
            canvas.drawPath(path, maskPaint)
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        override fun getOpacity(): Int {
            return PixelFormat.UNKNOWN
        }
    }

    fun setMaskDrawableRad(rippleDrawable: Drawable?, top: Int, bottom: Int) {
        if (rippleDrawable is RippleDrawable) {
            val drawable = rippleDrawable
            val count = drawable.numberOfLayers
            for (a in 0 until count) {
                val layer = drawable.getDrawable(a)
                if (layer is RippleRadMaskDrawable) {
                    layer.setRadius(top.toFloat(), bottom.toFloat())
                    break
                }
            }
        }
    }

    fun setMaskDrawableRad(
        rippleDrawable: Drawable?,
        topLeftRad: Float,
        topRightRad: Float,
        bottomRightRad: Float,
        bottomLeftRad: Float
    ) {
        if (Build.VERSION.SDK_INT < 21) {
            return
        }
        if (rippleDrawable is RippleDrawable) {
            val drawable = rippleDrawable
            val count = drawable.numberOfLayers
            for (a in 0 until count) {
                val layer = drawable.getDrawable(a)
                if (layer is RippleRadMaskDrawable) {
                    layer.setRadius(topLeftRad, topRightRad, bottomRightRad, bottomLeftRad)
                    break
                }
            }
        }
    }

    fun createRadSelectorDrawable(color: Int, topRad: Int, bottomRad: Int): Drawable {
        maskPaint.color = -0x1
        val maskDrawable: Drawable =
            RippleRadMaskDrawable(topRad.toFloat(), bottomRad.toFloat())
        val colorStateList =
            ColorStateList(arrayOf<IntArray>(StateSet.WILD_CARD), intArrayOf(color))
        return RippleDrawable(colorStateList, null, maskDrawable)
    }
}