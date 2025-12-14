package com.example.aicourse.domain.tools.modelInfo.model

import com.example.aicourse.domain.tools.ToolResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("model_info")
data class ModelInfo(
    val modelName: String?
) : ToolResult