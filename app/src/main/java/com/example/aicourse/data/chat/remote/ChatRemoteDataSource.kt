package com.example.aicourse.data.chat.remote

/**
 * Интерфейс для удаленного источника данных чата
 * Абстракция над конкретной реализацией API (GigaChat, OpenAI, и т.д.)
 */
interface ChatRemoteDataSource {

    /**
     * Отправляет сообщение в API и получает ответ
     * @param message текст сообщения
     * @return ответ от API
     * @throws Exception если произошла ошибка сети или API
     */
    suspend fun sendMessage(message: String): String
}
