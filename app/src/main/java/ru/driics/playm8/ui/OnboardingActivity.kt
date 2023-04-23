package ru.driics.playm8.ui

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ru.driics.playm8.R
import ru.driics.playm8.components.viewpager.indicator.ViewPageAdapter
import ru.driics.playm8.databinding.ActivityOnboardingBinding
import ru.driics.playm8.utils.AndroidUtils.setEndDrawable
import ru.driics.playm8.utils.ViewUtils.viewBinding

class OnboardingActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityOnboardingBinding::inflate)

    inner class Step(
        val action: () -> Unit = ::navigateNext,
        @StringRes val actionText: Int = R.string.app_name,
        @DrawableRes val actionDrawable: Int = R.drawable.ic_arrow_12,
        val createFragment: () -> Fragment
    )

    inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = steps.size
        override fun createFragment(position: Int): Fragment = steps[position].createFragment()
    }

    private val steps = mutableListOf<Step>()

    private fun initSteps() {
        val dottedSteps = listOf(
            bundleOf(
                OnboardingStep.TITLE to R.string.onboarding_welcome_title,
                OnboardingStep.DESC to R.string.onboarding_welcome_desc
            ),
            bundleOf(
                OnboardingStep.TITLE to R.string.app_name,
                OnboardingStep.DESC to R.string.next
            ),
        )

        dottedSteps.forEach {
            steps += Step {
                OnboardingStep().apply {
                    arguments = it
                }
            }
        }
    }

    private fun navigateNext() = with(binding.pager) {
        if (currentItem + 1 == adapter?.itemCount)
            finish()
        else setCurrentItem(currentItem + 1, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.hide()

        initSteps()

        with(binding) {
            pager.adapter = PagerAdapter()
            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val step = steps[position]
                    next.apply {
                        setOnClickListener { step.action() }
                        setEndDrawable(step.actionDrawable)
                        setText(step.actionText)
                    }
                }
            })
            shapePageIndicator.setCustomViewPagerAdapter(ViewPageAdapter(pager))
        }
    }
}