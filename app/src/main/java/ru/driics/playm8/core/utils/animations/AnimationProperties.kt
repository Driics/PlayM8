package ru.driics.playm8.core.utils.animations

import android.util.Property

class AnimationProperties {
    abstract class FloatProperty<T>(name: String?) : Property<T, Float>(
        Float::class.java, name
    ) {
        abstract fun setValue(obj: T, value: Float)
        override fun set(`object`: T, value: Float) {
            setValue(`object`, value)
        }
    }
}