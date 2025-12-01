package com.jewishhome.app.presentation.screens.screensaver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewishhome.app.presentation.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ScreensaverScreen(
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now().format(timeFormatter)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clock
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Light
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hebrew Date
            Text(
                text = "כ\"ח כסלו תשפ\"ה",
                style = MaterialTheme.typography.headlineLarge,
                color = Secondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Touch to exit hint
            Text(
                text = "גע במסך כדי לחזור",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
