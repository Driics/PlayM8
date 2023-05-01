package ru.driics.playm8.components.bulletin

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import ru.driics.playm8.MyApplication
import ru.driics.playm8.R
import ru.driics.playm8.components.bulletin.Bulletin.LottieLayout


class BulletinFactory private constructor(
    private val fragment: Fragment?,
    private val containerLayout: FrameLayout?
) {

    companion object {
        /**
         * Creates a [BulletinFactory] instance for a given [Fragment].
         * @param fragment The [Fragment] to create the factory for.
         * @return A new [BulletinFactory] instance.
         */
        fun of(fragment: Fragment) = BulletinFactory(fragment, null)

        /**
         * Creates a [BulletinFactory] instance for a given [FrameLayout].
         * @param containerLayout The [FrameLayout] to create the factory for.
         * @return A new [BulletinFactory] instance.
         */
        fun of(containerLayout: FrameLayout) = BulletinFactory(null, containerLayout)
    }

    /**
     * Creates a simple [Bulletin] with the specified drawable and text.
     * @param drawable The drawable resource for the bulletin's background.
     * @param text The text to display in the bulletin.
     * @return A new [Bulletin] instance.
     */
    fun createSimpleBulletin(@DrawableRes drawable: Int, text: CharSequence): Bulletin {
        val layout = Bulletin.SimpleLayout(getContext())
        layout.imageView.setBackgroundResource(drawable)
        layout.textView.text = text
        layout.textView.isSingleLine = false
        layout.textView.maxLines = 2
        return create(
            layout,
            if (text.length < 20) Bulletin.DURATION_SHORT else Bulletin.DURATION_LONG
        )
    }

    /**
     * Creates a simple [Bulletin] with the specified title and subtitle.
     * @param title The title text to display in the bulletin.
     * @param subtitle The subtitle text to display in the bulletin.
     * @return A new [Bulletin] instance.
     */
    fun createSimpleBulletin(title: CharSequence, subtitle: CharSequence): Bulletin {
        val layout = Bulletin.TwoLineLottieLayout(getContext()).apply {
            hideImage()
            titleTextView.text = title
            subtitleTextView.text = subtitle
        }
        return create(layout, Bulletin.DURATION_PROLONG)
    }

    /**
     * Creates a simple [Bulletin] with the specified title, subtitle, button, and button click action.
     * @param title The title text to display in the bulletin.
     * @param subtitle The subtitle text to display in the bulletin.
     * @param buttonText The text to display on the button.
     * @param onButtonClick The action to perform when the button is clicked.
     * @return A new [Bulletin] instance.
     */
    fun createSimpleBulletin(
        title: CharSequence,
        subtitle: CharSequence,
        buttonText: String,
        onButtonClick: Runnable
    ): Bulletin {
        val layout = Bulletin.TwoLineLottieLayout(getContext()).apply {
            hideImage()
            titleTextView.text = title
            subtitleTextView.text = subtitle
            button = Bulletin.UndoButton(context, true).apply {
                setText(buttonText)
                setUndoAction(onButtonClick)
            }
        }
        return create(layout, Bulletin.DURATION_PROLONG)
    }

    fun createErrorBulletin(
        errorMessage: CharSequence
    ): Bulletin {
        val layout = LottieLayout(getContext())
        layout.setAnimation(R.raw.error_lottie_anim)
        layout.textView.text = errorMessage
        layout.textView.isSingleLine = false
        layout.textView.maxLines = 2

        layout.imageView.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER
        ) {
            PorterDuffColorFilter(
                0xFF000000.toInt(),
                PorterDuff.Mode.SRC_ATOP
            )
        }

        return create(layout, Bulletin.DURATION_SHORT)
    }

    private fun create(layout: Bulletin.Layout, duration: Int): Bulletin {
        return if (fragment != null) {
            Bulletin.make(fragment, layout, duration)
        } else {
            Bulletin.make(containerLayout!!, layout, duration)
        }
    }

    private fun getContext(): Context {
        val context = fragment?.context ?: containerLayout?.context
        return context ?: MyApplication.applicationContext!!
    }
}
