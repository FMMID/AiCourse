package com.example.aicourse.prompt.personalAssistant

import com.example.aicourse.prompt.R
import com.example.aicourse.prompt.StaticSystemPrompt
import com.example.aicourse.prompt.plain.PlainTextResponse

class PersonalAssistantPrompt(
    override val temperature: Float = 0.7f,
    override val topP: Float = 0.1f,
    override val maxTokens: Int = 1024,
    override val contentResourceId: Int = R.raw.personal_assistant_prompt,
) : StaticSystemPrompt<PlainTextResponse> {

    override fun matches(message: String): Boolean = false

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawContent = rawResponse)
    }
}
