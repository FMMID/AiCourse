package com.example.aicourse.data.chat.local.room.mapper

import com.example.aicourse.data.chat.local.room.converters.PolymorphicJson
import com.example.aicourse.data.chat.local.room.dao.ChatStateData
import com.example.aicourse.data.chat.local.room.entity.AiMessageEntity
import com.example.aicourse.data.chat.local.room.entity.ChatEntity
import com.example.aicourse.data.chat.local.room.entity.ChatMessageEntity
import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.MessageType
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.promt.BotResponse
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Mapper для преобразования между domain моделями и Room entities
 *
 * Domain → Entity: перед сохранением в БД
 * Entity → Domain: после загрузки из БД
 *
 * Использует PolymorphicJson для сериализации сложных типов
 */
class ChatStateMapper {

    private val json = PolymorphicJson.instance

    // ========== Domain → Entity ==========

    /**
     * Преобразует ChatStateModel в набор Room entities
     *
     * @param domain domain модель состояния чата
     * @return Triple<ChatEntity, List<ChatMessageEntity>, List<AiMessageEntity>>
     */
    fun toEntityModels(domain: ChatStateModel): Triple<ChatEntity, List<ChatMessageEntity>, List<AiMessageEntity>> {
        val chatEntity = ChatEntity(
            chatId = domain.id,
            settingsJson = json.encodeToString(domain.settingsChatModel),
            contextSummaryInfoJson = domain.contextSummaryInfo?.let {
                json.encodeToString(it)
            },
            activeSystemPromptJson = json.encodeToString<SystemPrompt<*>>(domain.activeSystemPrompt),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val chatMessages = domain.chatMessages.map { message ->
            toChatMessageEntity(message, domain.id)
        }

        val aiMessages = domain.messagesForSendToAi.map { message ->
            toAiMessageEntity(message, domain.id)
        }

        return Triple(chatEntity, chatMessages, aiMessages)
    }

    /**
     * Преобразует Message в ChatMessageEntity
     */
    private fun toChatMessageEntity(message: Message, chatId: String): ChatMessageEntity {
        return ChatMessageEntity(
            messageId = message.id,
            chatId = chatId,
            text = message.text,
            type = message.type.name,
            timestamp = message.timestamp,
            typedResponseJson = message.typedResponse?.let {
                json.encodeToString<BotResponse>(it)
            },
            tokenUsageJson = message.tokenUsage?.let {
                json.encodeToString(it)
            },
            toolResultJson = message.toolResult?.let {
                json.encodeToString<ToolResult>(it)
            }
        )
    }

    /**
     * Преобразует Message в AiMessageEntity (упрощенная версия без UI данных)
     */
    private fun toAiMessageEntity(message: Message, chatId: String): AiMessageEntity {
        return AiMessageEntity(
            messageId = message.id,
            chatId = chatId,
            text = message.text,
            type = message.type.name,
            timestamp = message.timestamp,
            tokenUsageJson = message.tokenUsage?.let {
                json.encodeToString(it)
            }
        )
    }

    // ========== Entity → Domain ==========

    /**
     * Преобразует набор Room entities в ChatStateModel
     *
     * @param data результат loadChatState() транзакции
     * @return domain модель состояния чата
     */
    fun toDomainModel(data: ChatStateData): ChatStateModel {
        return ChatStateModel(
            id = data.chat.chatId,
            settingsChatModel = json.decodeFromString<SettingsChatModel>(data.chat.settingsJson),
            chatMessages = data.chatMessages.map { entity ->
                toChatMessage(entity)
            }.toMutableList(), // ВАЖНО: toMutableList() для сохранения типа MutableList
            messagesForSendToAi = data.aiMessages.map { entity ->
                toAiMessage(entity)
            }.toMutableList(), // ВАЖНО: toMutableList() для сохранения типа MutableList
            contextSummaryInfo = data.chat.contextSummaryInfoJson?.let {
                json.decodeFromString<ContextSummaryInfo>(it)
            },
            activeSystemPrompt = json.decodeFromString<SystemPrompt<*>>(data.chat.activeSystemPromptJson)
        )
    }

    /**
     * Преобразует ChatMessageEntity в Message (полная версия с UI данными)
     */
    private fun toChatMessage(entity: ChatMessageEntity): Message {
        return Message(
            id = entity.messageId,
            text = entity.text,
            type = MessageType.valueOf(entity.type),
            timestamp = entity.timestamp,
            typedResponse = entity.typedResponseJson?.let {
                json.decodeFromString<BotResponse>(it)
            },
            tokenUsage = entity.tokenUsageJson?.let {
                json.decodeFromString<TokenUsage>(it)
            },
            toolResult = entity.toolResultJson?.let {
                json.decodeFromString<ToolResult>(it)
            }
        )
    }

    /**
     * Преобразует AiMessageEntity в Message (упрощенная версия без UI данных)
     */
    private fun toAiMessage(entity: AiMessageEntity): Message {
        return Message(
            id = entity.messageId,
            text = entity.text,
            type = MessageType.valueOf(entity.type),
            timestamp = entity.timestamp,
            typedResponse = null, // AiMessage не содержит typedResponse
            tokenUsage = entity.tokenUsageJson?.let {
                json.decodeFromString<TokenUsage>(it)
            },
            toolResult = null // AiMessage не содержит toolResult
        )
    }
}
