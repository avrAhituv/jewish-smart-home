package com.jewishhome.app.presentation.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.jewishhome.app.presentation.theme.*

data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isRequired: Boolean = true
)

@Composable
fun PermissionsScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current

    val permissions = remember {
        buildList {
            add(PermissionItem(
                permission = Manifest.permission.READ_CONTACTS,
                title = "אנשי קשר",
                description = "גישה לאנשי קשר להצגה באלפון",
                icon = Icons.Default.Contacts
            ))
            add(PermissionItem(
                permission = Manifest.permission.READ_CALENDAR,
                title = "לוח שנה",
                description = "גישה ללוח השנה להצגת אירועים",
                icon = Icons.Default.CalendarMonth
            ))
            add(PermissionItem(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                title = "מיקום",
                description = "לחישוב זמנים הלכתיים מדויקים",
                icon = Icons.Default.LocationOn
            ))
            add(PermissionItem(
                permission = Manifest.permission.CALL_PHONE,
                title = "שיחות טלפון",
                description = "לביצוע שיחות מהאלפון",
                icon = Icons.Default.Phone,
                isRequired = false
            ))

            // Media permissions based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(PermissionItem(
                    permission = Manifest.permission.READ_MEDIA_AUDIO,
                    title = "קבצי מוזיקה",
                    description = "גישה לשירים במכשיר",
                    icon = Icons.Default.MusicNote
                ))
                add(PermissionItem(
                    permission = Manifest.permission.READ_MEDIA_IMAGES,
                    title = "תמונות",
                    description = "גישה לגלריית התמונות",
                    icon = Icons.Default.Photo
                ))
            } else {
                add(PermissionItem(
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                    title = "אחסון",
                    description = "גישה למוזיקה ותמונות",
                    icon = Icons.Default.Storage
                ))
            }
        }
    }

    var permissionStates by remember {
        mutableStateOf(
            permissions.associate {
                it.permission to (ContextCompat.checkSelfPermission(context, it.permission) == PackageManager.PERMISSION_GRANTED)
            }
        )
    }

    val allRequiredGranted = permissions
        .filter { it.isRequired }
        .all { permissionStates[it.permission] == true }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionStates = permissionStates + results
    }

    LaunchedEffect(allRequiredGranted) {
        if (allRequiredGranted) {
            onAllPermissionsGranted()
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "הרשאות נדרשות",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                "האפליקציה צריכה את ההרשאות הבאות כדי לפעול כראוי",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permissions list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(permissions) { permission ->
                    PermissionCard(
                        item = permission,
                        isGranted = permissionStates[permission.permission] == true,
                        onRequest = {
                            launcher.launch(arrayOf(permission.permission))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val notGranted = permissions
                            .filter { permissionStates[it.permission] != true }
                            .map { it.permission }
                            .toTypedArray()
                        if (notGranted.isNotEmpty()) {
                            launcher.launch(notGranted)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("אשר הכל")
                }

                Button(
                    onClick = onAllPermissionsGranted,
                    modifier = Modifier.weight(1f),
                    enabled = allRequiredGranted,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Text("המשך")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowBack, null)
                }
            }

            if (!allRequiredGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "יש לאשר את כל ההרשאות הנדרשות כדי להמשיך",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItem,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                Secondary.copy(alpha = 0.2f)
            else
                SurfaceLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) Secondary.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = if (isGranted) Secondary else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    if (!item.isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(אופציונלי)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isGranted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "מאושר",
                    tint = Secondary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                FilledTonalButton(
                    onClick = onRequest,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Secondary.copy(alpha = 0.3f)
                    )
                ) {
                    Text("אשר", color = Color.White)
                }
            }
        }
    }
}
