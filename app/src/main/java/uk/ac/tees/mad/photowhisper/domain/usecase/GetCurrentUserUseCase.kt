package uk.ac.tees.mad.photowhisper.domain.usecase

import uk.ac.tees.mad.photowhisper.domain.model.User
import uk.ac.tees.mad.photowhisper.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}