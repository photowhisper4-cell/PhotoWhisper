package uk.ac.tees.mad.photowhisper.domain.usecase

import uk.ac.tees.mad.photowhisper.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}