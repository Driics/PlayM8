package ru.driics.playm8.presentation.auth

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.AndroidUtils.launchActivity
import ru.driics.playm8.core.utils.ViewUtils.onClick
import ru.driics.playm8.databinding.FragmentOnboardingRegisterBinding
import ru.driics.playm8.domain.model.AuthOperation
import ru.driics.playm8.domain.model.Response
import ru.driics.playm8.presentation.home.HomeActivity

class AuthFragment : Fragment(R.layout.fragment_onboarding_register) {
    private lateinit var binding: FragmentOnboardingRegisterBinding
    private val viewModel: AuthViewModel by viewModels({ requireActivity() })


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingRegisterBinding.bind(view)

        with(binding) {
            loginText.apply {
                text = createUnderlinedText(getString(R.string.register_user_text))
                onClick { viewModel.changeUserState() }
            }

            authUserBtn.setOnClickListener {
                val nickname = binding.nickname.editText?.text.toString()
                val email = binding.email.editText?.text.toString()
                val password = binding.password.editText?.text.toString()
                val operation =
                    if (viewModel.loginUser.value) AuthOperation.SIGN_IN else AuthOperation.SIGN_UP

                viewModel.performAuthOperation(nickname, email, password, operation)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.authResponse.collect {
                    when (it) {
                        is Response.Loading -> Snackbar.make(
                            binding.root,
                            "Loading..",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        is Response.Success -> if (it.data) {
                            requireContext().launchActivity<HomeActivity>()
                            requireActivity().finish()
                        }

                        is Response.Failure -> it.apply {
                            Snackbar.make(
                                binding.root,
                                e.message!!,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.loginUser.collect {
                    with(binding) {
                        if (it) {
                            authUserBtn.text = getString(R.string.login_user)
                            titleText.text = getString(R.string.login_user)
                            nickname.visibility = View.GONE
                            loginText.text =
                                createUnderlinedText(getString(R.string.register_user_text))
                        } else {
                            authUserBtn.text = getString(R.string.register_user)
                            titleText.text = getString(R.string.register_user)
                            nickname.visibility = View.VISIBLE
                            loginText.text =
                                createUnderlinedText(getString(R.string.login_user_text))
                        }
                    }
                }
            }
        }
    }

    private fun createUnderlinedText(str: String): CharSequence = SpannableString(str).apply {
        setSpan(UnderlineSpan(), 0, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}