package ru.driics.playm8.utils.cache

import android.graphics.Bitmap
import java.security.SecureRandom


object Utils {
    @Volatile
    var globalQueue: DispatchQueue = DispatchQueue("globalQueue")

    var random = SecureRandom()

    fun clamp(value: Int, maxValue: Int, minValue: Int): Int {
        return value.coerceAtMost(maxValue).coerceAtLeast(minValue)
    }

    fun clamp(value: Float, maxValue: Float, minValue: Float): Float {
        if (java.lang.Float.isNaN(value)) {
            return minValue
        }
        return if (java.lang.Float.isInfinite(value)) {
            maxValue
        } else value.coerceAtMost(maxValue).coerceAtLeast(minValue)
    }
}