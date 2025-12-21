package com.example.aicourse.domain.chat.strategy

import android.app.Application
import com.example.aicourse.di.AppInjector
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.SendMessageResult
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicModel.DynamicModelPrompt
import com.example.aicourse.domain.chat.promt.dynamicSystemPrompt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.domain.chat.promt.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.promt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.TokenConsumptionMode
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.context.ContextWindowManager
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.domain.tools.context.model.ContextWindow
import com.example.aicourse.domain.tools.modelInfo.ModelInfoManager
import com.example.aicourse.domain.tools.tokenComparePrevious.TokenCompareManager
import java.util.UUID

class SimpleChatStrategy(
    initChatStateModel: ChatStateModel,
    private val applicationContext: Application
) : ChatStrategy {

    override var chatStateModel: ChatStateModel = initChatStateModel

    private val contextWindowManager = ContextWindowManager(
        // TODO сделать создание ContextWindow под конкретную модель, которая сейчас используется
        targetContextWindow = ContextWindow(
            originalLimit = 8000,
            keepLastMessagesNumber = 1,
            summaryThreshold = 0.4f
        ),
        contextRepository = AppInjector.createContextRepository(chatStateModel.settingsChatModel),
        applicationContext = applicationContext,
    )

    private val activeTool: Tool<*>? = run {
        when (val outPutStrategy = chatStateModel.settingsChatModel.outPutDataStrategy) {
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

    //TODO надо разбить функции на мелкие части, сейчас перегруз инфы
    override suspend fun prepareData(userMessage: Message): DataForSend {
        chatStateModel.chatMessages.add(userMessage)

        if (isResetCommand(userMessage.text)) {
            chatStateModel.activeSystemPrompt = PlainTextPrompt()
            return DataForSend.LocalResponse(
                responseMessage = null,
                activePrompt = chatStateModel.activeSystemPrompt
            )
        }

        val newPrompt = extractSystemPromptFromContent(
            content = userMessage.text,
            currentPrompt = chatStateModel.activeSystemPrompt
        ) ?: chatStateModel.activeSystemPrompt
        chatStateModel.activeSystemPrompt = newPrompt

        val localResponseMessage = handleLocalMessage(message = userMessage.text, activeSystemPrompt = chatStateModel.activeSystemPrompt)
        if (localResponseMessage != null) {
            chatStateModel.chatMessages.add(localResponseMessage)
            return DataForSend.LocalResponse(
                responseMessage = localResponseMessage,
                activePrompt = chatStateModel.activeSystemPrompt
            )
        }

        val cleanedMessageForSendToApi = prepareMessageForSending(
            prompt = chatStateModel.activeSystemPrompt,
            message = userMessage.text
        )
        chatStateModel.messagesForSendToAi.add(cleanedMessageForSendToApi)

        val historyToSend = prepareHistoryForSending(
            messageHistory = chatStateModel.messagesForSendToAi,
            activeSystemPrompt = chatStateModel.activeSystemPrompt,
            contextSummaryInfo = chatStateModel.contextSummaryInfo
        )

        return DataForSend.RemoteCall(
            messageHistory = historyToSend,
            activePrompt = chatStateModel.activeSystemPrompt,
            contextSummaryInfo = chatStateModel.contextSummaryInfo
        )
    }

    override suspend fun processReceivedData(sendMessageResult: SendMessageResult): DataForReceive {
        val responseMessage = sendMessageResult.toMessage()
        chatStateModel.messagesForSendToAi.add(responseMessage)

        val toolResult = when (val tool = activeTool) {
            is TokenCompareManager -> {
                tool.processData(processData = responseMessage)
            }

            is ContextWindowManager -> {
                tool.processData(processData = responseMessage)
            }

            else -> null
        }

        val finalMessage = responseMessage.copy(toolResult = toolResult)
        chatStateModel.chatMessages.add(finalMessage)

        return DataForReceive.Simple(
            message = finalMessage,
            activePrompt = chatStateModel.activeSystemPrompt,
            toolResult = toolResult
        )
    }

    override suspend fun clear() {
        activeTool?.clear()
        chatStateModel = ChatStateModel(
            id = chatStateModel.id,
            settingsChatModel = chatStateModel.settingsChatModel,
            chatMessages = mutableListOf(),
            messagesForSendToAi = mutableListOf(),
            contextSummaryInfo = null,
            activeSystemPrompt = AppInjector.initActiveUserPrompt
        )
    }

    private fun handleLocalMessage(message: String, activeSystemPrompt: SystemPrompt<*>): Message? {
        val localResponse = activeSystemPrompt.handleMessageLocally(message)
        return localResponse?.let {
            Message(
                id = UUID.randomUUID().toString(),
                text = localResponse.rawContent,
                type = MessageType.SYSTEM,
                typedResponse = localResponse,
            )
        }
    }

    /**
     * Извлекает подходящий SystemPrompt на основе триггеров в сообщении
     * Проходит по списку доступных промптов и возвращает первый подошедший
     *
     * Логика сохранения состояния:
     * - Если текущий промпт того же типа, что и новый - сохраняем его состояние
     * - Если типы разные - создаём новый с дефолтными параметрами
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
            currentPrompt as? DynamicSystemPrompt ?: DynamicSystemPrompt(),
            currentPrompt as? DynamicTemperaturePrompt ?: DynamicTemperaturePrompt(),
            currentPrompt as? DynamicModelPrompt ?: DynamicModelPrompt(),
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
    private fun prepareMessageForSending(prompt: SystemPrompt<*>, message: String): Message {
        val correctedMessageString = when (prompt) {
            is DynamicTemperaturePrompt -> {
                prompt.extractAndCleanMessage(message)
            }

            is DynamicModelPrompt -> {
                prompt.extractAndCleanMessage(message)
            }

            else -> message
        }

        val message = Message(
            id = UUID.randomUUID().toString(),
            text = correctedMessageString,
            type = MessageType.USER,
        )

        return message
    }

    /**
     * Формирует историю сообщений для отправки к API на основе активного промпта
     * Некоторые промпты могут не использовать историю
     *
     * @return история для отправки (может быть пустой для некоторых промптов)
     */
    private suspend fun prepareHistoryForSending(
        messageHistory: List<Message>,
        activeSystemPrompt: SystemPrompt<*>,
        contextSummaryInfo: ContextSummaryInfo?
    ): List<Message> {
        return when (chatStateModel.settingsChatModel.historyStrategy) {
            HistoryStrategy.PAIN -> messageHistory

            HistoryStrategy.ONE_MESSAGE -> emptyList()

            HistoryStrategy.SUMMARIZE -> contextWindowManager.processMessageHistory(
                messageHistory = messageHistory,
                activeSystemPrompt = activeSystemPrompt,
                contextSummaryInfo = contextSummaryInfo
            ).let { historyWithSummaryInfo ->
                chatStateModel.contextSummaryInfo = historyWithSummaryInfo.contextSummaryInfo
                chatStateModel.messagesForSendToAi = historyWithSummaryInfo.messagesForSendToAi.toMutableList()
                historyWithSummaryInfo.messagesForSendToAi
            }
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

    private fun isResetCommand(text: String): Boolean {
        val lowerText = text.trim().lowercase()
        return lowerText == "/reset" || lowerText == "/plain"
    }
}
