package com.example.aicourse.data.chat.remote.mapper

import android.content.Context
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.domain.utils.ResourceReader

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
        resolvedModel: String? = null,
        contextSummaryInfo: ContextSummaryInfo? = null
    ): ChatConfig {
        val baseSystemPrompt = systemPrompt.contentResourceId?.let { resourceId ->
            ResourceReader.readRawResource(context, resourceId)
        }

        val fullSystemContent = when {
            baseSystemPrompt != null && contextSummaryInfo != null -> {
                baseSystemPrompt + "\n\n" + "КОНТЕКСТ ДИАЛОГА:\n${contextSummaryInfo.message}"
            }

            baseSystemPrompt != null -> baseSystemPrompt

            contextSummaryInfo != null -> "КОНТЕКСТ ДИАЛОГА:\n${contextSummaryInfo.message}"

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
}
