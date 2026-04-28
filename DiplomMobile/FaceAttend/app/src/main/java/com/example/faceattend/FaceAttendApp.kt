package com.example.faceattend

import android.app.Application
import com.example.faceattend.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class FaceAttendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FaceAttendApp)
            modules(appModule)
        }
    }
}