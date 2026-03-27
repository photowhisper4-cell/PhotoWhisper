package uk.ac.tees.mad.photowhisper.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.data.local.FileManager
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.LogoutUseCase

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val fileManager: FileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    userEmail = user.email,
                    isLoading = false
                )
            }
        }
    }

    fun onClearCache() {
        viewModelScope.launch {
            try {
                fileManager.clearCache()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Cache cleared successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear cache: ${e.message}"
                )
            }
        }
    }

    fun onLogout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = logoutUseCase()
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Logout failed"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

data class ProfileUiState(
    val userEmail: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)