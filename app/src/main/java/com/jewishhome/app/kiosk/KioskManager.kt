package com.jewishhome.app.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService
import com.jewishhome.app.data.local.preferences.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KioskManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {
    private val devicePolicyManager: DevicePolicyManager? =
        context.getSystemService()

    private val activityManager: ActivityManager? =
        context.getSystemService()

    private val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)

    /**
     * Check if device admin is active
     */
    fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager?.isAdminActive(adminComponent) == true
    }

    /**
     * Check if device owner
     */
    fun isDeviceOwner(): Boolean {
        return devicePolicyManager?.isDeviceOwnerApp(context.packageName) == true
    }

    /**
     * Check if currently in lock task mode
     */
    fun isInLockTaskMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activityManager?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            @Suppress("DEPRECATION")
            activityManager?.isInLockTaskMode == true
        }
    }

    /**
     * Start lock task mode (kiosk mode)
     */
    fun startLockTaskMode(activity: Activity) {
        if (isDeviceOwner()) {
            // If device owner, we can whitelist the app first
            devicePolicyManager?.setLockTaskPackages(
                adminComponent,
                arrayOf(context.packageName)
            )
        }

        try {
            activity.startLockTask()
        } catch (e: Exception) {
            // Lock task not allowed - need device owner or user confirmation
            e.printStackTrace()
        }
    }

    /**
     * Stop lock task mode
     */
    fun stopLockTaskMode(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Request to enable device admin
     */
    fun requestDeviceAdmin(activity: Activity, requestCode: Int) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "מצב קיוסק דורש הרשאות ניהול מכשיר"
            )
        }
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Validate PIN code
     */
    suspend fun validatePin(enteredPin: String): Boolean {
        val storedPin = userPreferences.pinCode.first()
        return enteredPin == storedPin
    }

    /**
     * Set lock task features (for device owner)
     */
    fun setLockTaskFeatures() {
        if (isDeviceOwner() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            devicePolicyManager?.setLockTaskFeatures(
                adminComponent,
                DevicePolicyManager.LOCK_TASK_FEATURE_NONE
            )
        }
    }

    /**
     * Enable kiosk mode based on settings
     */
    suspend fun enableKioskMode(activity: Activity) {
        val kioskMode = userPreferences.kioskMode.first()
        when (kioskMode) {
            "lock_task" -> startLockTaskMode(activity)
            "device_owner" -> {
                if (isDeviceOwner()) {
                    setLockTaskFeatures()
                    startLockTaskMode(activity)
                }
            }
        }
    }

    /**
     * Disable kiosk mode
     */
    fun disableKioskMode(activity: Activity) {
        if (isInLockTaskMode()) {
            stopLockTaskMode(activity)
        }
    }

    companion object {
        const val REQUEST_CODE_ENABLE_ADMIN = 1001
    }
}
