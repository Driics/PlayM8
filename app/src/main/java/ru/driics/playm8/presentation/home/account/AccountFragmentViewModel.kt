package ru.driics.playm8.presentation.home.account

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.driics.playm8.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AccountFragmentViewModel @Inject constructor(
    repo: AuthRepository
): ViewModel() {

    val user: FirebaseUser? =
        repo.currentUser
}