package com.example.aicourse.domain.tools.tokenComparePrevious

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.ToolResult

class TokenCompareManager : Tool<Message> {

    private var previousProcessedMessage: Message? = null

    override fun processData(processData: Message): ToolResult {
        val tokenUsageDiff = TokenStatisticsCalculator.calculateDiff(current = processData, previous = previousProcessedMessage)
        previousProcessedMessage = processData
        return tokenUsageDiff
    }

    override fun clear() {
        previousProcessedMessage = null
    }
}