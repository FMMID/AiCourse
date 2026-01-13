package com.example.aicourse.di.config

import com.example.aicourse.BuildConfig

/**
 * Конфигурация API ключей и URLs
 * Использует BuildConfig для доступа к ключам из local.properties
 */
data class ApiConfig(
    val gigaChatAuthKey: String = BuildConfig.GIGACHAT_AUTH_KEY,
    val huggingFaceAuthKey: String = BuildConfig.HUGGING_FACE_AUTH_KEY,
    val mcpNoteUrl: String = BuildConfig.MCP_NOTE_URL,
    val mcpNotificationUrl: String = BuildConfig.MCP_NOTIFICATION_URL,
    val registerFmcTokenUrl: String = BuildConfig.REGISTER_FMC_TOKEN_URL,
    val mcpGitUrl: String = "http://10.0.2.2:3000/sse",
)
