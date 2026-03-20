package uk.ac.tees.mad.photowhisper.domain.usecase

import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.repository.MemoryRepository

class SaveMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: Memory): Result<Unit> {
        return try {
            memoryRepository.saveMemory(memory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}