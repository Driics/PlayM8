package ru.driics.playm8.presentation.home.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import ru.driics.playm8.R
import ru.driics.playm8.databinding.DashboardScreenRecyclerviewItemBinding

class DashboardRecyclerAdapter(
    private val dataSet: Array<GameModel>
) : RecyclerView.Adapter<DashboardRecyclerAdapter.ViewHolder>() {
    data class GameModel(
        @DrawableRes val gameIconRes: Int,
        val gameName: String,
        @ColorInt val backgroundColor: Int
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: DashboardScreenRecyclerviewItemBinding

        init {
            binding = DashboardScreenRecyclerviewItemBinding.bind(view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_screen_recyclerview_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        image.setImageResource(dataSet[position].gameIconRes)
        root.setCardBackgroundColor(dataSet[position].backgroundColor)
    }
}