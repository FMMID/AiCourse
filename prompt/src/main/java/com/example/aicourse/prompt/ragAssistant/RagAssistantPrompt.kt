package com.example.aicourse.prompt.ragAssistant

import android.content.Context
import com.example.aicourse.prompt.DynamicSystemPrompt
import com.example.aicourse.prompt.R
import com.example.aicourse.prompt.plain.PlainTextResponse
import com.example.aicourse.prompt.utils.ResourceReader
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
        // Проверяем, добавил ли LLM секцию "Источники:"
        val hasSourcesSection = rawResponse.contains("**Источники:**") ||
                rawResponse.contains("Источники:") ||
                rawResponse.contains("ИСТОЧНИКИ:")

        val finalResponse = if (!hasSourcesSection && ragDocumentChunks.isNotEmpty()) {
            // LLM забыла добавить источники - добавляем автоматически
            val sourcesSection = buildSourcesSection()
            "$rawResponse\n\n$sourcesSection"
        } else {
            rawResponse
        }

        return PlainTextResponse(rawContent = finalResponse)
    }

    private fun buildSourcesSection(): String {
        if (ragDocumentChunks.isEmpty()) return ""

        // Получаем уникальные источники в порядке их появления
        val uniqueSources = ragDocumentChunks
            .map { it.source }
            .distinct()

        // Нумеруем уникальные источники
        val sources = uniqueSources
            .mapIndexed { index, source -> "[${index + 1}] $source" }
            .joinToString(separator = "\n")

        return "**Источники:**\n$sources"
    }
}
