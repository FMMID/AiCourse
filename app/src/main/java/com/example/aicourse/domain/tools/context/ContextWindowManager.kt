package com.example.aicourse.domain.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.model.SendToChatDataModel
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextWindow

//TODO должен выдавать статистику по контекстному окну + обработать сообщения
class ContextWindowManager(
    private val targetContextWindow: ContextWindow
) : Tool<SendToChatDataModel> {


    override fun processData(processData: SendToChatDataModel): ToolResult {
        // TODO тут происходит обработка сообщения перед отправкой в чат.
        //  (история диалога + выбранная модель + чат) - текущее состояние контекстного окна, модель определят целевое контекстное окно
        //  по достижению которого нужно запускать процесс ужатия истории и помещать его в systemPromt, сравнение идет с targetContextWindow

        return error("NotImplemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    private fun summarizeMessages(messageHistory: List<Message>): String {
        // TODO тут происходит процесс ужимки истории и возвращение результата
        return error("Not implemented")
    }
}