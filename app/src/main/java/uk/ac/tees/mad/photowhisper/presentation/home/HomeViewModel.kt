package uk.ac.tees.mad.photowhisper.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.GetMemoriesUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.LogoutUseCase

class HomeViewModel(
    private val getMemoriesUseCase: GetMemoriesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMemories()
    }

    private fun loadMemories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                getMemoriesUseCase(currentUser.id).collect { memories ->
                    _uiState.value = _uiState.value.copy(
                        memories = memories,
                        isLoading = false,
                        userEmail = currentUser.email
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
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
}

data class HomeUiState(
    val memories: List<Memory> = emptyList(),
    val isLoading: Boolean = false,
    val userEmail: String = ""
)