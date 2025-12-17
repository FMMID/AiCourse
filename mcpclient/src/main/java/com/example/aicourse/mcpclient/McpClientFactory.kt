package com.example.aicourse.mcpclient

object McpClientFactory {

    fun createMcpClient(mcpClientType: McpClientType): McpClient {
        return RemoteMcpClientService(mcpClientType)
    }
}