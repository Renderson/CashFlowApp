package com.renderson.cashflowapp.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        trySend(firebaseAuth.currentUser)
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(it) }
    )

    suspend fun createAccount(name: String, email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = firebaseAuth.currentUser ?: throw IllegalStateException("User not found after sign up")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        user.updateProfile(profileUpdates).await()
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(it) }
    )

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(
            IllegalStateException("Nenhum usuário logado")
        )
        val email = user.email ?: return Result.failure(
            IllegalStateException("Email não disponível")
        )
        return runCatching {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            user.delete().await()
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }
}
