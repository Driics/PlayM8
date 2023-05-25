package ru.driics.playm8.presentation.main

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.ViewUtils.viewBinding
import ru.driics.playm8.databinding.ActivityMainBinding
import ru.driics.playm8.databinding.ChromeHomeBinding


@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val binding by viewBinding(ChromeHomeBinding::inflate)
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setContentView(binding.root)
    }

    /*private fun initDrawer() {
        binding.drawerChrome.toolbar.apply {
            setNavigationIcon(R.drawable.ic_hamburger)
            setNavigationOnClickListener {
                toggleDrawer()
            }
        }
    }

    private fun toggleDrawer() =
        if (binding.root.isDrawerOpen(GravityCompat.START))
            binding.root.closeDrawer(GravityCompat.START, true)
        else binding.root.openDrawer(
            GravityCompat.START,
            true
        )*/
}