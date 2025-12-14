package com.example.aicourse.domain.chat.strategy

import com.example.aicourse.domain.chat.model.ChatStateModel
import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SendMessageResult
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend

interface ChatStrategy {

    val chatStateModel: ChatStateModel

    suspend fun prepareData(userMessage: Message): DataForSend

    suspend fun processReceivedData(sendMessageResult: SendMessageResult): DataForReceive

    suspend fun clear()
}