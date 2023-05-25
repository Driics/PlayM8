package ru.driics.playm8.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import ru.driics.playm8.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repo: AuthRepository
) : ViewModel() {

    val user: FirebaseUser? =
        repo.currentUser
}