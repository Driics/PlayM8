package ru.driics.playm8.presentation.home.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.viewBinding
import ru.driics.playm8.databinding.FragmentDashboardBinding
import ru.driics.playm8.presentation.home.HomeViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    val binding: FragmentDashboardBinding by viewBinding(FragmentDashboardBinding::bind)
    val viewModel: HomeViewModel by viewModels({ requireActivity() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.welcomeText.text =
            getString(R.string.dashboardHeaderText, viewModel.user?.displayName)

        binding.recyclerView.adapter = DashboardRecyclerAdapter(
            arrayOf(
                DashboardRecyclerAdapter.GameModel(
                    R.drawable.fortnite,
                    "Fortnite",
                    0xFF350d76.toInt()
                ),
                DashboardRecyclerAdapter.GameModel(R.drawable.csgo, "CS:GO", 0xFF5d79ae.toInt()),
                DashboardRecyclerAdapter.GameModel(
                    R.drawable.mobilelegends,
                    "Mobile Legends",
                    0xFFcaca22.toInt()
                )
            ), requireContext()
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}