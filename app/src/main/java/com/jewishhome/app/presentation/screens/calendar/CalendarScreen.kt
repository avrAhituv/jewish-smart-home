package com.jewishhome.app.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.presentation.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
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
            title = { Text("לוח שנה עברי", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.goToToday() }) {
                    Icon(Icons.Default.Today, "היום", tint = Color.White)
                }
                IconButton(onClick = { viewModel.syncCalendars() }) {
                    Icon(Icons.Default.Sync, "סנכרן", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Secondary)
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Calendar Grid - Left side
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    MonthHeader(
                        monthInfo = uiState.currentMonth,
                        year = uiState.selectedYear,
                        month = uiState.selectedMonthNumber,
                        onPreviousMonth = { viewModel.goToPreviousMonth() },
                        onNextMonth = { viewModel.goToNextMonth() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendar Grid
                    CalendarGrid(
                        monthInfo = uiState.currentMonth,
                        selectedDate = uiState.selectedDate,
                        holidays = uiState.currentMonthHolidays,
                        onDateSelected = { viewModel.selectDate(it) }
                    )
                }

                // Side Panel - Right side
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    when {
                        uiState.selectedDayInfo != null -> {
                            DayDetailPanel(
                                dayInfo = uiState.selectedDayInfo!!,
                                onClose = { viewModel.clearSelectedDate() }
                            )
                        }
                        else -> {
                            UpcomingPanel(
                                upcomingEvents = uiState.upcomingEvents,
                                nextHoliday = uiState.nextHoliday,
                                nextParasha = uiState.nextParasha
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    monthInfo: MonthInfo?,
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val hebrewLocale = Locale("he", "IL")
    val yearMonth = YearMonth.of(year, month)
    val gregorianMonthName = yearMonth.month.getDisplayName(TextStyle.FULL, hebrewLocale)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronLeft, "חודש הבא", tint = Color.White)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$gregorianMonthName $year",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            monthInfo?.let {
                Text(
                    text = "${it.hebrewMonth.hebrewName} ${numberToHebrewYear(it.hebrewYear)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary
                )
            }
        }

        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronRight, "חודש קודם", tint = Color.White)
        }
    }
}

@Composable
private fun CalendarGrid(
    monthInfo: MonthInfo?,
    selectedDate: LocalDate?,
    holidays: List<JewishHoliday>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val hebrewDays = listOf("ש", "ו", "ה", "ד", "ג", "ב", "א")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day headers (reversed for RTL - Saturday to Sunday)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            hebrewDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (day == "ש") Secondary else Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days
        monthInfo?.let { info ->
            val firstDayOfMonth = LocalDate.of(info.gregorianYear, info.gregorianMonth, 1)
            val daysInMonth = firstDayOfMonth.lengthOfMonth()
            val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

            // Calculate leading empty cells (reversed for RTL)
            val leadingEmptyCells = (6 - startDayOfWeek + 1) % 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false
            ) {
                // Empty cells at start
                items(leadingEmptyCells) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                // Days (in reverse order for RTL)
                items(daysInMonth) { index ->
                    val dayOfMonth = daysInMonth - index
                    val date = LocalDate.of(info.gregorianYear, info.gregorianMonth, dayOfMonth)
                    val dayInfo = info.days.getOrNull(dayOfMonth - 1)
                    val holiday = holidays.find { it.date == date }
                    val isToday = date == today
                    val isSelected = date == selectedDate
                    val isShabbat = date.dayOfWeek == DayOfWeek.SATURDAY

                    CalendarDayCell(
                        gregorianDay = dayOfMonth,
                        hebrewDay = dayInfo?.hebrewDate?.day,
                        isToday = isToday,
                        isSelected = isSelected,
                        isShabbat = isShabbat,
                        holiday = holiday,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    gregorianDay: Int,
    hebrewDay: Int?,
    isToday: Boolean,
    isSelected: Boolean,
    isShabbat: Boolean,
    holiday: JewishHoliday?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Secondary
        isToday -> Secondary.copy(alpha = 0.3f)
        holiday?.yomTov == true -> Color(0xFFFFD700).copy(alpha = 0.2f)
        isShabbat -> Color.White.copy(alpha = 0.1f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isShabbat -> Secondary
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected)
                    Modifier.border(2.dp, Secondary, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = gregorianDay.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            hebrewDay?.let {
                Text(
                    text = numberToHebrewGematria(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            holiday?.let {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Secondary)
                )
            }
        }
    }
}

@Composable
private fun DayDetailPanel(
    dayInfo: DayInfo,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dayInfo.gregorianDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("he"))),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = dayInfo.hebrewDate.toHebrewString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Secondary
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "סגור", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parasha
        dayInfo.parasha?.let { parasha ->
            InfoCard(
                icon = Icons.Default.MenuBook,
                title = "פרשת השבוע",
                content = parasha
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Holidays
        if (dayInfo.holidays.isNotEmpty()) {
            dayInfo.holidays.forEach { holiday ->
                InfoCard(
                    icon = Icons.Default.Celebration,
                    title = holiday.category.name,
                    content = holiday.nameHebrew
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Events
        if (dayInfo.events.isNotEmpty()) {
            Text(
                text = "אירועים",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyColumn {
                items(dayInfo.events) { event ->
                    EventItem(event = event)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } else if (dayInfo.holidays.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "אין אירועים",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun UpcomingPanel(
    upcomingEvents: List<CalendarEvent>,
    nextHoliday: JewishHoliday?,
    nextParasha: Pair<LocalDate, String>?
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Next Parasha
        nextParasha?.let { (date, parasha) ->
            item {
                Text(
                    text = "פרשת השבוע",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                InfoCard(
                    icon = Icons.Default.MenuBook,
                    title = date.format(DateTimeFormatter.ofPattern("EEEE d/M", Locale("he"))),
                    content = parasha
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Next Holiday
        nextHoliday?.let { holiday ->
            item {
                Text(
                    text = "החג הקרוב",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                InfoCard(
                    icon = Icons.Default.Celebration,
                    title = holiday.date.format(DateTimeFormatter.ofPattern("d/M/yyyy")),
                    content = holiday.nameHebrew
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Upcoming Events
        if (upcomingEvents.isNotEmpty()) {
            item {
                Text(
                    text = "אירועים קרובים",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(upcomingEvents.take(10)) { event ->
                EventItem(event = event)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        if (upcomingEvents.isEmpty() && nextHoliday == null && nextParasha == null) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "אין אירועים קרובים",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Secondary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EventItem(event: CalendarEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(event.color))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (event.isAllDay) "כל היום" else {
                    "${event.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${event.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// Helper functions for Hebrew numerals
private fun numberToHebrewGematria(num: Int): String {
    val ones = arrayOf("", "א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט")
    val tens = arrayOf("", "י", "כ", "ל")

    return when {
        num == 15 -> "ט״ו"
        num == 16 -> "ט״ז"
        num <= 10 -> ones[num]
        num < 30 -> "${tens[num / 10]}${ones[num % 10]}".let {
            if (it.length > 1) "${it.dropLast(1)}״${it.last()}" else it
        }
        else -> num.toString()
    }
}

private fun numberToHebrewYear(year: Int): String {
    // Simplified - just show last 3 digits in Hebrew
    val shortYear = year % 1000
    return "ה'${numberToHebrewHundreds(shortYear)}"
}

private fun numberToHebrewHundreds(num: Int): String {
    val hundreds = arrayOf("", "ק", "ר", "ש", "ת", "תק", "תר", "תש", "תת", "תתק")
    val tens = arrayOf("", "י", "כ", "ל", "מ", "נ", "ס", "ע", "פ", "צ")
    val ones = arrayOf("", "א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט")

    val h = num / 100
    val t = (num % 100) / 10
    val o = num % 10

    // Handle 15 and 16
    val tensOnes = when (num % 100) {
        15 -> "טו"
        16 -> "טז"
        else -> "${tens[t]}${ones[o]}"
    }

    return "${hundreds.getOrElse(h) { "" }}$tensOnes"
}
