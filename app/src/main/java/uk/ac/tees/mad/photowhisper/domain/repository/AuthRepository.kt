package uk.ac.tees.mad.photowhisper.domain.repository

import uk.ac.tees.mad.photowhisper.domain.model.User

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun signOut(): Result<Unit>
    suspend fun saveUserSession(userId: String, email: String)
    suspend fun clearUserSession()
}