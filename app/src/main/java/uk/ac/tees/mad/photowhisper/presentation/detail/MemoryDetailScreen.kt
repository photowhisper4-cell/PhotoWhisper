// presentation/detail/MemoryDetailScreen.kt

package uk.ac.tees.mad.photowhisper.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import uk.ac.tees.mad.photowhisper.presentation.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailScreen(
    viewModel: MemoryDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Memory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently delete the photo, voice note, and all data " +
                            "associated with this memory. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDelete(onDeleted = onNavigateBack) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::onDismissDeleteDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Memory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                // ── NEW: delete icon in the top-right ────────────────────────
                actions = {
                    if (uiState.memory != null && !uiState.isDeleting) {
                        IconButton(onClick = viewModel::onDeleteMemory) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete memory",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Show full-screen spinner while deleting
                uiState.isDeleting -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Deleting memory…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.isLoading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }
                uiState.memory != null -> {
                    MemoryContent(
                        uiState = uiState,
                        onPlayPause = viewModel::onPlayPauseAudio,
                        onSeek = viewModel::onSeekAudio
                    )
                }
                else -> {
                    ErrorContent(message = uiState.errorMessage ?: "Memory not found")
                }
            }

            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(text = error)
                }
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
            }
        }
    }
}

// ── The rest of the file is unchanged ────────────────────────────────────────

@Composable
fun MemoryContent(
    uiState: MemoryDetailUiState,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit
) {
    val memory = uiState.memory ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = memory.localPhotoPath,
                contentDescription = "Memory photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = formatDate(memory.dateCaptured),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatTime(memory.dateCaptured),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        memory.location?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📍 $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        memory.cameraInfo?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📷 $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Voice Note", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        AudioPlayerCard(
            isPlaying = uiState.isPlaying,
            currentPosition = uiState.currentPosition,
            duration = uiState.audioDuration,
            onPlayPause = onPlayPause,
            onSeek = onSeek
        )
    }
}

@Composable
fun AudioPlayerCard(
    isPlaying: Boolean,
    currentPosition: Int,
    duration: Int,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = formatDuration(currentPosition), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = formatDuration(duration), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = onSeek,
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            FloatingActionButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
    }
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))

fun formatTime(timestamp: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

fun formatDuration(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format("%d:%02d", minutes, seconds)
}