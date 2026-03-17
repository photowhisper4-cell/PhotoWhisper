package uk.ac.tees.mad.photowhisper.data.remote



import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import uk.ac.tees.mad.photowhisper.domain.model.User

class AuthService(
    private val supabaseClient: SupabaseClient
) {
    suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            supabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUser = supabaseClient.client.auth.currentUserOrNull()
            if (currentUser != null) {
                Result.success(
                    User(
                        id = currentUser.id,
                        email = currentUser.email ?: ""
                    )
                )
            } else {
                Result.failure(Exception("Failed to retrieve user after sign up"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            supabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUser = supabaseClient.client.auth.currentUserOrNull()
            if (currentUser != null) {
                Result.success(
                    User(
                        id = currentUser.id,
                        email = currentUser.email ?: ""
                    )
                )
            } else {
                Result.failure(Exception("Failed to retrieve user after sign in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val currentUser = supabaseClient.client.auth.currentUserOrNull()
            currentUser?.let {
                User(
                    id = it.id,
                    email = it.email ?: ""
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}