package com.example.aicourse.data.chat.mapper

import android.content.Context
import com.example.aicourse.data.chat.remote.ChatConfig
import com.example.aicourse.domain.chat.model.SystemPrompt
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
        val systemContent = systemPrompt.contentResourceId?.let { resourceId ->
            readRawResource(context, resourceId)
        }

        return ChatConfig(
            temperature = systemPrompt.temperature,
            topP = systemPrompt.topP,
            maxTokens = systemPrompt.maxTokens,
            systemContent = systemContent,
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
