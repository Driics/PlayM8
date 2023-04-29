package ru.driics.playm8.utils

import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import ru.driics.playm8.utils.ViewUtils.toPx


object LayoutHelper {
    const val MATCH_PARENT = -1
    const val WRAP_CONTENT = -2

    private fun getSize(size: Float): Int {
        return (if (size < 0) size else size.toPx).toInt()
    }

    fun createFrame(
        width: Int,
        height: Float,
        gravity: Int,
        leftMargin: Float,
        topMargin: Float,
        rightMargin: Float,
        bottomMargin: Float
    ): FrameLayout.LayoutParams {
        val layoutParams =
            FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height), gravity)
        layoutParams.setMargins(
            leftMargin.toPx.toInt(),
            topMargin.toPx.toInt(),
            rightMargin.toPx.toInt(),
            bottomMargin.toPx.toInt()
        )
        return layoutParams
    }

    fun createFrame(width: Int, height: Int, gravity: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            getSize(width.toFloat()),
            getSize(height.toFloat()),
            gravity
        )
    }

    fun createFrame(width: Int, height: Float): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height))
    }

    fun createFrame(width: Float, height: Float, gravity: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(getSize(width), getSize(height), gravity)
    }

    fun createFrameRelatively(
        width: Float,
        height: Float,
        gravity: Int,
        startMargin: Float,
        topMargin: Float,
        endMargin: Float,
        bottomMargin: Float
    ): FrameLayout.LayoutParams {
        val layoutParams =
            FrameLayout.LayoutParams(getSize(width), getSize(height), getAbsoluteGravity(gravity))
        layoutParams.leftMargin = startMargin.toPx.toInt()
        layoutParams.topMargin = topMargin.toPx.toInt()
        layoutParams.rightMargin = endMargin.toPx.toInt()
        layoutParams.bottomMargin = bottomMargin.toPx.toInt()
        return layoutParams
    }

    private fun getAbsoluteGravity(gravity: Int): Int {
        return Gravity.getAbsoluteGravity(
            gravity,
            ViewCompat.LAYOUT_DIRECTION_LTR
        )
    }

    fun createFrameRelatively(
        width: Float,
        height: Float,
        gravity: Int
    ): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            getSize(width),
            getSize(height),
            getAbsoluteGravity(gravity)
        )
    }
}