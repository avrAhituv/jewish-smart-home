package com.jewishhome.app.presentation.screens.texts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewishhome.app.domain.model.BrachaText
import com.jewishhome.app.domain.model.Nusach
import com.jewishhome.app.domain.model.TextCategory
import com.jewishhome.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextsScreen(
    viewModel: TextsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.selectedText != null || uiState.selectedCategory != null) {
        when {
            uiState.selectedText != null -> viewModel.goBackToCategory()
            uiState.selectedCategory != null -> viewModel.clearSelection()
        }
    }

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
            title = {
                Text(
                    when {
                        uiState.selectedText != null -> uiState.selectedText!!.name
                        uiState.selectedCategory != null -> uiState.selectedCategory!!.name
                        else -> "ברכות וטקסטים"
                    },
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    when {
                        uiState.selectedText != null -> viewModel.goBackToCategory()
                        uiState.selectedCategory != null -> viewModel.clearSelection()
                        else -> onNavigateBack()
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                }
            },
            actions = {
                if (uiState.selectedText != null) {
                    // Font size controls
                    IconButton(onClick = { viewModel.decreaseFontSize() }) {
                        Icon(Icons.Default.TextDecrease, "הקטן טקסט", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.increaseFontSize() }) {
                        Icon(Icons.Default.TextIncrease, "הגדל טקסט", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        when {
            uiState.selectedText != null -> {
                TextDetailView(
                    text = uiState.selectedText!!,
                    displayText = viewModel.getTextForCurrentNusach(uiState.selectedText!!),
                    fontSize = uiState.fontSize,
                    nusach = uiState.currentNusach,
                    onNusachChange = { viewModel.setNusach(it) }
                )
            }
            uiState.selectedCategory != null -> {
                TextsListView(
                    texts = uiState.selectedCategory!!.texts,
                    onTextClick = { viewModel.selectText(it) }
                )
            }
            else -> {
                CategoriesView(
                    categories = uiState.categories,
                    onCategoryClick = { viewModel.selectCategory(it) }
                )
            }
        }
    }
}

@Composable
private fun CategoriesView(
    categories: List<TextCategory>,
    onCategoryClick: (TextCategory) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "בחר קטגוריה",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(categories) { category ->
            CategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: TextCategory,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Secondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when (category.icon) {
                    "restaurant" -> Icons.Default.Restaurant
                    "food" -> Icons.Default.Fastfood
                    "healing" -> Icons.Default.Healing
                    "directions_car" -> Icons.Default.DirectionsCar
                    "bedtime" -> Icons.Default.Bedtime
                    "wb_sunny" -> Icons.Default.WbSunny
                    "star" -> Icons.Default.Star
                    else -> Icons.Default.MenuBook
                },
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${category.texts.size} טקסטים",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Icon(
            Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TextsListView(
    texts: List<BrachaText>,
    onTextClick: (BrachaText) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(texts) { text ->
            TextListItem(
                text = text,
                onClick = { onTextClick(text) }
            )
        }
    }
}

@Composable
private fun TextListItem(
    text: BrachaText,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.TextSnippet,
            contentDescription = null,
            tint = Secondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            text.instructions?.let { instructions ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = instructions,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Icon(
            Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun TextDetailView(
    text: BrachaText,
    displayText: String,
    fontSize: Float,
    nusach: Nusach,
    onNusachChange: (Nusach) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Nusach Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            NusachButton(
                text = "אשכנז",
                selected = nusach == Nusach.ASHKENAZ,
                onClick = { onNusachChange(Nusach.ASHKENAZ) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            NusachButton(
                text = "ספרד",
                selected = nusach == Nusach.SEFARD,
                onClick = { onNusachChange(Nusach.SEFARD) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            NusachButton(
                text = "עדות המזרח",
                selected = nusach == Nusach.EDOT_MIZRACH,
                onClick = { onNusachChange(Nusach.EDOT_MIZRACH) }
            )
        }

        // Instructions
        text.instructions?.let { instructions ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Secondary.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Text Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.8f).sp
                ),
                color = Color.White,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun NusachButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        color = if (selected) Secondary else Color.White.copy(alpha = 0.1f),
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}
