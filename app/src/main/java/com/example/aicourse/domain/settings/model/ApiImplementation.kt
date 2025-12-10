package com.example.aicourse.domain.settings.model

import com.example.aicourse.BuildConfig

enum class ApiImplementation(val rawName: String, val key: String) {
    GIGA_CHAT(rawName = "GigaChat", key = BuildConfig.GIGACHAT_AUTH_KEY),
    HUGGING_FACE(rawName = "HuggingFace", key = BuildConfig.HUGGING_FACE_AUTH_KEY)
}