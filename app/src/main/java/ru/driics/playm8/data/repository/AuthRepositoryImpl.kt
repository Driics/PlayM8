package ru.driics.playm8.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import ru.driics.playm8.domain.model.Response
import ru.driics.playm8.domain.repository.AuthRepository
import ru.driics.playm8.domain.repository.AuthStateResponse
import ru.driics.playm8.domain.repository.ReloadUserResponse
import ru.driics.playm8.domain.repository.RevokeAccessResponse
import ru.driics.playm8.domain.repository.SendEmailVerificationResponse
import ru.driics.playm8.domain.repository.SendPasswordResetEmailResponse
import ru.driics.playm8.domain.repository.SignInResponse
import ru.driics.playm8.domain.repository.SignUpResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun firebaseSignUpWithEmailAndPassword(
        nickname: String,
        email: String,
        password: String
    ): SignUpResponse {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(nickname)
                .build()

            authResult.user?.updateProfile(userProfileChangeRequest)?.await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun sendEmailVerification(): SendEmailVerificationResponse {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun firebaseSignInWithEmailAndPassword(
        email: String,
        password: String
    ): SignInResponse {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun reloadFirebaseUser(): ReloadUserResponse {
        return try {
            auth.currentUser?.reload()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): SendPasswordResetEmailResponse {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override fun signOut() = auth.signOut()

    override suspend fun revokeAccess(): RevokeAccessResponse {
        return try {
            auth.currentUser?.delete()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override fun getAuthState(viewModelScope: CoroutineScope): AuthStateResponse =
        callbackFlow {
            val authStateListener = AuthStateListener { auth ->
                trySend(auth.currentUser == null)
            }

            auth.addAuthStateListener(authStateListener)

            awaitClose {
                auth.removeAuthStateListener(authStateListener)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), auth.currentUser == null)

}