package com.example.aicourse.domain.chat.model

import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.tools.ToolResult

//TODO временное решение, потом удалить, как будет нормальное управление состоянием
data class ComplexBotMessage(
    val message: Message,
    val activePrompt: SystemPrompt<*>,
    val toolResult: ToolResult?
)