package com.jewishhome.app.presentation.screens.zmanim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewishhome.app.presentation.theme.*

data class ZmanItem(
    val name: String,
    val time: String,
    val isPassed: Boolean,
    val isCurrent: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZmanimScreen(
    viewModel: ZmanimViewModel = hiltViewModel(),
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
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "זמני היום",
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "חזרה",
                        tint = Color.White
                    )
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = uiState.location,
                        color = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Hebrew Date
        Text(
            text = uiState.hebrewDate,
            style = MaterialTheme.typography.headlineMedium,
            color = Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Zmanim List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.zmanim) { zman ->
                ZmanRow(zman = zman)
            }
        }

        // Shabbat Times Card
        ShabbatTimesCard(
            candleLighting = uiState.candleLighting,
            shabbatEnds = uiState.shabbatEnds,
            parasha = uiState.parasha,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ZmanRow(zman: ZmanItem) {
    val backgroundColor = when {
        zman.isCurrent -> Secondary.copy(alpha = 0.3f)
        zman.isPassed -> Color.White.copy(alpha = 0.05f)
        else -> Color.White.copy(alpha = 0.15f)
    }

    val textColor = when {
        zman.isPassed -> Color.White.copy(alpha = 0.5f)
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (zman.isPassed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ZmanPassed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (zman.isCurrent) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = zman.name,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = if (zman.isCurrent) FontWeight.Bold else FontWeight.Normal
            )
        }

        Text(
            text = zman.time,
            style = MaterialTheme.typography.titleMedium,
            color = if (zman.isCurrent) Secondary else textColor,
            fontWeight = if (zman.isCurrent) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ShabbatTimesCard(
    candleLighting: String,
    shabbatEnds: String,
    parasha: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Shabbat.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "כניסת שבת",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = candleLighting,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "צאת שבת",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = shabbatEnds,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "פרשת $parasha",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}
