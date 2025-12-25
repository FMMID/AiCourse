package com.example.aicourse.domain.chat.promt.ragAssistant

import android.content.Context
import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextResponse
import com.example.aicourse.domain.utils.ResourceReader
import com.example.aicourse.rag.domain.model.DocumentChunk

class RagAssistantPrompt(
    override val temperature: Float = 0.6f,
    override val topP: Float = 0.2f,
    override val maxTokens: Int = 1024,
) : DynamicSystemPrompt<PlainTextResponse> {

    var ragDocumentChunks: List<DocumentChunk> = emptyList()

    override fun loadSystemPrompt(context: Context): String? {
        val rawTemplate = ResourceReader.readRawResource(context, R.raw.rag_system_prompt)

        val formattedContext = if (ragDocumentChunks.isNotEmpty()) {
            ragDocumentChunks.mapIndexed { index, chunk ->
                """
                [${index + 1}] Источник: ${chunk.source}
                ${chunk.text}
                """.trimIndent()
            }.joinToString(separator = "\n\n")
        } else {
            "Контекст отсутствует."
        }

        return rawTemplate.replace("{{CONTEXT}}", formattedContext)
    }

    override fun matches(message: String): Boolean = true

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawContent = rawResponse)
    }
}
