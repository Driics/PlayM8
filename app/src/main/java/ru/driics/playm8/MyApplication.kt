package ru.driics.playm8

import android.app.Application
import android.content.Context
import android.os.Handler
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Companion.applicationContext = applicationContext
        applicationHandler = Handler(applicationContext.mainLooper)
            //DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        @JvmField
        @Volatile
        var applicationContext: Context? = null

        @JvmField
        @Volatile
        var applicationHandler: Handler? = null
    }
}