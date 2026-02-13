package com.renderson.cashflowapp.data.session

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.renderson.cashflowapp.data.preferences.SessionDataStore
import com.renderson.cashflowapp.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val INACTIVITY_TIMEOUT_MS = 15 * 60 * 1000L

@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionDataStore: SessionDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                scope.launch {
                    sessionDataStore.setLastActiveTimestamp(System.currentTimeMillis())
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                scope.launch {
                    if (authRepository.getCurrentUserId() == null) return@launch
                    val lastActive = sessionDataStore.getLastActiveTimestamp() ?: return@launch
                    val elapsed = System.currentTimeMillis() - lastActive
                    if (elapsed >= INACTIVITY_TIMEOUT_MS) {
                        authRepository.signOut()
                    }
                }
            }
        })
    }
}
