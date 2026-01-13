package com.example.aicourse.domain.chat.strategy.model

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.prompt.SystemPrompt
import com.example.aicourse.domain.tools.ToolResult

sealed interface DataForReceive {

    val toolResult: ToolResult?

    class Simple(
        val message: Message,
        val activePrompt: SystemPrompt<*>,
        override val toolResult: ToolResult? = null
    ) : DataForReceive
}