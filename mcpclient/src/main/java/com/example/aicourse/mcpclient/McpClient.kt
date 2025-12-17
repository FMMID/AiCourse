package com.example.aicourse.mcpclient

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Tool

interface McpClient {

    suspend fun connect()

    suspend fun getTools(): List<Tool>

    suspend fun callTool(name: String, arguments: Map<String, Any?>): CallToolResult

    fun shutdown()
}