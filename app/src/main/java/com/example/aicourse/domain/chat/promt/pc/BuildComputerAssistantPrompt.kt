package com.example.aicourse.domain.chat.promt.pc

import com.example.aicourse.R
import com.example.aicourse.domain.chat.promt.StaticSystemPrompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Системный промпт для помощи в сборке ПК
 *
 * Активируется при:
 * - Командах: /build, /pc
 * - Ключевых словах: "собрать пк", "собрать компьютер", "сборка пк", "конфигурация пк"
 *
 * Ассистент задает вопросы пользователю о бюджете, целях использования,
 * и в финале предоставляет полную конфигурацию в JSON формате
 */
@Serializable
@SerialName("build_computer_assistant")
class BuildComputerAssistantPrompt : StaticSystemPrompt<PcBuildResponse> {

    override val temperature: Float = 0.7f
    override val topP: Float = 0.9f
    override val maxTokens: Int = 4096
    override val contentResourceId: Int = R.raw.build_computer_assistant_prompt

    companion object {
        private val COMMAND_TRIGGERS = listOf("/build", "/pc")

        private val KEYWORD_TRIGGERS = listOf(
            "собрать пк",
            "собрать компьютер",
            "сборка пк",
            "конфигурация пк",
            "подобрать пк",
            "подобрать компьютер",
            "помоги собрать пк",
            "помоги собрать компьютер"
        )

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    override fun matches(message: String): Boolean {
        val lowerMessage = message.trim().lowercase()

        // Проверка команд: точное совпадение или с пробелом после
        if (COMMAND_TRIGGERS.any { trigger ->
                lowerMessage == trigger || lowerMessage.startsWith("$trigger ")
            }) {
            return true
        }

        return KEYWORD_TRIGGERS.any { keyword ->
            lowerMessage.contains(keyword)
        }
    }

    override fun parseResponse(rawResponse: String): PcBuildResponse {
        val jsonContent = extractJsonFromMarkdown(rawResponse) ?: rawResponse

        return try {
            val apiResponse = json.decodeFromString<PcBuildApiResponse>(jsonContent)

            PcBuildResponse(
                rawContent = rawResponse,
                isFinished = apiResponse.isFinished,
                question = apiResponse.question,
                pcBuild = apiResponse.pcBuild
            )
        } catch (e: Exception) {
            PcBuildResponse(
                rawContent = rawResponse,
                isFinished = false,
                question = "Ошибка парсинга ответа: ${e.message}",
                pcBuild = null
            )
        }
    }

    /**
     * Извлекает JSON из markdown блока кода
     */
    private fun extractJsonFromMarkdown(text: String): String? {
        val jsonBlockPattern = "```(?:json)?\\s*([\\s\\S]*?)```".toRegex()
        val match = jsonBlockPattern.find(text)
        return match?.groupValues?.get(1)?.trim()
    }
}
