package com.example.aicourse.domain.settings.repository

import com.example.aicourse.mcpclient.McpClientType
import io.modelcontextprotocol.kotlin.sdk.types.Tool

interface McpRepository {

    suspend fun getAvailableTools(mcpClientType: McpClientType): List<Tool>

    suspend fun shutdown(mcpClientType: McpClientType)
}