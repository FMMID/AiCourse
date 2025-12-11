package com.example.aicourse.domain.tools.context

import com.example.aicourse.domain.chat.model.Message
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.plain.PlainTextPrompt
import com.example.aicourse.domain.chat.strategy.model.DataForReceive
import com.example.aicourse.domain.chat.strategy.model.DataForSend
import com.example.aicourse.domain.tools.Tool
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextInfo
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import com.example.aicourse.domain.tools.context.model.ContextWindow

//TODO должен выдавать статистику по контекстному окну + обработать сообщения
class ContextWindowManager(
    private val targetContextWindow: ContextWindow,
    private val contextRepository: ContextRepository
) : Tool<DataForReceive.Simple> {

    private val messageBuffer: List<Message> = mutableListOf()
    private var contextSummary: ContextSummaryInfo = ContextSummaryInfo(message = "", totalTokens = 0)
    private var currentSystemPrompt: SystemPrompt<*> = PlainTextPrompt()
    private var contextInfo: ContextInfo = ContextInfo(sizeOfSummaryMessages = 0, sizeOfActiveMessages = 0, sizeOfSystemPrompt = 0)

    fun processMessageHistory(dataForSend: DataForSend.RemoteCall): List<Message> {
        /**
         * TODO:
         *  1. обновляем состояние на основе входных параметров
         *  2. определяем, нужна ли суммаризация: needSummary = targetContextWindow.shouldSummeryDialog(contextSummary.totalTokens + messageBufferSize + activeStrategy.systemPromptSize)
         *  2. делаем запрос на суммаризацию сообщений, обновляем:
         *      - из messageBuffer убираем количество сообщений, которое отправляем на саммаризацию, оставляем 5 последних сообщений.
         *      - currentSummary принимает значение просчитанной суммаризации
         *  3. добавляем суммаризацию в текущий системный промпт
         *  4. отдаем список сообщений на отправку в чат (саммаризация подтянется из промпта)
         */

        return TODO()
    }

    //TODO возвращает текущее состояние контекста для отображения в UI.
    override fun processData(processData: DataForReceive.Simple): ToolResult {
        return error("NotImplemented")
    }

    override fun clear() {
        //TODO выполняет очистку текущего состояния
    }

    private fun summarizeMessages(messageHistory: List<Message>): String {
        // TODO тут происходит процесс ужимки истории и возвращение результата
        return error("Not implemented")
    }
}