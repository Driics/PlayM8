package ru.driics.playm8.presentation.home.notifications

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.ViewUtils.viewBinding
import ru.driics.playm8.databinding.ActivityFriendsBinding

class NotificationsActivity : AppCompatActivity() {
    val binding by viewBinding(ActivityFriendsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.drawerNotifications)
        }

        binding.textEmptyList.text = getString(R.string.empty_notifications_list)
    }
}