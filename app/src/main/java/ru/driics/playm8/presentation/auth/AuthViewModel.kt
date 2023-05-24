package ru.driics.playm8.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.driics.playm8.domain.model.AuthOperation
import ru.driics.playm8.domain.model.Response
import ru.driics.playm8.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _authResponse = MutableStateFlow<Response<Boolean>>(Response.Empty)
    val authResponse: StateFlow<Response<Boolean>> = _authResponse.asStateFlow()

    private val _isNotNewUser = MutableStateFlow(false)
    val loginUser: StateFlow<Boolean> = _isNotNewUser.asStateFlow()

    fun performAuthOperation(
        email: String,
        password: String,
        operation: AuthOperation
    ) = viewModelScope.launch {
        _authResponse.value = Response.Loading

        try {
            _authResponse.value = when (operation) {
                AuthOperation.SIGN_IN -> repo.firebaseSignInWithEmailAndPassword(email, password)
                AuthOperation.SIGN_UP -> repo.firebaseSignUpWithEmailAndPassword(email, password)
            }
        } catch (e: Exception) {
            _authResponse.value = Response.Failure(e)
        }
    }

    fun changeUserState() {
        _isNotNewUser.value = _isNotNewUser.value.not()
    }
}