package com.jewishhome.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewishhome.app.presentation.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToZmanim: () -> Unit,
    onNavigateToMusic: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToTexts: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToScreensaver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Primary,
                        PrimaryVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top section - Clock and Date
            ClockSection(
                currentTime = uiState.currentTime,
                hebrewDate = uiState.hebrewDate,
                gregorianDate = uiState.gregorianDate
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info bar - Next Zman and Today's Event
            InfoBar(
                nextZman = uiState.nextZman,
                nextZmanTime = uiState.nextZmanTime,
                todayEvent = uiState.todayEvent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Grid
            AppGrid(
                onNavigateToZmanim = onNavigateToZmanim,
                onNavigateToMusic = onNavigateToMusic,
                onNavigateToContacts = onNavigateToContacts,
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToRecipes = onNavigateToRecipes,
                onNavigateToTexts = onNavigateToTexts,
                onNavigateToPhotos = onNavigateToPhotos,
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )

            // Mini Player (if playing)
            if (uiState.isPlaying) {
                MiniPlayer(
                    songTitle = uiState.currentSongTitle,
                    artistName = uiState.currentArtist,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.playNext() },
                    isPlaying = uiState.isPlaying
                )
            }
        }
    }
}

@Composable
private fun ClockSection(
    currentTime: String,
    hebrewDate: String,
    gregorianDate: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                fontWeight = FontWeight.Light
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = hebrewDate,
            style = MaterialTheme.typography.headlineMedium,
            color = Secondary
        )

        Text(
            text = gregorianDate,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun InfoBar(
    nextZman: String,
    nextZmanTime: String,
    todayEvent: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Next Zman
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "הזמן הקרוב: $nextZman",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = nextZmanTime,
                    style = MaterialTheme.typography.titleMedium,
                    color = Secondary
                )
            }
        }

        // Today's Event
        if (todayEvent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = todayEvent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AppGrid(
    onNavigateToZmanim: () -> Unit,
    onNavigateToMusic: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToTexts: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apps = listOf(
        AppItem("זמנים", Icons.Default.WbSunny, onNavigateToZmanim),
        AppItem("נגן", Icons.Default.MusicNote, onNavigateToMusic),
        AppItem("אלפון", Icons.Default.Contacts, onNavigateToContacts),
        AppItem("לוח שנה", Icons.Default.CalendarMonth, onNavigateToCalendar),
        AppItem("מתכונים", Icons.Default.Restaurant, onNavigateToRecipes),
        AppItem("ברכות", Icons.Default.MenuBook, onNavigateToTexts),
        AppItem("תמונות", Icons.Default.PhotoLibrary, onNavigateToPhotos),
        AppItem("הגדרות", Icons.Default.Settings, onNavigateToSettings)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            apps.take(4).forEach { app ->
                AppButton(
                    title = app.title,
                    icon = app.icon,
                    onClick = app.onClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            apps.drop(4).forEach { app ->
                AppButton(
                    title = app.title,
                    icon = app.icon,
                    onClick = app.onClick
                )
            }
        }
    }
}

@Composable
private fun AppButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.15f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniPlayer(
    songTitle: String,
    artistName: String,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    isPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Row {
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private data class AppItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
