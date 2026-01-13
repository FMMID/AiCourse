package com.example.aicourse.data.chat.local.room.converters

import com.example.aicourse.prompt.BotResponse
import com.example.aicourse.prompt.SystemPrompt
import com.example.aicourse.prompt.dynamicModel.DynamicModelPrompt
import com.example.aicourse.prompt.dynamicModel.DynamicModelResponse
import com.example.aicourse.prompt.dynamicSystemPrompt.DynamicSystemPrompt
import com.example.aicourse.prompt.dynamicSystemPrompt.DynamicSystemPromptResponse
import com.example.aicourse.prompt.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.prompt.dynamicTemperature.DynamicTemperatureResponse
import com.example.aicourse.prompt.json.JsonOutputPrompt
import com.example.aicourse.prompt.json.JsonOutputResponse
import com.example.aicourse.prompt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.prompt.pc.PcBuildResponse
import com.example.aicourse.prompt.plain.PlainTextPrompt
import com.example.aicourse.prompt.plain.PlainTextResponse
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextInfo
import com.example.aicourse.domain.tools.context.model.ContextWindowInfo
import com.example.aicourse.domain.tools.modelInfo.model.ModelInfo
import com.example.aicourse.domain.tools.tokenComparePrevious.model.TokenUsageDiff
import com.example.aicourse.prompt.ModelType
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Централизованный JSON serializer с поддержкой полиморфной сериализации
 *
 * Используется для сериализации domain моделей в JSON строки для хранения в Room БД
 * Все полиморфные типы (BotResponse, ToolResult, SystemPrompt) регистрируются
 * с их @SerialName дискриминаторами
 */
object PolymorphicJson {
    val instance = Json {
        serializersModule = SerializersModule {
            // BotResponse полиморфизм (6 типов)
            polymorphic(BotResponse::class) {
                subclass(PlainTextResponse::class)
                subclass(JsonOutputResponse::class)
                subclass(PcBuildResponse::class)
                subclass(DynamicTemperatureResponse::class)
                subclass(DynamicModelResponse::class)
                subclass(DynamicSystemPromptResponse::class)
            }

            // ToolResult полиморфизм (4 типа)
            polymorphic(ToolResult::class) {
                subclass(ContextWindowInfo::class)
                subclass(TokenUsageDiff::class)
                subclass(ModelInfo::class)
                subclass(ContextInfo::class)
            }

            // SystemPrompt полиморфизм (6 типов)
            polymorphic(SystemPrompt::class) {
                subclass(PlainTextPrompt::class)
                subclass(JsonOutputPrompt::class)
                subclass(BuildComputerAssistantPrompt::class)
                subclass(DynamicTemperaturePrompt::class)
                subclass(DynamicModelPrompt::class)
                subclass(DynamicSystemPrompt::class)
            }

            polymorphic(ModelType::class) {
                subclass(ModelType.Fast::class)
                subclass(ModelType.Balanced::class)
                subclass(ModelType.Powerful::class)
            }
        }

        // Кодировать значения по умолчанию для полноты данных
        encodeDefaults = true

        // Игнорировать неизвестные ключи при десериализации (для обратной совместимости)
        ignoreUnknownKeys = true

        // Красивое форматирование для отладки (опционально, можно убрать для production)
        prettyPrint = false
    }
}
