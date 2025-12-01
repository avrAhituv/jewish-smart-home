package com.jewishhome.app

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.kiosk.KioskManager
import com.jewishhome.app.kiosk.PinDialog
import com.jewishhome.app.presentation.navigation.JewishHomeNavHost
import com.jewishhome.app.presentation.theme.JewishHomeTheme
import com.jewishhome.app.util.InactivityHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var kioskManager: KioskManager

    private lateinit var inactivityHandler: InactivityHandler

    private var kioskModeEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize inactivity handler
        inactivityHandler = InactivityHandler(lifecycleScope)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Hide system bars for kiosk-like experience
        hideSystemBars()

        // Load and apply screensaver settings
        lifecycleScope.launch {
            userPreferences.screensaverEnabled.collect { enabled ->
                inactivityHandler.setEnabled(enabled)
            }
        }
        lifecycleScope.launch {
            userPreferences.screensaverTimeout.collect { timeout ->
                inactivityHandler.setTimeoutMinutes(timeout)
            }
        }

        // Check if kiosk mode should be enabled
        lifecycleScope.launch {
            val kioskMode = userPreferences.kioskMode.first()
            if (kioskMode != "disabled") {
                kioskModeEnabled = true
                kioskManager.enableKioskMode(this@MainActivity)
            }
        }

        setContent {
            val shouldShowScreensaver by inactivityHandler.shouldShowScreensaver.collectAsState()
            var showPinDialog by remember { mutableStateOf(false) }
            var pinError by remember { mutableStateOf(false) }

            // Handle back press in kiosk mode
            BackHandler(enabled = kioskModeEnabled) {
                showPinDialog = true
            }

            JewishHomeTheme {
                // Force RTL layout for Hebrew
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            JewishHomeNavHost(
                                showScreensaver = shouldShowScreensaver,
                                onScreensaverDismiss = { inactivityHandler.dismissScreensaver() }
                            )
                        }

                        // PIN dialog for exiting kiosk mode
                        if (showPinDialog) {
                            PinDialog(
                                title = "יציאה ממצב קיוסק",
                                subtitle = "הזן את קוד ה-PIN כדי לצאת",
                                isError = pinError,
                                onPinEntered = { enteredPin ->
                                    lifecycleScope.launch {
                                        if (kioskManager.validatePin(enteredPin)) {
                                            kioskManager.disableKioskMode(this@MainActivity)
                                            kioskModeEnabled = false
                                            showPinDialog = false
                                            pinError = false
                                        } else {
                                            pinError = true
                                        }
                                    }
                                },
                                onDismiss = {
                                    showPinDialog = false
                                    pinError = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Reset inactivity timer on any touch
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            inactivityHandler.onUserActivity()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Block certain keys in kiosk mode
        if (kioskModeEnabled) {
            when (keyCode) {
                KeyEvent.KEYCODE_HOME,
                KeyEvent.KEYCODE_APP_SWITCH,
                KeyEvent.KEYCODE_MENU -> return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        inactivityHandler.start()
    }

    override fun onPause() {
        super.onPause()
        inactivityHandler.stop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }
}
