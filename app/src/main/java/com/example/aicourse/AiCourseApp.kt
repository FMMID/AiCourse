package com.example.aicourse

import android.app.Application
import com.example.aicourse.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AiCourseApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AiCourseApp)
            modules(appModule)
        }
    }
}