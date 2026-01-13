package com.example.aicourse.mcpclient.domain

import com.example.aicourse.mcpclient.McpClientConfig
import io.modelcontextprotocol.kotlin.sdk.types.Tool

interface McpRepository {

    suspend fun getAvailableTools(mcpClientConfig: McpClientConfig): List<Tool>

    suspend fun shutdown(mcpClientConfig: McpClientConfig)
}