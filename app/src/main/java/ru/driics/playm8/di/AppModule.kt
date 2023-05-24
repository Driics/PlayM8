package ru.driics.playm8.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import ru.driics.playm8.data.repository.AuthRepositoryImpl
import ru.driics.playm8.domain.repository.AuthRepository

@Module
@InstallIn(ViewModelComponent::class)
class AppModule {

    @Provides
    fun provideAuthRepository(): AuthRepository = AuthRepositoryImpl(Firebase.auth)
}