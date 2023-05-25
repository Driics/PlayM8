package ru.driics.playm8.core.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import ru.driics.playm8.MyApplication
import ru.driics.playm8.core.utils.ViewUtils.toPx
import ru.driics.playm8.core.utils.animations.CubicBezierInterpolator


object AndroidUtils {
    inline fun <reified T : Any> Context.launchActivity(
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {}
    ) {
        val intent = Intent(this, T::class.java)
        intent.init()
        startActivity(intent, options)
    }

    inline fun <reified T : Fragment> AppCompatActivity.launchFragment(@IdRes fragmentContainerId: Int) {
        supportFragmentManager.commit {
            replace<T>(fragmentContainerId)
            setReorderingAllowed(true)
            addToBackStack(T::class.java.name)
        }
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

    fun updateViewShow(view: View?, show: Boolean) {
        updateViewShow(view, show, true, true)
    }

    fun updateViewShow(view: View?, show: Boolean, scale: Boolean, animated: Boolean) {
        updateViewShow(view, show, scale, 0f, animated, null)
    }

    fun updateViewShow(
        view: View?,
        show: Boolean,
        scale: Boolean,
        animated: Boolean,
        onDone: Runnable?
    ) {
        updateViewShow(view, show, scale, 0f, animated, onDone)
    }

    fun updateViewShow(
        view: View?,
        show: Boolean,
        scale: Boolean,
        translate: Float,
        animated1: Boolean,
        onDone: Runnable?
    ) {
        var animated = animated1
        if (view == null) {
            return
        }
        if (view.parent == null) {
            animated = false
        }
        view.animate().setListener(null).cancel()
        if (!animated) {
            view.visibility = if (show) View.VISIBLE else View.GONE
            view.tag = if (show) 1 else null
            view.alpha = 1f
            view.scaleX = if (scale && !show) 0f else 1f
            view.scaleY = if (scale && !show) 0f else 1f
            if (translate != 0f) {
                view.translationY = (if (show) 0 else (-16).toPx * translate) as Float
            }
            onDone?.run()
        } else if (show) {
            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
                view.alpha = 0f
                view.scaleX = (if (scale) 0 else 1).toFloat()
                view.scaleY = (if (scale) 0 else 1).toFloat()
                if (translate != 0f) {
                    view.translationY = (-16).toPx * translate
                }
            }
            var animate = view.animate()
            animate = animate.alpha(1f).scaleY(1f).scaleX(1f)
                .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(340)
                .withEndAction(onDone)
            if (translate != 0f) {
                animate.translationY(0f)
            }
            animate.start()
        } else {
            var animate = view.animate()
            animate = animate.alpha(0f).scaleY((if (scale) 0 else 1).toFloat())
                .scaleX((if (scale) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        view.visibility = View.GONE
                    }
                })
                .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(340)
                .withEndAction(onDone)
            if (translate != 0f) {
                animate.translationY((-16).toPx * translate)
            }
            animate.start()
        }
    }
}