package com.example.aicourse.domain.usecase

import kotlinx.coroutines.delay

class ChatUseCase {

    suspend fun sendMessageToBot(message: String): String {
        delay(1000)

        return "Это заглушка ответа бота на сообщение: '$message'"
    }
}
