package com.example.aicourse.domain.chat.strategy

import com.example.aicourse.di.AppInjector
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicModel.DynamicModelPrompt
import com.example.aicourse.domain.chat.promt.dynamicSystemPrompt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.domain.chat.promt.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.promt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.repository.SendMessageResult
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.settings.model.TokenConsumptionMode
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.context.ContextWindowManager
import com.example.aicourse.domain.tools.context.model.ContextWindow
import com.example.aicourse.domain.tools.modelInfo.ModelInfoManager
import com.example.aicourse.domain.tools.tokenComparePrevious.TokenCompareManager
import java.util.UUID

class SimpleChatStrategy(initSettingsChatModel: SettingsChatModel) : ChatStrategy {

    override val settingsChatModel: SettingsChatModel = initSettingsChatModel

    private val contextWindowManager = ContextWindowManager(
        targetContextWindow = ContextWindow(originalLimit = 1000), // TODO сделать создание ContextWindow под конкретную модель, которая сейчас используется
        contextRepository = AppInjector.createContextRepository(settingsChatModel)
    )
    private val activeTool: Tool<*>? = run {
        when (val outPutStrategy = initSettingsChatModel.outPutDataStrategy) {
            is OutPutDataStrategy.ModelInfo -> ModelInfoManager()

            is OutPutDataStrategy.Token -> {
                when (outPutStrategy.tokenConsumptionMode) {
                    TokenConsumptionMode.COMPARE_PREVIOUS -> TokenCompareManager()
                    TokenConsumptionMode.CONTEXT_MODE -> contextWindowManager
                }
            }

            OutPutDataStrategy.None -> null
        }
    }
    private val messageHistory: MutableList<Message> = mutableListOf()
    private var activeSystemPrompt: SystemPrompt<*> = PlainTextPrompt()

    override suspend fun prepareData(userMessage: Message): DataForSend {
        messageHistory.add(userMessage)

        val newPrompt = extractSystemPromptFromContent(
            content = userMessage.text,
            currentPrompt = activeSystemPrompt
        ) ?: activeSystemPrompt

        activeSystemPrompt = newPrompt

        val localResponseMessage = handleLocalMessage(message = userMessage.text)
        if (localResponseMessage != null) {
            return DataForSend.LocalResponse(
                responseMessage = localResponseMessage,
                activePrompt = activeSystemPrompt
            )
        }

        val cleanedMessage = prepareMessageForSending(
            prompt = newPrompt,
            message = userMessage.text
        )
        val historyToSend = prepareHistoryForSending(message = cleanedMessage)

        // Если используется SUMMARIZE стратегия, обновляем промпт с суммаризацией
        if (settingsChatModel.historyStrategy == HistoryStrategy.SUMMARIZE) {
            contextWindowManager.updatePromptWithSummary(activeSystemPrompt)
        }

        return DataForSend.RemoteCall(
            message = cleanedMessage,
            messageHistory = historyToSend,
            activePrompt = activeSystemPrompt
        )
    }

    override suspend fun processReceivedData(sendMessageResult: SendMessageResult): DataForReceive {
        val responseMessage = sendMessageResult.toMessage()
        messageHistory.add(responseMessage)

        val toolResult = when (val tool = activeTool) {
            is TokenCompareManager -> {
                tool.processData(processData = responseMessage)
            }

            is ContextWindowManager -> {
                tool.processData(processData = DataForReceive.Simple(message = responseMessage, activePrompt = activeSystemPrompt))
            }

            else -> null
        }

        return DataForReceive.Simple(
            message = responseMessage,
            activePrompt = activeSystemPrompt,
            toolResult = toolResult
        )
    }

    override suspend fun clear() {
        activeTool?.clear()
        messageHistory.clear()
    }

    private fun handleLocalMessage(message: String): Message? {
        val localResponse = activeSystemPrompt.handleMessageLocally(message)
        return localResponse?.let {
            Message(
                id = UUID.randomUUID().toString(),
                text = localResponse.rawContent,
                type = MessageType.BOT,
                typedResponse = localResponse,
                tokenUsage = null,
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
     * @param message текущее сообщение для отправки
     * @return история для отправки (может быть пустой для некоторых промптов)
     */
    private suspend fun prepareHistoryForSending(message: String): List<Message> {
        return when (settingsChatModel.historyStrategy) {
            HistoryStrategy.PAIN -> messageHistory
            HistoryStrategy.ONE_MESSAGE -> emptyList()
            HistoryStrategy.SUMMARIZE -> contextWindowManager.processMessageHistory(
                DataForSend.RemoteCall(
                    message = message,
                    messageHistory = messageHistory,
                    activePrompt = activeSystemPrompt
                )
            )
        }
    }

    private fun SendMessageResult.toMessage(): Message {
        return Message(
            id = UUID.randomUUID().toString(),
            text = botResponse.rawContent,
            type = MessageType.BOT,
            typedResponse = botResponse,
            tokenUsage = tokenUsage,
        )
    }
}
