package ru.driics.playm8.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.driics.playm8.data.repository.AuthRepositoryImpl
import ru.driics.playm8.domain.repository.AuthRepository
import ru.driics.playm8.domain.useCases.GetAuthState
import ru.driics.playm8.domain.useCases.SignIn
import ru.driics.playm8.domain.useCases.SignOut
import ru.driics.playm8.domain.useCases.UseCases

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    fun provideAuthRepository(
        auth: FirebaseAuth
    ): AuthRepository = AuthRepositoryImpl(auth)

    @Provides
    fun provideUseCases(
        repository: AuthRepository
    ) = UseCases(
        getAuthState = GetAuthState(repository),
        signIn = SignIn(repository),
        signOut = SignOut(repository)
    )
}