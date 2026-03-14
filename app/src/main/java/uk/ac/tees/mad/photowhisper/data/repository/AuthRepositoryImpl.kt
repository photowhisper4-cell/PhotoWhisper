package uk.ac.tees.mad.photowhisper.data.repository

import uk.ac.tees.mad.photowhisper.data.local.PreferencesManager
import uk.ac.tees.mad.photowhisper.data.remote.AuthService
import uk.ac.tees.mad.photowhisper.domain.model.User
import uk.ac.tees.mad.photowhisper.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override suspend fun signUp(email: String, password: String): Result<User> {
        val result = authService.signUp(email, password)
        if (result.isSuccess) {
            val user = result.getOrNull()
            user?.let {
                saveUserSession(it.id, it.email)
            }
        }
        return result
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        val result = authService.signIn(email, password)
        if (result.isSuccess) {
            val user = result.getOrNull()
            user?.let {
                saveUserSession(it.id, it.email)
            }
        }
        return result
    }

    override suspend fun getCurrentUser(): User? {
        return authService.getCurrentUser()
    }

    override suspend fun signOut(): Result<Unit> {
        val result = authService.signOut()
        if (result.isSuccess) {
            clearUserSession()
        }
        return result
    }

    override suspend fun saveUserSession(userId: String, email: String) {
        preferencesManager.saveUserSession(userId, email)
    }

    override suspend fun clearUserSession() {
        preferencesManager.clearUserSession()
    }
}