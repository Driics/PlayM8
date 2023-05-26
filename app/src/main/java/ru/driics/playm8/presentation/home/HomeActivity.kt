package ru.driics.playm8.presentation.home

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.ktx.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.AndroidUtils.launchActivity
import ru.driics.playm8.core.utils.AndroidUtils.launchFragment
import ru.driics.playm8.core.utils.ViewUtils.onClick
import ru.driics.playm8.core.utils.ViewUtils.viewBinding
import ru.driics.playm8.databinding.AccountViewBinding
import ru.driics.playm8.databinding.ActivityHomeBinding
import ru.driics.playm8.presentation.home.account.AccountFragment
import ru.driics.playm8.presentation.onboarding.OnboardingActivity
import java.util.Locale

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    val binding: ActivityHomeBinding by viewBinding(ActivityHomeBinding::inflate)
    val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.chromeHome.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        setContentView(binding.root)
        initDrawer()
        initDrawerView()

        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        binding.chromeHome.bottomNav.setupWithNavController(navController)

        supportFragmentManager.addOnBackStackChangedListener {
            updateNavigationIcon()
        }
    }

    private fun initDrawer() {
        with(binding.chromeHome.toolbar) {
            setNavigationIcon(R.drawable.ic_hamburger)
            setNavigationOnClickListener { toggleDrawer() }
        }
    }

    private fun updateNavigationIcon() {
        val icon = if (supportFragmentManager.backStackEntryCount > 0) {
            R.drawable.round_arrow_back_24
        } else {
            R.drawable.ic_hamburger
        }

        binding.chromeHome.toolbar.setNavigationIcon(icon)
    }

    private fun initDrawerView() {
        with(binding.drawerHome) {
            textVersion.text = getString(R.string.appVersion, BuildConfig.VERSION_NAME)
            viewModel.user?.let {
                val accountViewBinding = AccountViewBinding.bind(navigationDrawer.getHeaderView(0))
                with(accountViewBinding) {
                    textUserName.text = it.displayName
                    textUserEmail.text = it.email
                    textUserInitials.text =
                        (it.displayName ?: it.email)?.let { username ->
                            getInitials(
                                username
                            )
                        }

                    this.root.onClick {
                        launchFragment<AccountFragment>(R.id.fragmentContainer)
                        toggleDrawer()
                    }

                    logOut.onClick {
                        viewModel.signOut()
                        launchActivity<OnboardingActivity>()
                        finish()
                    }
                }
            }
        }
    }

    private fun toggleDrawer() = with(binding.root) {
        if (isDrawerOpen(GravityCompat.START)) {
            closeDrawer(GravityCompat.START, true)
        } else {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                openDrawer(GravityCompat.START, true)
            }
        }
    }

    private fun getInitials(username: String): String =
        username.substring(0, 1.coerceAtMost(username.length)).uppercase(Locale.ROOT)
}