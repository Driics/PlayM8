package ru.driics.playm8.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.driics.playm8.domain.model.Response
import ru.driics.playm8.domain.repository.SignInResponse
import ru.driics.playm8.domain.repository.SignOutResponse
import ru.driics.playm8.domain.useCases.UseCases
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {
    init {
        getAuthState()
    }

    val user: FirebaseUser?
        get() = Firebase.auth.currentUser

    var signInResponse by mutableStateOf<SignInResponse>(Response.Success(false))
        private set
    var signOutResponse by mutableStateOf<SignOutResponse>(Response.Success(false))
        private set

    fun getAuthState() = useCases.getAuthState(viewModelScope)

    fun signIn(email: String, password: String) = viewModelScope.launch {
        signInResponse = Response.Loading
        signInResponse = useCases.signIn(email, password)
    }

    fun signOut() = viewModelScope.launch {
        signOutResponse = Response.Loading
        signOutResponse = useCases.signOut()
    }
}