package com.example.aicourse.data.chat.local

import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.settings.model.ApiImplementation
import com.example.aicourse.domain.settings.model.HistoryStrategy
import com.example.aicourse.domain.settings.model.OutPutDataStrategy
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.settings.model.TokenConsumptionMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * In-memory реализация локального хранилища для истории чата
 * TODO: Заменить на Room Database или SharedPreferences для персистентного хранения
 */
class InMemoryChatDataSource : ChatLocalDataSource {

    private var actualChatStateModel: ChatStateModel = ChatStateModel(
        id = ChatLocalDataSource.MAIN_CHAT_ID,
        settingsChatModel = SettingsChatModel(
            currentUseApiImplementation = ApiImplementation.GIGA_CHAT,
            historyStrategy = HistoryStrategy.SUMMARIZE,
            outPutDataStrategy = OutPutDataStrategy.Token(TokenConsumptionMode.CONTEXT_MODE)
        ),
        chatMessages = mutableListOf(),
        messagesForSendToAi = mutableListOf(),
        contextSummaryInfo = null,
        activeSystemPrompt = PlainTextPrompt(),
    )

    override suspend fun getChatState(id: String): ChatStateModel = withContext(Dispatchers.IO) {
        return@withContext actualChatStateModel
    }

    override suspend fun saveChatState(chatStateModel: ChatStateModel) = withContext(Dispatchers.IO) {
        actualChatStateModel = chatStateModel
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        //TODO
    }
}
