package ru.driics.playm8.presentation.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ru.driics.playm8.domain.repository.AuthRepository
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    repo: AuthRepository
) : ViewModel() {

    val user: FirebaseUser? = repo.currentUser
}