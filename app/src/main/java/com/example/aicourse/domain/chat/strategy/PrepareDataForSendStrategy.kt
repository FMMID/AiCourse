package com.example.aicourse.domain.chat.strategy

import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.settings.model.SettingsChatModel

interface PrepareDataForSendStrategy {

    var settingsChatModel: SettingsChatModel

    suspend fun prepareData(sendToChatDataModel: SendToChatDataModel): DataForSend
}