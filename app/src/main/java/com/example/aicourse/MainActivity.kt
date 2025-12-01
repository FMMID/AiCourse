package com.example.aicourse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aicourse.presentation.chat.ChatScreen
import com.example.aicourse.ui.theme.AiCourseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiCourseTheme {
                ChatScreen()
            }
        }
    }
}