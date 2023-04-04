package ru.tn.shinglass.application

import android.app.Application
import ru.tn.shinglass.auth.AppAuth

class ShinglassApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppAuth.initApp()
    }


}