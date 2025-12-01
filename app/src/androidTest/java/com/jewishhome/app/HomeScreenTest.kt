package com.jewishhome.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jewishhome.app.presentation.screens.home.HomeScreen
import com.jewishhome.app.presentation.theme.JewishHomeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysAllAppButtons() {
        composeTestRule.setContent {
            JewishHomeTheme {
                HomeScreen(
                    onNavigateToZmanim = {},
                    onNavigateToMusic = {},
                    onNavigateToContacts = {},
                    onNavigateToCalendar = {},
                    onNavigateToRecipes = {},
                    onNavigateToTexts = {},
                    onNavigateToPhotos = {},
                    onNavigateToSettings = {},
                    onNavigateToScreensaver = {}
                )
            }
        }

        // Check that all app buttons are displayed
        composeTestRule.onNodeWithText("זמנים").assertIsDisplayed()
        composeTestRule.onNodeWithText("נגן").assertIsDisplayed()
        composeTestRule.onNodeWithText("אלפון").assertIsDisplayed()
        composeTestRule.onNodeWithText("לוח שנה").assertIsDisplayed()
        composeTestRule.onNodeWithText("מתכונים").assertIsDisplayed()
        composeTestRule.onNodeWithText("ברכות").assertIsDisplayed()
        composeTestRule.onNodeWithText("תמונות").assertIsDisplayed()
        composeTestRule.onNodeWithText("הגדרות").assertIsDisplayed()
    }

    @Test
    fun homeScreen_zmanimButtonClick_triggersNavigation() {
        var navigated = false

        composeTestRule.setContent {
            JewishHomeTheme {
                HomeScreen(
                    onNavigateToZmanim = { navigated = true },
                    onNavigateToMusic = {},
                    onNavigateToContacts = {},
                    onNavigateToCalendar = {},
                    onNavigateToRecipes = {},
                    onNavigateToTexts = {},
                    onNavigateToPhotos = {},
                    onNavigateToSettings = {},
                    onNavigateToScreensaver = {}
                )
            }
        }

        composeTestRule.onNodeWithText("זמנים").performClick()
        assert(navigated)
    }

    @Test
    fun homeScreen_settingsButtonClick_triggersNavigation() {
        var navigated = false

        composeTestRule.setContent {
            JewishHomeTheme {
                HomeScreen(
                    onNavigateToZmanim = {},
                    onNavigateToMusic = {},
                    onNavigateToContacts = {},
                    onNavigateToCalendar = {},
                    onNavigateToRecipes = {},
                    onNavigateToTexts = {},
                    onNavigateToPhotos = {},
                    onNavigateToSettings = { navigated = true },
                    onNavigateToScreensaver = {}
                )
            }
        }

        composeTestRule.onNodeWithText("הגדרות").performClick()
        assert(navigated)
    }
}
