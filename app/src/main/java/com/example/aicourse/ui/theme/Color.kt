package com.example.aicourse.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Цвета для отображения статистики использования токенов
 */
object TokenUsageColors {
    /** Цвет для input tokens (prompt tokens) */
    val InputTokens = Color(0xFF2196F3) // Material Blue

    /** Цвет для output tokens (completion tokens) */
    val OutputTokens = Color(0xFF4CAF50) // Material Green

    /** Цвет для свободного места (free space) */
    val FreeSpace = Color(0xFFBDBDBD) // Material Grey 400
}