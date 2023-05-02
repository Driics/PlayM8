package ru.driics.playm8.domain.useCases

data class UseCases (
    val getAuthState: GetAuthState,
    val signIn: SignIn,
    val signOut: SignOut
)