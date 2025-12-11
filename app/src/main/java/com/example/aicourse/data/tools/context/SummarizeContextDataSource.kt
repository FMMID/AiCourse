package com.example.aicourse.data.tools.context

interface SummarizeContextDataSource {

    suspend fun summarizeContext(messageHistory: String): String
}