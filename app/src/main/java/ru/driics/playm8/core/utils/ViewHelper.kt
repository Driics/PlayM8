package ru.driics.playm8.core.utils

import android.view.View
import ru.driics.playm8.core.utils.ViewUtils.toPx

object ViewHelper {
    fun setPadding(view: View, padding: Float) {
        val px = if (padding != 0f) padding.toInt() else 0
        view.setPadding(px, px, px, px)
    }

    fun setPadding(view: View, left: Float, top: Float, right: Float, bottom: Float) {
        view.setPadding(
            left.toPx.toInt(),
            top.toPx.toInt(),
            right.toPx.toInt(),
            bottom.toPx.toInt()
        )
    }

    fun setPaddingRelative(view: View, start: Float, top: Float, end: Float, bottom: Float) {
        setPadding(view, start, top, end, bottom)
    }

    fun getPaddingStart(view: View): Int {
        return view.paddingLeft
    }

    fun getPaddingEnd(view: View): Int {
        return view.paddingRight
    }
}