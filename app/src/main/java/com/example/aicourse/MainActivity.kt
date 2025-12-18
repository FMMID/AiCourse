package com.example.aicourse

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aicourse.data.notifications.PushTokenManager
import com.example.aicourse.presentation.chat.ChatScreen
import com.example.aicourse.ui.theme.AiCourseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = "unknown_user"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        PushTokenManager.register(userId)

        enableEdgeToEdge()
        setContent {
            /**
             * TODO Добавить систему навигации, реализовать навигацию между ChatScreen -> SettingsScreen
             * TODO Начальный экран при входе в приложение - ChatScreen
             */
            AiCourseTheme {
                ChatScreen()
            }
        }
    }
}