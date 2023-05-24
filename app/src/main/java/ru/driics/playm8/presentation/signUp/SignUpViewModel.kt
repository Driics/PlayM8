package ru.driics.playm8.presentation.signUp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.driics.playm8.domain.model.Response
import ru.driics.playm8.domain.repository.AuthRepository
import ru.driics.playm8.domain.repository.SignInResponse
import ru.driics.playm8.domain.repository.SignUpResponse
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _signUpResponse = MutableStateFlow<SignUpResponse>(Response.Empty)
    val signUpResponse: StateFlow<SignUpResponse> = _signUpResponse

    private val _signInResponse = MutableStateFlow<SignInResponse>(Response.Empty)
    val signInResponse: StateFlow<SignInResponse> = _signUpResponse

    val isNotNewUser: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun signUpWithEmailAndPassword(email: String, password: String) = viewModelScope.launch {
        _signUpResponse.value = Response.Loading
        _signUpResponse.value = repo.firebaseSignUpWithEmailAndPassword(email, password)
    }

    fun signInWithEmailAndPassword(email: String, password: String) = viewModelScope.launch {
        _signInResponse.value = Response.Loading
        _signInResponse.value = repo.firebaseSignInWithEmailAndPassword(email, password)
    }

    fun changeUserState() = isNotNewUser.value?.let {
        isNotNewUser.value = !it
    }

    /*fun sendEmailVerification() = viewModelScope.launch {
        sendEmailVerificationResponse = Response.Loading
        sendEmailVerificationResponse = repo.sendEmailVerification()
    }*/

}