package uk.kulikov.compottie.sample

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlin.math.roundToInt

sealed interface AnimationSource {
    data class Url(val url: String) : AnimationSource
    data class FileBytes(val bytes: ByteArray, val isZip: Boolean) : AnimationSource
}

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        var animationSource by remember { mutableStateOf<AnimationSource?>(null) }
        var urlText by remember { mutableStateOf("") }
        var showFilePicker by remember { mutableStateOf(false) }
        var iterations by remember { mutableStateOf(Compottie.IterateForever) }
        var speed by remember { mutableStateOf(1f) }

        PlatformFilePicker(show = showFilePicker) { bytes ->
            showFilePicker = false
            if (bytes != null) {
                val isZip = bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
                animationSource = AnimationSource.FileBytes(bytes, isZip)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Lottie Player",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(12.dp))

            // URL input row
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Paste Lottie URL (.json or .lottie)") },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        if (urlText.isNotBlank()) {
                            animationSource = AnimationSource.Url(urlText.trim())
                        }
                    },
                    enabled = urlText.isNotBlank(),
                ) {
                    Text("Load")
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { showFilePicker = true }) {
                    Text("Open File")
                }
                if (animationSource != null) {
                    OutlinedButton(
                        onClick = {
                            animationSource = null
                            urlText = ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                    ) {
                        Text("Clear")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Animation display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(animationSource) { source ->
                    if (source != null) {
                        LottiePlayer(
                            source = source,
                            iterations = iterations,
                            speed = speed,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No animation loaded",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "Paste a URL or open a .json / .lottie file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Playback controls
            if (animationSource != null) {
                PlaybackControls(
                    iterations = iterations,
                    onIterationsChange = { iterations = it },
                    speed = speed,
                    onSpeedChange = { speed = it },
                )
            }
        }
    }
}

@Composable
private fun LottiePlayer(
    source: AnimationSource,
    iterations: Int,
    speed: Float,
) {
    val composition by rememberLottieComposition(source) {
        when (source) {
            is AnimationSource.Url -> LottieCompositionSpec.Url(source.url)
            is AnimationSource.FileBytes -> {
                if (source.isZip) {
                    LottieCompositionSpec.DotLottie(source.bytes)
                } else {
                    LottieCompositionSpec.JsonString(source.bytes.decodeToString())
                }
            }
        }
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
    )

    val currentComposition = composition
    if (currentComposition != null) {
        Image(
            painter = rememberLottiePainter(
                composition = currentComposition,
                progress = { progress },
            ),
            contentDescription = "Lottie animation",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        CircularProgressIndicator()
    }
}

@Composable
private fun PlaybackControls(
    iterations: Int,
    onIterationsChange: (Int) -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = iterations == Compottie.IterateForever,
                    onClick = {
                        onIterationsChange(
                            if (iterations == Compottie.IterateForever) 1
                            else Compottie.IterateForever
                        )
                    },
                    label = { Text("Loop") },
                )
                Text(
                    "Speed: ${(speed * 10).roundToInt() / 10.0}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Slider(
                value = speed,
                onValueChange = onSpeedChange,
                valueRange = 0.1f..3f,
                steps = 28,
            )
        }
    }
}
