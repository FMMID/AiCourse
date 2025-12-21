package com.example.aicourse.mcpclient

object McpClientFactory {

    fun createMcpClient(mcpClientConfig: McpClientConfig): McpClient {
        return RemoteMcpClientService(mcpClientConfig)
    }
}