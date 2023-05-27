package ru.driics.playm8.presentation.home.request

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.driics.playm8.R
import ru.driics.playm8.core.utils.ViewUtils.onClick
import ru.driics.playm8.databinding.ItemRecyclerviewGameRequestBinding

class RequestRVAdapter(
    private val dataSet: Array<SourceInfo>
) : RecyclerView.Adapter<RequestRVAdapter.ViewHolder>() {
    data class SourceInfo(
        val platformSource: PlatformSource = PlatformSource.VK,
        val link: String,
        val sourceName: String
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: ItemRecyclerviewGameRequestBinding

        init {
            binding = ItemRecyclerviewGameRequestBinding.bind(view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recyclerview_game_request, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        val sourceInfo = dataSet[position]

        val sourceIcon = when (sourceInfo.platformSource) {
            PlatformSource.VK -> R.drawable.vk_logo
            PlatformSource.TELEGRAM -> R.drawable.telegram_logo
        }
        sourceLogo.setImageResource(sourceIcon)

        textSourceName.text = sourceInfo.sourceName
        textPublicLink.text = sourceInfo.link

        buttonOpenLink.onClick {
            holder.itemView.context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("vkontakte://profile/1")
                )
            )
        }
    }
}