package uk.ac.tees.mad.photowhisper.presentation.capture

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.data.local.FileManager
import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.usecase.GetCurrentUserUseCase
import uk.ac.tees.mad.photowhisper.domain.usecase.SaveMemoryUseCase
import java.util.UUID

class CaptureViewModel(
    private val saveMemoryUseCase: SaveMemoryUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val fileManager: FileManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val photoPath = fileManager.savePhoto(bitmap)
                val thumbnailPath = fileManager.saveThumbnail(photoPath)

                _uiState.value = _uiState.value.copy(
                    capturedPhotoPath = photoPath,
                    thumbnailPath = thumbnailPath,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save photo: ${e.message}"
                )
            }
        }
    }

    fun onAudioRecorded(audioPath: String, duration: Long) {
        viewModelScope.launch {
            try {
                val savedAudioPath = fileManager.saveAudio(audioPath)

                _uiState.value = _uiState.value.copy(
                    audioPath = savedAudioPath,
                    audioDuration = duration,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save audio: ${e.message}"
                )
            }
        }
    }

    fun onSaveMemory(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        if (currentState.capturedPhotoPath == null) {
            _uiState.value = currentState.copy(errorMessage = "Please capture a photo")
            return
        }

        if (currentState.audioPath == null) {
            _uiState.value = currentState.copy(errorMessage = "Please record audio")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSaving = true)

            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    errorMessage = "User not found"
                )
                return@launch
            }

            val memory = Memory(
                id = UUID.randomUUID().toString(),
                userId = currentUser.id,
                photoUrl = "",
                audioUrl = "",
                thumbnailUrl = currentState.thumbnailPath,
                dateCaptured = System.currentTimeMillis(),
                location = null,
                cameraInfo = null,
                isSynced = false,
                localPhotoPath = currentState.capturedPhotoPath,
                localAudioPath = currentState.audioPath
            )

            val result = saveMemoryUseCase(memory)

            result.fold(
                onSuccess = {
                    _uiState.value = currentState.copy(isSaving = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        errorMessage = "Failed to save memory: ${exception.message}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class CaptureUiState(
    val capturedPhotoPath: String? = null,
    val thumbnailPath: String? = null,
    val audioPath: String? = null,
    val audioDuration: Long = 0,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)