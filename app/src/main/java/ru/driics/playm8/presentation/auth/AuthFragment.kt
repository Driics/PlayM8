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
import ru.driics.playm8.databinding.FragmentOnboardingRegisterBinding
import ru.driics.playm8.domain.model.AuthOperation
import ru.driics.playm8.domain.model.Response

class AuthFragment : Fragment(R.layout.fragment_onboarding_register) {
    private lateinit var binding: FragmentOnboardingRegisterBinding
    private val viewModel: AuthViewModel by viewModels({ requireActivity() })


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingRegisterBinding.bind(view)

        with(binding) {
            loginText.apply {
                text = createUnderlinedText(getString(R.string.register_user_text))
                setOnClickListener { viewModel.changeUserState() }
            }

            authUserBtn.setOnClickListener {
                val email = binding.email.editText?.text.toString()
                val password = binding.password.editText?.text.toString()
                val operation =
                    if (viewModel.loginUser.value) AuthOperation.SIGN_IN else AuthOperation.SIGN_UP

                viewModel.performAuthOperation(email, password, operation)
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

                        is Response.Success -> Snackbar.make(
                            binding.root,
                            "Success ${it.data} \n Login: ${viewModel.loginUser.value}",
                            Snackbar.LENGTH_SHORT
                        ).show()

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
                    if (it) {
                        binding.authUserBtn.text = getString(R.string.login_user)
                        binding.titleText.text = getString(R.string.login_user)
                        binding.loginText.text =
                            createUnderlinedText(getString(R.string.login_user_text))
                    } else {
                        binding.authUserBtn.text = getString(R.string.register_user)
                        binding.titleText.text = getString(R.string.register_user)
                        binding.loginText.text =
                            createUnderlinedText(getString(R.string.login_user_text))
                    }
                }
            }
        }
    }

    private fun createUnderlinedText(str: String): CharSequence = SpannableString(str).apply {
        setSpan(UnderlineSpan(), 0, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}