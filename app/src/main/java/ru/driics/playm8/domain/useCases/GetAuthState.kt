package ru.driics.playm8.domain.useCases

import kotlinx.coroutines.CoroutineScope
import ru.driics.playm8.domain.repository.AuthRepository
import ru.driics.playm8.domain.repository.AuthStateResponse

class GetAuthState(
    private val repo: AuthRepository
) {
    operator fun invoke(
        viewModelScope: CoroutineScope
    ): AuthStateResponse = repo.getAuthState(viewModelScope)
}