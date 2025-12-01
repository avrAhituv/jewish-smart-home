package com.jewishhome.app.presentation.screens.screensaver

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jewishhome.app.presentation.theme.*

@Composable
fun ScreensaverScreen(
    onDismiss: () -> Unit,
    viewModel: ScreensaverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Animate clock position for burn-in protection
    val infiniteTransition = rememberInfiniteTransition(label = "position")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismiss()
            }
    ) {
        // Background Photo with crossfade
        if (uiState.hasPhotos && uiState.currentPhoto != null) {
            key(uiState.currentPhoto?.id) {
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(1500)),
                    exit = fadeOut(animationSpec = tween(1500))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.currentPhoto?.uri)
                            .crossfade(1500)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Darken overlay for better clock visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        // Clock and info overlay
        if (uiState.showClock) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = offsetX.dp, y = offsetY.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Digital Clock
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = uiState.currentTime,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 140.sp,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = (-4).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = uiState.currentSeconds,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light
                            ),
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hebrew Date
                    if (uiState.hebrewDate.isNotEmpty()) {
                        Text(
                            text = uiState.hebrewDate,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next Zman
                    uiState.nextZman?.let { zman ->
                        Surface(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = zman.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = zman.timeFormatted,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Secondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "גע במסך כדי לחזור",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
