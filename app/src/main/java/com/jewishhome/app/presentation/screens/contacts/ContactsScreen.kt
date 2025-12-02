package com.jewishhome.app.presentation.screens.contacts

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jewishhome.app.domain.model.Contact
import com.jewishhome.app.domain.model.ContactGroup
import com.jewishhome.app.domain.model.PhoneType
import com.jewishhome.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
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
            title = { Text("אלפון", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.showDialpad(!uiState.showDialpad) }) {
                    Icon(
                        if (uiState.showDialpad) Icons.Default.Contacts else Icons.Default.Dialpad,
                        contentDescription = if (uiState.showDialpad) "אנשי קשר" else "חייגן",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { viewModel.syncContacts() }) {
                    Icon(Icons.Default.Sync, "סנכרן", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (uiState.showDialpad) {
            DialpadView(
                number = uiState.dialpadNumber,
                onNumberChange = { viewModel.updateDialpadNumber(it) },
                onCall = { viewModel.callNumber(uiState.dialpadNumber) }
            )
        } else {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.search(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs
            ContactsTabs(
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
                    uiState.contacts.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.PersonOff,
                            title = "אין אנשי קשר",
                            subtitle = "סנכרן את אנשי הקשר מהמכשיר"
                        )
                    }
                    else -> {
                        when (uiState.selectedTab) {
                            ContactsTab.CONTACTS -> ContactsList(
                                contacts = uiState.filteredContacts,
                                onContactClick = { viewModel.selectContact(it) }
                            )
                            ContactsTab.FAVORITES -> ContactsList(
                                contacts = uiState.favorites,
                                onContactClick = { viewModel.selectContact(it) }
                            )
                            ContactsTab.RECENT -> ContactsList(
                                contacts = uiState.recentContacts,
                                onContactClick = { viewModel.selectContact(it) }
                            )
                            ContactsTab.GROUPS -> GroupsList(
                                groups = uiState.groups,
                                onGroupClick = { viewModel.selectGroup(it) }
                            )
                            ContactsTab.SPEED_DIAL -> SpeedDialGrid(
                                speedDials = uiState.speedDials,
                                contacts = uiState.contacts,
                                onSpeedDialClick = { phoneNumber ->
                                    viewModel.callNumber(phoneNumber)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Contact Detail Dialog
    uiState.selectedContact?.let { contact ->
        ContactDetailDialog(
            contact = contact,
            onDismiss = { viewModel.clearSelectedContact() },
            onCall = { number -> viewModel.callNumber(number, contact) }
        )
    }

    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar then clear
            viewModel.clearError()
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
        placeholder = { Text("חפש איש קשר...", color = Color.White.copy(alpha = 0.5f)) },
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
private fun ContactsTabs(
    selectedTab: ContactsTab,
    onTabSelected: (ContactsTab) -> Unit
) {
    val tabs = listOf(
        ContactsTab.CONTACTS to "הכל",
        ContactsTab.FAVORITES to "מועדפים",
        ContactsTab.RECENT to "אחרונים",
        ContactsTab.GROUPS to "קבוצות",
        ContactsTab.SPEED_DIAL to "חיוג מהיר"
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
private fun ContactsList(
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            ContactItem(
                contact = contact,
                onClick = { onContactClick(contact) }
            )
        }
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Secondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = contact.displayName.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Secondary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Contact Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (contact.isFavorite) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            contact.phoneNumbers.firstOrNull()?.let { phone ->
                Text(
                    text = phone.number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Call button
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Secondary.copy(alpha = 0.2f))
        ) {
            Icon(
                Icons.Default.Call,
                contentDescription = "התקשר",
                tint = Secondary
            )
        }
    }
}

@Composable
private fun GroupsList(
    groups: List<ContactGroup>,
    onGroupClick: (ContactGroup) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onGroupClick(group) }
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
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = Secondary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SpeedDialGrid(
    speedDials: List<com.jewishhome.app.domain.model.SpeedDial>,
    contacts: List<Contact>,
    onSpeedDialClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items((1..9).toList()) { position ->
            val speedDial = speedDials.find { it.position == position }
            val contact = speedDial?.let { sd ->
                contacts.find { it.id == sd.contactId }
            }

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (contact != null) Secondary.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .clickable(enabled = speedDial != null) {
                        speedDial?.phoneNumber?.let { onSpeedDialClick(it) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (contact != null) Secondary else Color.White.copy(alpha = 0.3f)
                    )
                    if (contact != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contact.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialpadView(
    number: String,
    onNumberChange: (String) -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display
        Text(
            text = number.ifEmpty { "הקלד מספר" },
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
            color = if (number.isEmpty()) Color.White.copy(alpha = 0.5f) else Color.White,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Dialpad Grid
        val dialpadButtons = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        dialpadButtons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (digit, letters) ->
                    DialpadButton(
                        digit = digit,
                        letters = letters,
                        onClick = { onNumberChange(number + digit) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Backspace
            IconButton(
                onClick = { if (number.isNotEmpty()) onNumberChange(number.dropLast(1)) },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.Backspace,
                    contentDescription = "מחק",
                    tint = Color.White
                )
            }

            // Call Button
            IconButton(
                onClick = onCall,
                enabled = number.isNotEmpty(),
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (number.isNotEmpty()) Color(0xFF4CAF50) else Color.Gray)
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "התקשר",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Clear
            IconButton(
                onClick = { onNumberChange("") },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "נקה",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun DialpadButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ContactDetailDialog(
    contact: Contact,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Primary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (contact.photoUri != null) {
                        AsyncImage(
                            model = contact.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = contact.displayName.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = Secondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = contact.displayName,
                    color = Color.White
                )
            }
        },
        text = {
            Column {
                contact.phoneNumbers.forEach { phone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCall(phone.number) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (phone.type) {
                                PhoneType.MOBILE -> Icons.Default.PhoneAndroid
                                PhoneType.HOME -> Icons.Default.Home
                                PhoneType.WORK -> Icons.Default.Work
                                PhoneType.OTHER -> Icons.Default.Phone
                            },
                            contentDescription = null,
                            tint = Secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = phone.number,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Text(
                                text = when (phone.type) {
                                    PhoneType.MOBILE -> "נייד"
                                    PhoneType.HOME -> "בית"
                                    PhoneType.WORK -> "עבודה"
                                    PhoneType.OTHER -> phone.label ?: "אחר"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "התקשר",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("סגור", color = Secondary)
            }
        }
    )
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
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
