package com.jewishhome.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jewishhome.app.presentation.screens.settings.SettingsScreen
import com.jewishhome.app.presentation.theme.JewishHomeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysAllCategories() {
        composeTestRule.setContent {
            JewishHomeTheme {
                SettingsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Check that all settings categories are displayed
        composeTestRule.onNodeWithText("מיקום").assertIsDisplayed()
        composeTestRule.onNodeWithText("זמנים").assertIsDisplayed()
        composeTestRule.onNodeWithText("תצוגה").assertIsDisplayed()
        composeTestRule.onNodeWithText("שומר מסך").assertIsDisplayed()
        composeTestRule.onNodeWithText("טקסטים").assertIsDisplayed()
        composeTestRule.onNodeWithText("מצב קיוסק").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hasBackButton() {
        composeTestRule.setContent {
            JewishHomeTheme {
                SettingsScreen(
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("חזרה").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_backButtonClick_triggersNavigation() {
        var backPressed = false

        composeTestRule.setContent {
            JewishHomeTheme {
                SettingsScreen(
                    onNavigateBack = { backPressed = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("חזרה").performClick()
        assert(backPressed)
    }
}
