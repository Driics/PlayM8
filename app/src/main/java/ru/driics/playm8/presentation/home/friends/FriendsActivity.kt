package ru.driics.playm8.presentation.home.friends

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.ViewUtils.viewBinding
import ru.driics.playm8.databinding.ActivityFriendsBinding
import ru.driics.playm8.databinding.ActivityRequestBinding

class FriendsActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityFriendsBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.drawerFriends)

        binding.textEmptyList.text = getString(R.string.empty_friends_list)
    }
}