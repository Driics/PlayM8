package ru.driics.playm8.presentation.home.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.BuildConfig
import kotlinx.coroutines.launch
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.viewBinding
import ru.driics.playm8.databinding.FragmentAccountBinding

class AccountFragment : Fragment(R.layout.fragment_account) {
    private val binding: FragmentAccountBinding by viewBinding(FragmentAccountBinding::bind)
    private val viewModel: AccountFragmentViewModel by viewModels({ requireActivity() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            with(binding) {
                viewModel.user?.let {
                    textUser.text = it.displayName
                    textVersion.text = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME
                }
            }
        }
    }
}