package ru.driics.playm8.presentation.home.request

import androidx.lifecycle.ViewModel

class RequestViewModel : ViewModel() {
    private val csgoSource = arrayOf(
        RequestRVAdapter.SourceInfo(
            sourceName = "CS:GO ПОИСК КОМАНДЫ | ИЩУ ИГРОКА",
            link = "https://vk.com/tfcs2"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "Поиск Игроков / Команды | FACEIT CS:GO",
            link = "https://vk.com/faceitsearch"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "Ищу команду CS GO",
            link = "https://vk.com/teamaddme"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "Ищу команду, поиск тимы |DOTA 2|CS:GO|RUST|PUBG|",
            link = "https://vk.com/stf_gaming"
        )
    )

    private val fortniteSource = arrayOf(
        RequestRVAdapter.SourceInfo(
            sourceName = "Fortnite Поиск тиммейтов игроков",
            link = "https://vk.com/tfcs2"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "Тимейты фортнайт / Fortnite / Поиск тимейтов",
            link = "https://vk.com/nice_team_fortnite"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "Поиск Напарников / Команды | Fortnite | Фортнайт",
            link = "https://vk.com/fortnite.search"
        )
    )

    private val mobilelegendsSource = arrayOf(
        RequestRVAdapter.SourceInfo(
            sourceName = "Mobile Legends - Поиск команды / тимы / игроков",
            link = "https://vk.com/poiskml"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "Поиск команды | Mobile Legends",
            link = "https://vk.com/teammlbb"
        ),
        RequestRVAdapter.SourceInfo(
            sourceName = "REVOLUTION |Поиск команды Mobile Legends",
            link = "https://vk.com/public179373183"
        )
    )

    fun getPublicsList(gameName: String): Array<RequestRVAdapter.SourceInfo> = when (gameName) {
        "CS:GO" -> csgoSource
        "Fortnite" -> fortniteSource
        "Mobile Legends" -> mobilelegendsSource
        else -> csgoSource
    }
}