package com.example.aicourse.domain.tools.modelInfo

import com.example.aicourse.domain.chat.repository.SendMessageResult
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.modelInfo.model.ModelInfo

class ModelInfoManager : Tool<SendMessageResult> {

    override fun processData(processData: SendMessageResult): ToolResult {
        return ModelInfo(processData.modelName)
    }

    override fun clear() = Unit
}