package uk.ac.tees.mad.photowhisper.domain.usecase

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.repository.MemoryRepository

class GetMemoriesUseCase(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(userId: String): Flow<List<Memory>> {
        return memoryRepository.getAllMemories(userId)
    }
}