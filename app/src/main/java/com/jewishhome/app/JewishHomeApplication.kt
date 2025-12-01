package com.jewishhome.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JewishHomeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
