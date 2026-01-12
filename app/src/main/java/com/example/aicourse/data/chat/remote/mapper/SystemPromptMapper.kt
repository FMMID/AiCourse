package com.example.aicourse.data.chat.remote.mapper

import android.content.Context
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.prompt.SystemPrompt
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

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

        val baseSystemPrompt = systemPrompt.extractSystemPrompt(context)

        val contextSummaryText = contextSummaryInfo?.message?.let {
            """
            Также при общении учитывай контекст нашего диалога: ${contextSummaryInfo.message}
            """.trimIndent()
        }

        val fullSystemContent = when {
            baseSystemPrompt != null && contextSummaryText != null -> baseSystemPrompt + "\n" + contextSummaryText
            baseSystemPrompt != null -> baseSystemPrompt
            contextSummaryInfo != null -> contextSummaryText
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
