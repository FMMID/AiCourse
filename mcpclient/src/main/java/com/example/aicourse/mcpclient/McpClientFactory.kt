package com.example.aicourse.mcpclient

object McpClientFactory {

    fun createMcpClient(mcpClientType: McpClientType): McpClient {
        return when (mcpClientType) {
            McpClientType.LOCAL_WEATHER -> LocalWeatherMcpClientService(mcpClientType)
        }
    }
}