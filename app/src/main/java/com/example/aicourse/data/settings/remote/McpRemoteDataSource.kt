package com.example.aicourse.data.settings.remote

import com.example.aicourse.mcpclient.McpClient
import com.example.aicourse.mcpclient.McpClientFactory
import com.example.aicourse.mcpclient.McpClientType
import io.modelcontextprotocol.kotlin.sdk.types.Tool

class McpRemoteDataSource {

    val mapOfMcpClients = mutableMapOf<McpClientType, McpClient>()

    suspend fun getAvailableTools(mcpClientType: McpClientType): List<Tool> {
        return getOrInitMcpClient(mcpClientType).getTools()
    }

    suspend fun shutdown(mcpClientType: McpClientType) {
        mapOfMcpClients[mcpClientType]?.shutdown()
    }

    private suspend fun getOrInitMcpClient(mcpClientType: McpClientType): McpClient {
        return mapOfMcpClients.getOrPut(mcpClientType) {
            val mcpClient = McpClientFactory.createMcpClient(mcpClientType)
            mcpClient.connect()
            mcpClient
        }
    }
}