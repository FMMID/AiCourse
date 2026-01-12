package com.example.aicourse.domain.chat.model

import com.example.aicourse.prompt.BotResponse
import com.example.aicourse.prompt.SystemPrompt
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.rag.domain.model.RagMode
import kotlinx.serialization.Serializable

@Serializable
data class ChatStateModel(
    val id: String,
    val settingsChatModel: SettingsChatModel,
    val chatMessages: MutableList<Message>,
    var messagesForSendToAi: MutableList<Message>,
    var contextSummaryInfo: ContextSummaryInfo?, //TODO возможно занести под SettingsChatModel.historyStrategy
    var activeSystemPrompt: SystemPrompt<BotResponse>,
    val ragIds: List<String>,
    var ragMode: RagMode = if (ragIds.isNotEmpty()) RagMode.STANDARD else RagMode.DISABLED
)
