package com.example.aicourse.mcpclient.data

import com.example.aicourse.mcpclient.McpClientConfig
import com.example.aicourse.mcpclient.domain.McpRepository
import io.modelcontextprotocol.kotlin.sdk.types.Tool

class McpRepositoryImp(
    private val mcpRemoteDataSource: McpRemoteDataSource
) : McpRepository {

    override suspend fun getAvailableTools(mcpClientConfig: McpClientConfig): List<Tool> {
        return mcpRemoteDataSource.getAvailableTools(mcpClientConfig)
    }

    override suspend fun shutdown(mcpClientConfig: McpClientConfig) {
        mcpRemoteDataSource.shutdown(mcpClientConfig)
    }
}