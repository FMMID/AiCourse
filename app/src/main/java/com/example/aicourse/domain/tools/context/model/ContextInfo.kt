package com.example.aicourse.domain.tools.context.model

data class ContextInfo(
    val sizeOfSummaryMessages: Int,
    val sizeOfActiveMessages: Int,
    val sizeOfSystemPrompt: Int
)