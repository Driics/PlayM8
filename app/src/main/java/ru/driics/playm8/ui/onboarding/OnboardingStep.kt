package ru.driics.playm8.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.driics.playm8.R
import ru.driics.playm8.databinding.FragmentOnboardingStepBinding

class OnboardingStep : Fragment(R.layout.fragment_onboarding_step) {

    private lateinit var binding: FragmentOnboardingStepBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingStepBinding.bind(view)

        val titleRes = arguments?.getInt(TITLE)!!
        val descriptionRes = arguments?.getInt(DESC)!!
        with(binding) {

            title.setText(titleRes)
            description.setText(descriptionRes)
        }
    }

    companion object {
        const val TITLE = "title"
        const val DESC = "desc"
    }
}