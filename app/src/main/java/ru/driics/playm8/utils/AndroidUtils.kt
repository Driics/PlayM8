package ru.driics.playm8.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.annotation.DrawableRes
import ru.driics.playm8.MyApplication
import ru.driics.playm8.utils.cache.Utils


object AndroidUtils {
    inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}
    ) {
        val intent = Intent(this, T::class.java)
        intent.init()
        startActivity(intent, options)
    }

    fun Context.registerBroadcastReceiver(
        intentFilter: IntentFilter,
        onReceive: (intent: Intent?) -> Unit
    ): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onReceive(intent)
            }
        }
        this.registerReceiver(receiver, intentFilter)
        return receiver
    }

    fun Context.makeAccessibilityAnnouncement(what: CharSequence?) {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (am.isEnabled) {
            val ev = AccessibilityEvent.obtain()
            ev.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            ev.text.add(what)
            am.sendAccessibilityEvent(ev)
        }
    }

    fun Button.setEndDrawable(@DrawableRes id: Int = 0) =
        this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, id, 0)


    @JvmOverloads
    fun runOnUIThread(runnable: Runnable, delay: Long = 0) {
        if (MyApplication.applicationHandler == null)
            return

        if (delay == 0L) {
            MyApplication.applicationHandler!!.post(runnable)
        } else {
            MyApplication.applicationHandler!!.postDelayed(runnable, delay)
        }
    }

    fun cancelRunOnUIThread(runnable: Runnable) {
        if (MyApplication.applicationHandler == null)
            return

        MyApplication.applicationHandler!!.removeCallbacks(runnable)
    }

    fun recycleBitmap(image: Bitmap) {
        recycleBitmaps(listOf(image))
    }

    fun recycleBitmaps(bitmapToRecycle: List<Bitmap>) {
        if (bitmapToRecycle.isNotEmpty()) {
            runOnUIThread({
                Utils.globalQueue.postRunnable {
                    for (i in bitmapToRecycle.indices) {
                        val bitmap = bitmapToRecycle[i]
                        if (!bitmap.isRecycled) {
                            try {
                                bitmap.recycle()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }, 36)
        }
    }
}