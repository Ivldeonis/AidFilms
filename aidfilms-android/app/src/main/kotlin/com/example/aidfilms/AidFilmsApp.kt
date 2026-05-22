package com.example.aidfilms

import android.app.Application
import com.example.aidfilms.utils.FileLogger

class AidFilmsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FileLogger.setup(this)
    }
}
