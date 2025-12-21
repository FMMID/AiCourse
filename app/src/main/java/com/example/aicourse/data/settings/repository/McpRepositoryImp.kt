package com.example.aicourse.data.settings.repository

import com.example.aicourse.data.settings.remote.McpRemoteDataSource
import com.example.aicourse.domain.settings.repository.McpRepository
import com.example.aicourse.mcpclient.McpClientConfig
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
