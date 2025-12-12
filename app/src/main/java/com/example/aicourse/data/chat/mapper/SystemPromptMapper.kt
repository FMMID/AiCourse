package com.example.aicourse.data.chat.mapper

import android.content.Context
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.domain.chat.promt.SystemPrompt
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Маппер для конвертации domain модели SystemPrompt в data модель ChatConfig
 * Следует принципам Clean Architecture: зависимость data → domain
 */
object SystemPromptMapper {

    /**
     * Конвертирует SystemPrompt в ChatConfig для использования в data layer
     * Читает содержимое промпта из ресурсов, если указан contentResourceId
     * Объединяет базовый системный промпт с contextSummary, если таковой имеется
     *
     * @param context Android context для доступа к ресурсам
     * @param systemPrompt domain модель промпта
     * @param resolvedModel уже резолвленный идентификатор модели (опционально)
     * @return ChatConfig для передачи в ChatRemoteDataSource
     */
    fun toChatConfig(
        context: Context,
        systemPrompt: SystemPrompt<*>,
        resolvedModel: String? = null
    ): ChatConfig {
        val baseContent = systemPrompt.contentResourceId?.let { resourceId ->
            readRawResource(context, resourceId)
        }

        // Объединяем базовый контент с контекстной суммаризацией
        val fullSystemContent = when {
            baseContent != null && systemPrompt.contextSummary != null -> {
                "$baseContent\n\nКОНТЕКСТ ДИАЛОГА:\n${systemPrompt.contextSummary}"
            }
            baseContent != null -> baseContent
            systemPrompt.contextSummary != null -> "КОНТЕКСТ ДИАЛОГА:\n${systemPrompt.contextSummary}"
            else -> null
        }

        return ChatConfig(
            temperature = systemPrompt.temperature,
            topP = systemPrompt.topP,
            maxTokens = systemPrompt.maxTokens,
            systemContent = fullSystemContent,
            model = resolvedModel
        )
    }

    /**
     * Читает содержимое текстового файла из res/raw
     * @param context Android context
     * @param resourceId ID ресурса из R.raw
     * @return содержимое файла в виде строки
     */
    private fun readRawResource(context: Context, resourceId: Int): String {
        return context.resources.openRawResource(resourceId).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    }
}
