package com.example.aicourse.domain.chat.promt.adbManager

import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.StaticSystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextResponse

class AdbManagerPrompt(
    override val temperature: Float = 0.6f,
    override val topP: Float = 0.1f,
    override val maxTokens: Int = 1024,
    override val contentResourceId: Int = R.raw.personal_assistant_prompt,
) : StaticSystemPrompt<PlainTextResponse> {

    override fun matches(message: String): Boolean = false

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawContent = rawResponse)
    }
}
