package com.example.aicourse.data.chat.mapper

import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.domain.chat.model.SystemPrompt

/**
 * Маппер для конвертации domain модели SystemPrompt в data модель ChatConfig
 * Следует принципам Clean Architecture: зависимость data → domain
 */
object SystemPromptMapper {

    /**
     * Конвертирует SystemPrompt в ChatConfig для использования в data layer
     * @param systemPrompt domain модель промпта
     * @return ChatConfig для передачи в ChatRemoteDataSource
     */
    fun toChatConfig(systemPrompt: SystemPrompt<*>): ChatConfig {
        return ChatConfig(
            temperature = systemPrompt.temperature,
            topP = systemPrompt.topP,
            systemContent = systemPrompt.content
        )
    }
}
