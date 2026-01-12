package com.example.aicourse.domain.settings.usecase

import com.example.aicourse.core.BaseUseCase
import com.example.aicourse.mcpclient.domain.McpRepository
import com.example.aicourse.mcpclient.McpClientConfig
import io.modelcontextprotocol.kotlin.sdk.types.Tool

class GetLocalMcpToolsUseCase(
    private val mcpRepositoryImp: McpRepository,
) : BaseUseCase<McpClientConfig, List<Tool>> {

    override suspend fun invoke(input: McpClientConfig): Result<List<Tool>> {
        return Result.success(mcpRepositoryImp.getAvailableTools(input))
    }
}
