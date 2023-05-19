package ru.driics.playm8.presentation.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import ru.driics.playm8.R
import ru.driics.playm8.components.bulletin.BulletinFactory
import ru.driics.playm8.components.viewpager.indicator.ViewPageAdapter
import ru.driics.playm8.core.utils.AndroidUtils.setEndDrawable
import ru.driics.playm8.core.utils.ViewUtils.viewBinding
import ru.driics.playm8.databinding.ActivityOnboardingBinding
import ru.driics.playm8.databinding.FragmentOnboardingRegisterBinding
import ru.driics.playm8.domain.model.Response
import ru.driics.playm8.presentation.auth.AuthViewModel


class OnboardingRegisterFragment : Fragment(R.layout.fragment_onboarding_register) {
    private lateinit var binding: FragmentOnboardingRegisterBinding
    private val viewModel: AuthViewModel by viewModels({ requireActivity() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingRegisterBinding.bind(view)
    }
}


@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityOnboardingBinding::inflate)
    private val viewModel: AuthViewModel by viewModels()

    inner class Step(
        val action: () -> Unit = ::navigateNext,
        @StringRes val actionText: Int = R.string.next,
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

        steps += Step(
            action = {
                navigateNext()
            }
        ) {
            OnboardingRegisterFragment()
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
                    super.onPageSelected(position)
                    updateViewPagerHeight(BottomSheetBehavior.from(frame), pager, position)

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

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    private fun updateViewPagerHeight(
        behavior: BottomSheetBehavior<FrameLayout>,
        viewPager: ViewPager2,
        position: Int
    ) {
        val currentFragment = supportFragmentManager.findFragmentByTag("f$position")

        viewPager.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val desiredHeight = currentFragment?.view?.measuredHeight ?: viewPager.height
                behavior.state = if (desiredHeight > viewPager.height)
                    BottomSheetBehavior.STATE_EXPANDED
                else BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        })
    }
}