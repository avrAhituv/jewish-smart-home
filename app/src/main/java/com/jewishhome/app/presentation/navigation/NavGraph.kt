package com.jewishhome.app.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jewishhome.app.presentation.screens.home.HomeScreen
import com.jewishhome.app.presentation.screens.zmanim.ZmanimScreen
import com.jewishhome.app.presentation.screens.music.MusicScreen
import com.jewishhome.app.presentation.screens.contacts.ContactsScreen
import com.jewishhome.app.presentation.screens.calendar.CalendarScreen
import com.jewishhome.app.presentation.screens.recipes.RecipesScreen
import com.jewishhome.app.presentation.screens.texts.TextsScreen
import com.jewishhome.app.presentation.screens.photos.PhotosScreen
import com.jewishhome.app.presentation.screens.settings.SettingsScreen
import com.jewishhome.app.presentation.screens.screensaver.ScreensaverScreen
import com.jewishhome.app.presentation.screens.permissions.PermissionsScreen

sealed class Screen(val route: String) {
    data object Permissions : Screen("permissions")
    data object Home : Screen("home")
    data object Zmanim : Screen("zmanim")
    data object Music : Screen("music")
    data object Contacts : Screen("contacts")
    data object Calendar : Screen("calendar")
    data object Recipes : Screen("recipes")
    data object Texts : Screen("texts")
    data object Photos : Screen("photos")
    data object Settings : Screen("settings")
    data object Screensaver : Screen("screensaver")
}

@Composable
fun JewishHomeNavHost(
    navController: NavHostController = rememberNavController(),
    showScreensaver: Boolean = false,
    onScreensaverDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    // Check if required permissions are granted
    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.READ_CONTACTS)
            add(Manifest.permission.READ_CALENDAR)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    val allPermissionsGranted = remember(requiredPermissions) {
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    val startDestination = if (allPermissionsGranted) {
        Screen.Home.route
    } else {
        Screen.Permissions.route
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main navigation content
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onAllPermissionsGranted = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Permissions.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToZmanim = { navController.navigate(Screen.Zmanim.route) },
                    onNavigateToMusic = { navController.navigate(Screen.Music.route) },
                    onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                    onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                    onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) },
                    onNavigateToTexts = { navController.navigate(Screen.Texts.route) },
                    onNavigateToPhotos = { navController.navigate(Screen.Photos.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToScreensaver = { navController.navigate(Screen.Screensaver.route) }
                )
            }

            composable(Screen.Zmanim.route) {
                ZmanimScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Music.route) {
                MusicScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Contacts.route) {
                ContactsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Recipes.route) {
                RecipesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Texts.route) {
                TextsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Photos.route) {
                PhotosScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Screensaver.route) {
                ScreensaverScreen(
                    onDismiss = { navController.popBackStack() }
                )
            }
        }

        // Auto-triggered screensaver overlay
        AnimatedVisibility(
            visible = showScreensaver,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ScreensaverScreen(
                onDismiss = onScreensaverDismiss
            )
        }
    }
}
