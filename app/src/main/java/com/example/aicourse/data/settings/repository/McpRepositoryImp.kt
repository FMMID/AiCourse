package com.example.aicourse.data.settings.repository

import com.example.aicourse.data.settings.remote.McpRemoteDataSource
import com.example.aicourse.domain.settings.repository.McpRepository
import com.example.aicourse.mcpclient.McpClientType
import io.modelcontextprotocol.kotlin.sdk.types.Tool

class McpRepositoryImp(
    private val mcpRemoteDataSource: McpRemoteDataSource
) : McpRepository {

    override suspend fun getAvailableTools(mcpClientType: McpClientType): List<Tool> {
        return mcpRemoteDataSource.getAvailableTools(mcpClientType)
    }

    override suspend fun shutdown(mcpClientType: McpClientType) {
        mcpRemoteDataSource.shutdown(mcpClientType)
    }
}
