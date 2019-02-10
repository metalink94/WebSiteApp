package ru.lopav.kzn.websiteapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class WebApp: Application() {

    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        super.onCreate()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
    }
}