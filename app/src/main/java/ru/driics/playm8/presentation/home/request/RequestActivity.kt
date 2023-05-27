package ru.driics.playm8.presentation.home.request

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.driics.playm8.core.utils.ViewUtils.viewBinding
import ru.driics.playm8.databinding.ActivityRequestBinding
import ru.driics.playm8.presentation.home.dashboard.DashboardRecyclerAdapter

@AndroidEntryPoint
class RequestActivity : AppCompatActivity() {

    private val viewModel: RequestViewModel by viewModels()
    private val binding by viewBinding(ActivityRequestBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding) {
            val gameName =
                intent.extras?.getString(DashboardRecyclerAdapter.GAME_NAME_TAG, "CS:GO")
            val list = viewModel.getPublicsList(gameName!!)
            supportActionBar?.title = gameName

            recyclerView.adapter = RequestRVAdapter(list)
            recyclerView.layoutManager = LinearLayoutManager(this@RequestActivity)
        }
    }
}