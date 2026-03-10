package uk.ac.tees.mad.photowhisper.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MutedBrown,
    onPrimary = Cream,
    primaryContainer = Beige,
    onPrimaryContainer = DarkBrown,
    secondary = PastelPeach,
    onSecondary = DarkBrown,
    secondaryContainer = PastelBlush,
    onSecondaryContainer = TextPrimary,
    background = Cream,
    onBackground = TextPrimary,
    surface = Beige,
    onSurface = TextPrimary,
    surfaceVariant = SoftGray,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Cream
)

@Composable
fun PhotoWhisperTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}