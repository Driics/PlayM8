package ru.driics.playm8.domain.useCases

import ru.driics.playm8.domain.repository.AuthRepository

class SignIn(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ) = repo.firebaseSingIn(email, password)
}