package ru.driics.playm8.components

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import ru.driics.playm8.core.utils.ViewUtils.toPx

class EmailVerificationDrawable(
    textSize: Int = 16,
    private val text: String
) : Drawable() {
    private val rect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val textWidth: Int

    init {
        textPaint.textSize = textSize.toPx
        textPaint.color = -0x1000000

        paint.color = -0x1
        textWidth = kotlin.math.ceil(textPaint.measureText(text).toDouble()).toInt()
    }

    override fun draw(canvas: Canvas) {
        rect.set(bounds)
        canvas.drawRoundRect(
            rect,
            2.toPx,
            2.toPx, paint
        )
        canvas.drawText(
            text,
            rect.left + 5.toPx,
            rect.top + 12.toPx,
            textPaint
        )
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.RGB_888", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    override fun getIntrinsicHeight(): Int {
        return 16.toPx.toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return textWidth + (5 * 2).toPx.toInt()
    }
}