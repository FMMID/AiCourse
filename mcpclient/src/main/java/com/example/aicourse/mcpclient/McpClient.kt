package com.example.aicourse.mcpclient

import io.modelcontextprotocol.kotlin.sdk.types.Tool

interface McpClient {

    suspend fun connect()

    suspend fun getTools(): List<Tool>

    fun shutdown()
}