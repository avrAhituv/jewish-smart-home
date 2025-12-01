package com.jewishhome.app.presentation.navigation

import androidx.compose.runtime.Composable
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

sealed class Screen(val route: String) {
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
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
}
