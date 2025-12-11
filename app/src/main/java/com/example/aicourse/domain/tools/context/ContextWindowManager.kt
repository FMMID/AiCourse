package com.example.aicourse.domain.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.tools.context.model.ContextWindow

class ContextWindowManager(
    private val targetContextWindow: ContextWindow
) {

    fun processMessageBeforeSendToApi() {
        // TODO тут происходит обработка сообщения перед отправкой в чат.
        //  (история диалога + выбранная модель + чат) - текущее состояние контекстного окна, модель определят целевое контекстное окно
        //  по достижению которого нужно запускать процесс ужатия истории и помещать его в systemPromt, сравнение идет с targetContextWindow
    }

    private fun summarizeMessages(messageHistory: List<Message>): String {
        // TODO тут происходит процесс ужимки истории и возвращение результата
        return error("Not implemented")
    }
}