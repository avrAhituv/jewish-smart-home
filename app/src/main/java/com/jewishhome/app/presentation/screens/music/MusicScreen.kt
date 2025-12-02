package com.jewishhome.app.presentation.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jewishhome.app.domain.model.Song
import com.jewishhome.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryVariant)
                )
            )
    ) {
        TopAppBar(
            title = { Text("נגן מוזיקה", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshSongs() }) {
                    Icon(Icons.Default.Refresh, "רענן", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.search(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs
        MusicTabs(
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.selectTab(it) }
        )

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Secondary
                    )
                }
                uiState.songs.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.MusicOff,
                        title = "אין שירים",
                        subtitle = "העתק שירים לכרטיס הזכרון"
                    )
                }
                else -> {
                    when (uiState.selectedTab) {
                        MusicTab.SONGS -> SongsList(
                            songs = uiState.filteredSongs,
                            currentSong = uiState.currentSong,
                            onSongClick = { song ->
                                viewModel.playQueue(uiState.filteredSongs, uiState.filteredSongs.indexOf(song))
                            }
                        )
                        MusicTab.ARTISTS -> ArtistsList(
                            artists = uiState.artists,
                            onArtistClick = { viewModel.selectArtist(it) }
                        )
                        MusicTab.ALBUMS -> AlbumsList(
                            albums = uiState.albums,
                            onAlbumClick = { viewModel.selectAlbum(it) }
                        )
                        MusicTab.PLAYLISTS -> PlaylistsList(
                            playlists = uiState.playlists,
                            onCreatePlaylist = { viewModel.createPlaylist(it) }
                        )
                        MusicTab.RECENT -> SongsList(
                            songs = uiState.recentlyPlayed,
                            currentSong = uiState.currentSong,
                            onSongClick = { song ->
                                viewModel.playQueue(uiState.recentlyPlayed, uiState.recentlyPlayed.indexOf(song))
                            }
                        )
                    }
                }
            }
        }

        // Now Playing Bar
        if (uiState.currentSong != null) {
            NowPlayingBar(
                song = uiState.currentSong!!,
                isPlaying = uiState.isPlaying,
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                shuffleMode = uiState.shuffleMode,
                repeatMode = uiState.repeatMode,
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.skipToNext() },
                onPrevious = { viewModel.skipToPrevious() },
                onSeek = { viewModel.seekTo(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeatMode() }
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("חפש שירים...", color = Color.White.copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "נקה", tint = Color.White)
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Secondary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Secondary
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
private fun MusicTabs(
    selectedTab: MusicTab,
    onTabSelected: (MusicTab) -> Unit
) {
    val tabs = listOf(
        MusicTab.SONGS to "שירים",
        MusicTab.ARTISTS to "אמנים",
        MusicTab.ALBUMS to "אלבומים",
        MusicTab.PLAYLISTS to "רשימות",
        MusicTab.RECENT to "אחרונים"
    )

    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab },
        containerColor = Color.Transparent,
        contentColor = Color.White,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            if (tabPositions.isNotEmpty()) {
                val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = tabPositions[selectedIndex].left)
                        .width(tabPositions[selectedIndex].width)
                        .height(3.dp)
                        .background(Secondary)
                )
            }
        }
    ) {
        tabs.forEach { (tab, title) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        title,
                        color = if (selectedTab == tab) Secondary else Color.White.copy(alpha = 0.7f)
                    )
                }
            )
        }
    }
}

@Composable
private fun SongsList(
    songs: List<Song>,
    currentSong: Song?,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs, key = { it.id }) { song ->
            SongItem(
                song = song,
                isPlaying = currentSong?.id == song.id,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun SongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying) Secondary.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Song Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPlaying) Secondary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration
        Text(
            text = formatDuration(song.duration),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ArtistsList(
    artists: List<String>,
    onArtistClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(artists) { artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onArtistClick(artist) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Secondary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AlbumsList(
    albums: List<String>,
    onAlbumClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(albums) { album ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onAlbumClick(album) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = null,
                        tint = Secondary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = album,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PlaylistsList(
    playlists: List<com.jewishhome.app.domain.model.Playlist>,
    onCreatePlaylist: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Secondary.copy(alpha = 0.2f))
                    .clickable { showCreateDialog = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("צור רשימת השמעה חדשה", color = Secondary)
            }
        }

        items(playlists) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = Secondary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = "${playlist.songs.size} שירים",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("רשימת השמעה חדשה") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("שם הרשימה") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("צור")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("ביטול")
                }
            }
        )
    }
}

@Composable
private fun NowPlayingBar(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    shuffleMode: com.jewishhome.app.domain.model.ShuffleMode,
    repeatMode: com.jewishhome.app.domain.model.RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(16.dp)
    ) {
        // Progress Bar
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { onSeek((it * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = Secondary,
                activeTrackColor = Secondary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Song Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "ערבב",
                    tint = if (shuffleMode == com.jewishhome.app.domain.model.ShuffleMode.ON) Secondary else Color.White.copy(alpha = 0.5f)
                )
            }

            // Previous
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "הקודם",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play/Pause
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Secondary)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "השהה" else "נגן",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            IconButton(onClick = onNext) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "הבא",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat
            IconButton(onClick = onToggleRepeat) {
                Icon(
                    when (repeatMode) {
                        com.jewishhome.app.domain.model.RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "חזור",
                    tint = if (repeatMode != com.jewishhome.app.domain.model.RepeatMode.OFF) Secondary else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Secondary.copy(alpha = 0.5f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
