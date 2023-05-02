package ru.driics.playm8.domain.useCases

import ru.driics.playm8.domain.repository.AuthRepository

class SignOut(
    private val repo: AuthRepository
) {
    suspend operator fun invoke() = repo.firebaseSignOut()
}