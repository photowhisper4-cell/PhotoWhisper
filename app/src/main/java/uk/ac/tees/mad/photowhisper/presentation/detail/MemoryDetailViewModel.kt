package uk.ac.tees.mad.photowhisper.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.photowhisper.data.local.AudioPlayer
import uk.ac.tees.mad.photowhisper.domain.model.Memory
import uk.ac.tees.mad.photowhisper.domain.repository.MemoryRepository

class MemoryDetailViewModel(
    private val memoryRepository: MemoryRepository,
    private val audioPlayer: AudioPlayer,
    private val memoryId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryDetailUiState())
    val uiState: StateFlow<MemoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadMemory()
    }


    private fun loadMemory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val memory = memoryRepository.getMemoryById(memoryId)
                if (memory != null) {
                    _uiState.value = _uiState.value.copy(
                        memory = memory,
                        isLoading = false,
                        audioDuration = getAudioDuration(memory.localAudioPath)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Memory not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load memory: ${e.message}"
                )
            }
        }
    }

    private fun getAudioDuration(audioPath: String?): Int {
        return try {
            if (audioPath != null) {
                val player = android.media.MediaPlayer().apply {
                    setDataSource(audioPath)
                    prepare()
                }
                val duration = player.duration
                player.release()
                duration
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun onPlayPauseAudio() {
        val currentState = _uiState.value
        val audioPath = currentState.memory?.localAudioPath

        if (audioPath == null) {
            _uiState.value = currentState.copy(errorMessage = "Audio file not found")
            return
        }

        try {
            if (currentState.isPlaying) {
                audioPlayer.pause()
                _uiState.value = currentState.copy(isPlaying = false)
            } else {
                if (audioPlayer.isPlaying()) {
                    audioPlayer.resume()
                } else {
                    audioPlayer.play(audioPath) {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = false,
                            currentPosition = 0
                        )
                    }
                }
                _uiState.value = currentState.copy(isPlaying = true)
                startProgressUpdate()
            }
        } catch (e: Exception) {
            _uiState.value = currentState.copy(
                isPlaying = false,
                errorMessage = "Failed to play audio: ${e.message}"
            )
        }
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val position = audioPlayer.getCurrentPosition()
                _uiState.value = _uiState.value.copy(currentPosition = position)
                kotlinx.coroutines.delay(100)
            }
        }
    }

    fun onSeekAudio(position: Float) {
        audioPlayer.seekTo(position.toInt())
        _uiState.value = _uiState.value.copy(currentPosition = position.toInt())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}

data class MemoryDetailUiState(
    val memory: Memory? = null,
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val audioDuration: Int = 0,
    val errorMessage: String? = null
)