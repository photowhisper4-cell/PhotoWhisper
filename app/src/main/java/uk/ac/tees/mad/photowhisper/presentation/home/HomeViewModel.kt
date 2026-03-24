package uk.ac.tees.mad.photowhisper.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.repository.MemoryRepository
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.GetMemoriesUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.LogoutUseCase

class HomeViewModel(
    private val getMemoriesUseCase: GetMemoriesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        syncAndLoadMemories()
    }

    private fun syncAndLoadMemories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser != null) {
                    currentUserId = currentUser.id

                    try {
                        memoryRepository.syncMemories(currentUser.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    getMemoriesUseCase(currentUser.id).collect { memories ->
                        _uiState.value = _uiState.value.copy(
                            memories = memories,
                            isLoading = false,
                            userEmail = currentUser.email,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load memories: ${e.message}"
                )
            }
        }
    }

    fun onLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = logoutUseCase()
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    fun refreshMemories() {
        if (currentUserId != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                try {
                    memoryRepository.syncMemories(currentUserId!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        } else {
            syncAndLoadMemories()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class HomeUiState(
    val memories: List<Memory> = emptyList(),
    val isLoading: Boolean = false,
    val userEmail: String = "",
    val errorMessage: String? = null
)