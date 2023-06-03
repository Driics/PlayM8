package ru.driics.playm8.presentation.home.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.AndroidUtils.launchActivity
import ru.driics.playm8.core.utils.ViewUtils.onClick
import ru.driics.playm8.databinding.DashboardScreenRecyclerviewItemBinding
import ru.driics.playm8.presentation.home.request.RequestActivity

class DashboardRecyclerAdapter(
    private val dataSet: Array<GameModel>,
    private val context: Context
) : RecyclerView.Adapter<DashboardRecyclerAdapter.ViewHolder>() {
    companion object {
        const val GAME_NAME_TAG = "game_name"
        const val GAME_ID_TAG = "game_id"
    }

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
        val game = dataSet[position]

        image.setImageResource(game.gameIconRes)
        root.setCardBackgroundColor(game.backgroundColor)


        root.onClick {
            context.launchActivity<RequestActivity> {
                putExtra(GAME_NAME_TAG, game.gameName)
            }
        }
    }
}