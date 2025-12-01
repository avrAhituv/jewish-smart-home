package com.jewishhome.app.presentation.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewishhome.app.domain.model.CandleLightingOffset
import com.jewishhome.app.domain.model.GeoLocation
import com.jewishhome.app.domain.model.ZmanimCalculationMethod
import com.jewishhome.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
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
            title = {
                Text(
                    if (uiState.selectedCategory == null) "הגדרות" else uiState.selectedCategory!!.hebrewName,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (uiState.selectedCategory != null) {
                            viewModel.selectCategory(null)
                        } else {
                            onNavigateBack()
                        }
                    }
                ) {
                    Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        AnimatedContent(
            targetState = uiState.selectedCategory,
            label = "settings_content"
        ) { category ->
            if (category == null) {
                SettingsCategoriesList(
                    onCategoryClick = { viewModel.selectCategory(it) }
                )
            } else {
                when (category) {
                    SettingsCategory.LOCATION -> LocationSettings(
                        selectedLocation = uiState.selectedLocation,
                        availableLocations = viewModel.availableLocations,
                        onLocationSelected = { viewModel.setLocation(it) }
                    )
                    SettingsCategory.ZMANIM -> ZmanimSettings(
                        calculationMethod = uiState.calculationMethod,
                        candleLightingOffset = uiState.candleLightingOffset,
                        onMethodChange = { viewModel.setCalculationMethod(it) },
                        onOffsetChange = { viewModel.setCandleLightingOffset(it) }
                    )
                    SettingsCategory.DISPLAY -> DisplaySettings(
                        use24HourFormat = uiState.use24HourFormat,
                        darkMode = uiState.darkMode,
                        textSize = uiState.textSize,
                        backgroundName = uiState.backgroundName,
                        onUse24HourChange = { viewModel.setUse24HourFormat(it) },
                        onDarkModeChange = { viewModel.setDarkMode(it) },
                        onTextSizeChange = { viewModel.setTextSize(it) },
                        onBackgroundChange = { viewModel.setBackgroundName(it) }
                    )
                    SettingsCategory.SCREENSAVER -> ScreensaverSettings(
                        enabled = uiState.screensaverEnabled,
                        timeout = uiState.screensaverTimeout,
                        showClock = uiState.screensaverShowClock,
                        onEnabledChange = { viewModel.setScreensaverEnabled(it) },
                        onTimeoutChange = { viewModel.setScreensaverTimeout(it) },
                        onShowClockChange = { viewModel.setScreensaverShowClock(it) }
                    )
                    SettingsCategory.TEXTS -> TextsSettings(
                        nusach = uiState.nusach,
                        fontSize = uiState.fontSize,
                        onNusachChange = { viewModel.setNusach(it) },
                        onFontSizeChange = { viewModel.setFontSize(it) }
                    )
                    SettingsCategory.KIOSK -> KioskSettings(
                        pinCode = uiState.pinCode,
                        kioskMode = uiState.kioskMode,
                        onPinCodeChange = { viewModel.setPinCode(it) },
                        onKioskModeChange = { viewModel.setKioskMode(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoriesList(
    onCategoryClick: (SettingsCategory) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SettingsCategory.entries) { category ->
            SettingsCategoryCard(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    category: SettingsCategory,
    onClick: () -> Unit
) {
    val icon = when (category) {
        SettingsCategory.LOCATION -> Icons.Default.LocationOn
        SettingsCategory.ZMANIM -> Icons.Default.Schedule
        SettingsCategory.DISPLAY -> Icons.Default.Palette
        SettingsCategory.SCREENSAVER -> Icons.Default.Slideshow
        SettingsCategory.TEXTS -> Icons.Default.MenuBook
        SettingsCategory.KIOSK -> Icons.Default.Lock
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                category.hebrewName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun LocationSettings(
    selectedLocation: GeoLocation,
    availableLocations: List<GeoLocation>,
    onLocationSelected: (GeoLocation) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableLocations) { location ->
            LocationCard(
                location = location,
                isSelected = location.name == selectedLocation.name,
                onClick = { onLocationSelected(location) }
            )
        }
    }
}

@Composable
private fun LocationCard(
    location: GeoLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Secondary.copy(alpha = 0.3f) else SurfaceLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Secondary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    location.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    "${location.country} • ${String.format("%.2f", location.latitude)}°N",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ZmanimSettings(
    calculationMethod: ZmanimCalculationMethod,
    candleLightingOffset: CandleLightingOffset,
    onMethodChange: (ZmanimCalculationMethod) -> Unit,
    onOffsetChange: (CandleLightingOffset) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "שיטת חישוב") {
                ZmanimCalculationMethod.entries.forEach { method ->
                    RadioOption(
                        text = when (method) {
                            ZmanimCalculationMethod.GRA -> "גר\"א"
                            ZmanimCalculationMethod.MGA -> "מג\"א"
                            ZmanimCalculationMethod.BAAL_HATANYA -> "בעל התניא"
                        },
                        selected = method == calculationMethod,
                        onClick = { onMethodChange(method) }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "הדלקת נרות לפני השקיעה") {
                CandleLightingOffset.entries.forEach { offset ->
                    RadioOption(
                        text = "${offset.minutes} דקות",
                        selected = offset == candleLightingOffset,
                        onClick = { onOffsetChange(offset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplaySettings(
    use24HourFormat: Boolean,
    darkMode: String,
    textSize: String,
    backgroundName: String,
    onUse24HourChange: (Boolean) -> Unit,
    onDarkModeChange: (String) -> Unit,
    onTextSizeChange: (String) -> Unit,
    onBackgroundChange: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSwitchItem(
                title = "פורמט 24 שעות",
                checked = use24HourFormat,
                onCheckedChange = onUse24HourChange
            )
        }

        item {
            SettingsSection(title = "מצב תצוגה") {
                listOf("light" to "בהיר", "dark" to "כהה", "auto" to "אוטומטי").forEach { (mode, name) ->
                    RadioOption(
                        text = name,
                        selected = mode == darkMode,
                        onClick = { onDarkModeChange(mode) }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "גודל טקסט") {
                listOf("small" to "קטן", "medium" to "בינוני", "large" to "גדול").forEach { (size, name) ->
                    RadioOption(
                        text = name,
                        selected = size == textSize,
                        onClick = { onTextSizeChange(size) }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "רקע") {
                val backgrounds = listOf(
                    "jerusalem_gold" to "ירושלים זהב",
                    "kotel" to "הכותל",
                    "sunset" to "שקיעה",
                    "shabbat" to "שבת",
                    "solid_blue" to "כחול אחיד"
                )
                backgrounds.forEach { (name, displayName) ->
                    RadioOption(
                        text = displayName,
                        selected = name == backgroundName,
                        onClick = { onBackgroundChange(name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreensaverSettings(
    enabled: Boolean,
    timeout: Int,
    showClock: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onShowClockChange: (Boolean) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSwitchItem(
                title = "הפעל שומר מסך",
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }

        item {
            SettingsSwitchItem(
                title = "הצג שעון",
                checked = showClock,
                onCheckedChange = onShowClockChange
            )
        }

        item {
            SettingsSection(title = "זמן המתנה (דקות)") {
                val timeouts = listOf(1, 2, 5, 10, 15, 30)
                timeouts.forEach { minutes ->
                    RadioOption(
                        text = "$minutes דקות",
                        selected = minutes == timeout,
                        onClick = { onTimeoutChange(minutes) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextsSettings(
    nusach: String,
    fontSize: Float,
    onNusachChange: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "נוסח") {
                val nusachim = listOf(
                    "ASHKENAZ" to "אשכנז",
                    "SFARD" to "ספרד",
                    "EDOT_HAMIZRACH" to "עדות המזרח"
                )
                nusachim.forEach { (value, name) ->
                    RadioOption(
                        text = name,
                        selected = value == nusach,
                        onClick = { onNusachChange(value) }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "גודל גופן") {
                Column {
                    Text(
                        "גודל: ${fontSize.toInt()}",
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 14f..32f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Secondary,
                            activeTrackColor = Secondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun KioskSettings(
    pinCode: String,
    kioskMode: String,
    onPinCodeChange: (String) -> Unit,
    onKioskModeChange: (String) -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "מצב קיוסק") {
                val modes = listOf(
                    "lock_task" to "נעילת משימה (Lock Task)",
                    "device_owner" to "בעלות על מכשיר (Device Owner)"
                )
                modes.forEach { (mode, name) ->
                    RadioOption(
                        text = name,
                        selected = mode == kioskMode,
                        onClick = { onKioskModeChange(mode) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPinDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Pin, null, tint = Secondary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("קוד PIN", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            "****",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "הערה: מצב קיוסק מונע יציאה מהאפליקציה. לביטול, הזן את קוד ה-PIN.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }

    if (showPinDialog) {
        PinCodeDialog(
            currentPin = pinCode,
            onDismiss = { showPinDialog = false },
            onConfirm = { newPin ->
                onPinCodeChange(newPin)
                showPinDialog = false
            }
        )
    }
}

@Composable
private fun PinCodeDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("שנה קוד PIN") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                label = { Text("קוד חדש (4-6 ספרות)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (pin.length >= 4) onConfirm(pin) },
                enabled = pin.length >= 4
            ) {
                Text("אישור")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Secondary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White)
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Secondary,
                    checkedTrackColor = Secondary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
