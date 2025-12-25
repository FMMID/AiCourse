package com.example.aicourse.domain.chat.promt.ragAssistant

import android.content.Context
import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextResponse
import com.example.aicourse.domain.utils.ResourceReader
import com.example.aicourse.rag.domain.model.DocumentChunk

class RagAssistantPrompt(
    override val temperature: Float = 0.7f,
    override val topP: Float = 0.1f,
    override val maxTokens: Int = 1024,
) : DynamicSystemPrompt<PlainTextResponse> {

    var ragDocumentChunks: List<DocumentChunk> = emptyList()

    override fun loadSystemPrompt(context: Context): String? {
        val baseSystemPrompt = ResourceReader.readRawResource(context, R.raw.rag_system_prompt)
        val formattedChunks = ragDocumentChunks.joinToString("\n\n") { chunk ->
            """
                [Источник: ${chunk.source}]
                Текст: ${chunk.text}
            """.trimIndent()
        }

        // Инструкция для модели (можно добавить прямо здесь или в txt файле)
        val citationInstruction = "\n\nИспользуй предоставленный контекст для ответа. " +
                "В конце ответа ОБЯЗАТЕЛЬНО перечисли источники, которые ты использовал, в формате: 'Источники: [Название файла]'."

        return baseSystemPrompt + citationInstruction + "\n\n=== КОНТЕКСТ ===\n" + formattedChunks
    }

    override fun matches(message: String): Boolean = true

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawContent = rawResponse)
    }
}
