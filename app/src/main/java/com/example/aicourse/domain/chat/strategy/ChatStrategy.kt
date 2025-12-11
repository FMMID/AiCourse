package com.example.aicourse.domain.chat.strategy

import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.chat.repository.SendMessageResult
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.settings.model.SettingsChatModel

interface ChatStrategy {

    val settingsChatModel: SettingsChatModel

    suspend fun prepareData(sendToChatDataModel: SendToChatDataModel): DataForSend

    suspend fun processReceivedData(
        sendMessageResult: SendMessageResult,
        sendToChatDataModel: SendToChatDataModel
    ): DataForReceive

    suspend fun clear()
}