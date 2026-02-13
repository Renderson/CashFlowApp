package com.renderson.cashflowapp.data.cloud

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.renderson.cashflowapp.data.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val BACKUP_PATH = "users/%s/backup.json"

@Singleton
class CloudBackupRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository
) {

    suspend fun uploadBackup(userId: String, jsonBytes: ByteArray): Result<Unit> {
        return try {
            val ref = storage.reference.child(BACKUP_PATH.format(userId))
            ref.putBytes(jsonBytes).await()
            Result.success(Unit)
        } catch (e: StorageException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(userId: String): Result<ByteArray> {
        return try {
            val ref = storage.reference.child(BACKUP_PATH.format(userId))
            val bytes = ref.getBytes(Long.MAX_VALUE).await()
            Result.success(bytes)
        } catch (e: StorageException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
