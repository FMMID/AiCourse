package com.example.aicourse.domain.chat.strategy

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicModel.DynamicModelPrompt
import com.example.aicourse.domain.chat.promt.dynamicSystemPrompt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.domain.chat.promt.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.promt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import java.util.UUID

class SimpleDataForSendStrategyImp(initSettingsChatModel: SettingsChatModel) : PrepareDataForSendStrategy {

    override var settingsChatModel: SettingsChatModel = initSettingsChatModel

    override suspend fun prepareData(sendToChatDataModel: SendToChatDataModel): DataForSend {
        val newPrompt = extractSystemPromptFromContent(
            content = sendToChatDataModel.message,
            currentPrompt = sendToChatDataModel.systemPrompt
        ) ?: sendToChatDataModel.systemPrompt

        val localResponseMessage = handleLocalMessage(newPrompt, sendToChatDataModel.message)
        if (localResponseMessage != null) return DataForSend.LocalResponse(
            responseMessage = localResponseMessage,
            activePrompt = newPrompt,
            activeModelName = null
        )

        val cleanedMessage = prepareMessageForSending(newPrompt, sendToChatDataModel.message)
        val historyToSend = prepareHistoryForSending(newPrompt, sendToChatDataModel.messageHistory, settingsChatModel)

        return DataForSend.RemoteCall(
            SendToChatDataModel(
                message = cleanedMessage,
                systemPrompt = newPrompt,
                messageHistory = historyToSend
            )
        )
    }

    private fun handleLocalMessage(activePrompt: SystemPrompt<*>, message: String): Message? {
        val localResponse = activePrompt.handleMessageLocally(message)
        return localResponse?.let {
            Message(
                id = UUID.randomUUID().toString(),
                text = localResponse.rawContent,
                type = MessageType.BOT,
                typedResponse = localResponse,
                tokenUsage = null,
                tokenUsageDiff = null
            )
        }
    }

    /**
     * Извлекает подходящий SystemPrompt на основе триггеров в сообщении
     * Проходит по списку доступных промптов и возвращает первый подошедший
     *
     * @param content текст сообщения от пользователя
     * @param currentPrompt активный промпт в рамках текущего чата
     * @return подходящий SystemPrompt или null если триггеров не найдено
     */
    private fun extractSystemPromptFromContent(
        content: String,
        currentPrompt: SystemPrompt<*>,
    ): SystemPrompt<*>? {
        val availablePrompts = listOf(
            JsonOutputPrompt(),
            BuildComputerAssistantPrompt(),
            DynamicSystemPrompt(currentPrompt),
            DynamicTemperaturePrompt(currentPrompt),
            DynamicModelPrompt(currentPrompt),
        )

        return availablePrompts.firstOrNull { prompt ->
            prompt.matches(content)
        }
    }

    /**
     * Формирует сообщение для отправки к API на основе активного промпта
     * Некоторые промпты могут изменять сообщение перед отправкой
     *
     * @param prompt активный системный промпт
     * @param message исходное сообщение пользователя
     * @return обработанное сообщение для отправки
     */
    private fun prepareMessageForSending(prompt: SystemPrompt<*>, message: String): String {
        return when (prompt) {
            is DynamicTemperaturePrompt -> {
                prompt.extractAndCleanMessage(message)
            }

            is DynamicModelPrompt -> {
                prompt.extractAndCleanMessage(message)
            }

            else -> message
        }
    }

    /**
     * Формирует историю сообщений для отправки к API на основе активного промпта
     * Некоторые промпты могут не использовать историю
     *
     * @param prompt активный системный промпт
     * @param messageHistory полная история сообщений
     * @param settingsChatModel настройка чата
     * @return история для отправки (может быть пустой для некоторых промптов)
     */
    private fun prepareHistoryForSending(
        prompt: SystemPrompt<*>,
        messageHistory: List<Message>,
        settingsChatModel: SettingsChatModel
    ): List<Message> {
        return when (settingsChatModel.historyStrategy) {
            HistoryStrategy.PAIN -> messageHistory
            HistoryStrategy.ONE_MESSAGE -> emptyList()
            HistoryStrategy.SUMMERIZE -> error("not implemented")
        }
    }
}