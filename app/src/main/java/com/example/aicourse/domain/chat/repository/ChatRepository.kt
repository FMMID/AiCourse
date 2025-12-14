package com.example.aicourse.domain.chat.repository

import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SendMessageResult
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo

/**
 * Интерфейс репозитория для работы с чатом
 * Определяет контракт взаимодействия с источниками данных (Clean Architecture)
 */
interface ChatRepository {

    /**
     * Отправляет сообщение боту и получает типизированный ответ с метаданными
     * @param systemPrompt промпт, определяющий поведение модели и тип ответа
     * @param messageHistory история предыдущих сообщений для контекста
     * @param contextSummaryInfo контекст диалога, если есть
     * @return результат с типизированным ответом и метаданными (токены, имя модели)
     * @throws Exception если произошла ошибка при отправке/получении
     */
    suspend fun sendMessage(
        systemPrompt: SystemPrompt<*>,
        messageHistory: List<Message> = emptyList(),
        contextSummaryInfo: ContextSummaryInfo? = null
    ): Result<SendMessageResult>

    /**
     * Сохраняет состояние чата в хранилище
     */
    suspend fun saveChatSate(chatStateModel: ChatStateModel): Result<Unit>

    /**
     * Восстанавливает состояние чата из хранилища
     */
    suspend fun getChatState(chatId: String): Result<ChatStateModel>

    /**
     * Очищает историю сообщений
     */
    suspend fun clearHistory(): Result<Unit>
}
