package uk.ac.tees.mad.photowhisper.data.remote

import io.github.jan.supabase.postgrest.from
import uk.ac.tees.mad.photowhisper.data.remote.dto.MemoryDto

class DatabaseService(
    private val supabaseClient: SupabaseClient
) {
    private companion object {
        const val MEMORIES_TABLE = "memories"
    }

    suspend fun insertMemory(memoryDto: MemoryDto): Result<Unit> {
        return try {
            supabaseClient.client.from(MEMORIES_TABLE).insert(memoryDto)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateMemory(memoryDto: MemoryDto): Result<Unit> {
        return try {
            supabaseClient.client.from(MEMORIES_TABLE)
                .update(memoryDto) {
                    filter {
                        eq("id", memoryDto.id)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMemoriesForUser(userId: String): Result<List<MemoryDto>> {
        return try {
            val memories = supabaseClient.client.from(MEMORIES_TABLE)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<MemoryDto>()

            Result.success(memories)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMemoryById(memoryId: String): Result<MemoryDto?> {
        return try {
            val memory = supabaseClient.client.from(MEMORIES_TABLE)
                .select {
                    filter {
                        eq("id", memoryId)
                    }
                }
                .decodeSingleOrNull<MemoryDto>()

            Result.success(memory)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteMemory(memoryId: String): Result<Unit> {
        return try {
            supabaseClient.client.from(MEMORIES_TABLE)
                .delete {
                    filter {
                        eq("id", memoryId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}