package com.example.aicourse.data.settings.remote

import com.example.aicourse.mcpclient.McpClient
import com.example.aicourse.mcpclient.McpClientConfig
import com.example.aicourse.mcpclient.McpClientFactory
import io.modelcontextprotocol.kotlin.sdk.types.Tool

class McpRemoteDataSource {

    val mapOfMcpClients = mutableMapOf<McpClientConfig, McpClient>()

    suspend fun getAvailableTools(mcpClientConfig: McpClientConfig): List<Tool> {
        return getOrInitMcpClient(mcpClientConfig).getTools()
    }

    suspend fun shutdown(mcpClientConfig: McpClientConfig) {
        mapOfMcpClients[mcpClientConfig]?.shutdown()
    }

    private suspend fun getOrInitMcpClient(mcpClientConfig: McpClientConfig): McpClient {
        return mapOfMcpClients.getOrPut(mcpClientConfig) {
            val mcpClient = McpClientFactory.createMcpClient(mcpClientConfig)
            mcpClient.connect()
            mcpClient
        }
    }
}