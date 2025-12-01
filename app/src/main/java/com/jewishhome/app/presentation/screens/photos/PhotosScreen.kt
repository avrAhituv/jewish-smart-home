package com.jewishhome.app.presentation.screens.photos

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jewishhome.app.domain.model.Photo
import com.jewishhome.app.domain.model.PhotoAlbum
import com.jewishhome.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    onNavigateBack: () -> Unit,
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAlbums()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryVariant)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        when (uiState.viewMode) {
                            PhotosViewMode.ALBUMS -> "תמונות"
                            PhotosViewMode.GRID -> uiState.selectedAlbum?.name ?: "כל התמונות"
                            PhotosViewMode.DETAIL -> uiState.selectedPhoto?.name ?: ""
                        },
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (uiState.viewMode) {
                                PhotosViewMode.ALBUMS -> onNavigateBack()
                                PhotosViewMode.GRID -> viewModel.goToAlbums()
                                PhotosViewMode.DETAIL -> viewModel.clearSelectedPhoto()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.viewMode == PhotosViewMode.ALBUMS) {
                        IconButton(onClick = { viewModel.refreshPhotos() }) {
                            if (uiState.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "רענן", tint = Color.White)
                            }
                        }
                    }
                    if (uiState.viewMode == PhotosViewMode.ALBUMS) {
                        IconButton(onClick = { viewModel.showAllPhotos() }) {
                            Icon(Icons.Default.PhotoLibrary, "כל התמונות", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.viewMode != PhotosViewMode.DETAIL -> {
                        LoadingContent()
                    }
                    uiState.error != null -> {
                        ErrorContent(
                            error = uiState.error!!,
                            onRetry = { viewModel.loadAlbums() }
                        )
                    }
                    else -> {
                        AnimatedContent(
                            targetState = uiState.viewMode,
                            label = "view_mode_transition"
                        ) { viewMode ->
                            when (viewMode) {
                                PhotosViewMode.ALBUMS -> {
                                    AlbumsGrid(
                                        albums = uiState.albums,
                                        screensaverAlbumId = uiState.screensaverAlbumId,
                                        onAlbumClick = { viewModel.selectAlbum(it) },
                                        onToggleScreensaver = { viewModel.toggleScreensaverAlbum(it) }
                                    )
                                }
                                PhotosViewMode.GRID -> {
                                    PhotosGrid(
                                        photos = uiState.currentPhotos,
                                        onPhotoClick = { viewModel.selectPhoto(it) }
                                    )
                                }
                                PhotosViewMode.DETAIL -> {
                                    uiState.selectedPhoto?.let { photo ->
                                        PhotoDetailView(
                                            photo = photo,
                                            currentIndex = uiState.selectedPhotoIndex,
                                            totalCount = uiState.currentPhotos.size,
                                            onPrevious = { viewModel.navigateToPreviousPhoto() },
                                            onNext = { viewModel.navigateToNextPhoto() },
                                            onClose = { viewModel.clearSelectedPhoto() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Error Snackbar
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Secondary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "טוען תמונות...",
                color = Color.White
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                error,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("נסה שוב")
            }
        }
    }
}

@Composable
private fun AlbumsGrid(
    albums: List<PhotoAlbum>,
    screensaverAlbumId: String?,
    onAlbumClick: (PhotoAlbum) -> Unit,
    onToggleScreensaver: (PhotoAlbum) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.Photo,
            message = "לא נמצאו אלבומים",
            subMessage = "הוסף תמונות למכשיר כדי לראות אותן כאן"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 200.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    isScreensaverAlbum = album.id == screensaverAlbumId,
                    onClick = { onAlbumClick(album) },
                    onToggleScreensaver = { onToggleScreensaver(album) }
                )
            }
        }
    }
}

@Composable
private fun AlbumCard(
    album: PhotoAlbum,
    isScreensaverAlbum: Boolean,
    onClick: () -> Unit,
    onToggleScreensaver: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cover Image
            if (album.coverUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.coverUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = album.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Gradient overlay for text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${album.photoCount} תמונות",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Screensaver indicator/toggle
            IconButton(
                onClick = onToggleScreensaver,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    if (isScreensaverAlbum) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isScreensaverAlbum) "הסר משומר מסך" else "הגדר כשומר מסך",
                    tint = if (isScreensaverAlbum) Secondary else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PhotosGrid(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit
) {
    if (photos.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.PhotoLibrary,
            message = "אין תמונות באלבום זה"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoThumbnail(
                    photo = photo,
                    onClick = { onPhotoClick(photo) }
                )
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: Photo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = photo.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun PhotoDetailView(
    photo: Photo,
    currentIndex: Int,
    totalCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > 100) {
                            if (dragOffset > 0 && currentIndex > 0) {
                                onPrevious()
                            } else if (dragOffset < 0 && currentIndex < totalCount - 1) {
                                onNext()
                            }
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showInfo = !showInfo }
                )
            }
    ) {
        // Main Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = photo.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (abs(dragOffset) > 0) (abs(dragOffset) / 10).dp else 0.dp),
            contentScale = ContentScale.Fit
        )

        // Navigation arrows (always visible on tablet)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Next button (RTL - right side goes to next)
            FilledIconButton(
                onClick = onNext,
                enabled = currentIndex < totalCount - 1,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "הבא",
                    tint = if (currentIndex < totalCount - 1) Color.White else Color.Transparent
                )
            }

            // Previous button (RTL - left side goes to previous)
            FilledIconButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "הקודם",
                    tint = if (currentIndex > 0) Color.White else Color.Transparent
                )
            }
        }

        // Top bar with close and info
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "סגור", tint = Color.White)
                    }

                    Text(
                        "${currentIndex + 1} / $totalCount",
                        color = Color.White
                    )

                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, "שתף", tint = Color.White)
                    }
                }
            }
        }

        // Bottom info panel
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        photo.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val displayDate = photo.dateTaken ?: photo.dateAdded

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        PhotoInfoItem(
                            icon = Icons.Default.CalendarToday,
                            text = dateFormat.format(Date(displayDate))
                        )
                        PhotoInfoItem(
                            icon = Icons.Default.AspectRatio,
                            text = "${photo.width} × ${photo.height}"
                        )
                        PhotoInfoItem(
                            icon = Icons.Default.Storage,
                            text = formatFileSize(photo.size)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EmptyStateContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    subMessage: String? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (subMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    subMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
