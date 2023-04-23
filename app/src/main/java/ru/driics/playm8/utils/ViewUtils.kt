package ru.driics.playm8.utils

import android.app.Activity
import android.content.res.Resources
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import kotlin.math.roundToInt


object ViewUtils {
    inline fun <VB : ViewBinding> Activity.viewBinding(crossinline inflater: (LayoutInflater) -> VB) =
        lazy(LazyThreadSafetyMode.NONE) { inflater(layoutInflater) }

    private fun convertDpToPixel(dp: Int): Int =
        (dp * Resources.getSystem().displayMetrics.density).roundToInt()

    private fun convertPixelsToDp(px: Int): Float =
        px.toFloat() / Resources.getSystem().displayMetrics.density

    fun Int.toPx(): Int = convertDpToPixel(this)

    fun Int.toDp(): Float = convertPixelsToDp(this)
}