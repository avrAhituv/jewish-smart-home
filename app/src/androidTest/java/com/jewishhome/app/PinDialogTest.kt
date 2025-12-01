package com.jewishhome.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jewishhome.app.kiosk.PinDialog
import com.jewishhome.app.presentation.theme.JewishHomeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pinDialog_displaysTitle() {
        composeTestRule.setContent {
            JewishHomeTheme {
                PinDialog(
                    title = "הזן קוד PIN",
                    onPinEntered = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("הזן קוד PIN").assertIsDisplayed()
    }

    @Test
    fun pinDialog_displaysKeypad() {
        composeTestRule.setContent {
            JewishHomeTheme {
                PinDialog(
                    title = "Test",
                    onPinEntered = {},
                    onDismiss = {}
                )
            }
        }

        // Check that number keys are displayed
        for (i in 0..9) {
            composeTestRule.onNodeWithText(i.toString()).assertIsDisplayed()
        }
    }

    @Test
    fun pinDialog_enteringPinTriggerCallback() {
        var enteredPin = ""

        composeTestRule.setContent {
            JewishHomeTheme {
                PinDialog(
                    title = "Test",
                    pinLength = 4,
                    onPinEntered = { enteredPin = it },
                    onDismiss = {}
                )
            }
        }

        // Enter 1234
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("4").performClick()

        assert(enteredPin == "1234")
    }

    @Test
    fun pinDialog_closeButtonDismisses() {
        var dismissed = false

        composeTestRule.setContent {
            JewishHomeTheme {
                PinDialog(
                    title = "Test",
                    onPinEntered = {},
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("סגור").performClick()
        assert(dismissed)
    }

    @Test
    fun pinDialog_showsErrorState() {
        composeTestRule.setContent {
            JewishHomeTheme {
                PinDialog(
                    title = "Test",
                    onPinEntered = {},
                    onDismiss = {},
                    isError = true
                )
            }
        }

        composeTestRule.onNodeWithText("קוד שגוי, נסה שוב").assertIsDisplayed()
    }
}
