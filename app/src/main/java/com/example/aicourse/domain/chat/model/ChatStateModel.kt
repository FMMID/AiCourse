package com.example.aicourse.domain.chat.model

import com.example.aicourse.domain.chat.promt.BotResponse
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import kotlinx.serialization.Serializable

@Serializable
data class ChatStateModel(
    val id: String,
    val settingsChatModel: SettingsChatModel,
    val chatMessages: MutableList<Message>,
    var messagesForSendToAi: MutableList<Message>,
    var contextSummaryInfo: ContextSummaryInfo?, //TODO возможно занести под SettingsChatModel.historyStrategy
    var activeSystemPrompt: SystemPrompt<BotResponse> = PlainTextPrompt()
)
