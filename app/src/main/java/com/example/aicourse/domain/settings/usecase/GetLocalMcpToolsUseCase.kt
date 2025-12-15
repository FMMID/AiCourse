package com.example.aicourse.domain.settings.usecase

import com.example.aicourse.domain.base.BaseUseCase
import com.example.aicourse.domain.settings.repository.McpRepository
import com.example.aicourse.mcpclient.McpClientType
import io.modelcontextprotocol.kotlin.sdk.types.Tool

class GetLocalMcpToolsUseCase(
    private val mcpRepositoryImp: McpRepository,
) : BaseUseCase<McpClientType, List<Tool>> {

    override suspend fun invoke(input: McpClientType): Result<List<Tool>> {
        return Result.success(mcpRepositoryImp.getAvailableTools(input))
    }
}
