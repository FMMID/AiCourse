package com.example.aicourse.prompt.projectAssistant

import android.content.Context
import android.util.Log
import com.example.aicourse.mcpclient.McpClient
import com.example.aicourse.prompt.DynamicSystemPrompt
import com.example.aicourse.prompt.R
import com.example.aicourse.prompt.plain.PlainTextResponse
import com.example.aicourse.prompt.utils.ResourceReader
import com.example.aicourse.rag.domain.model.DocumentChunk
import kotlinx.coroutines.runBlocking

class ProjectAssistantPrompt(
    private val mcpGitClient: McpClient,
    override val temperature: Float = 0.4f, // Пониже для точности
    override val topP: Float = 0.1f,
    override val maxTokens: Int = 2048
) : DynamicSystemPrompt<PlainTextResponse> {

    // Сюда стратегия будет подкладывать найденные чанки
    var ragDocumentChunks: List<DocumentChunk> = emptyList()

    override fun matches(message: String): Boolean {
        return message.trim().lowercase().startsWith("/help")
    }

    override fun loadSystemPrompt(context: Context): String? {
        val rawTemplate = ResourceReader.readRawResource(context, R.raw.project_assistant_prompt)

        val (toolsInfo, gitStatus) = try {
            runBlocking {
                mcpGitClient.connect()
                val tools = mcpGitClient.getTools()
                val toolsStr = tools.joinToString("\n") { "- ${it.name}: ${it.description}" }

                val statusDeferred = try {
                    mcpGitClient.callTool("status", emptyMap()).content.toString()
                } catch (e: Exception) {
                    "Не удалось получить статус: ${e.message}"
                }

                val branchDeferred = try {
                    mcpGitClient.callTool("branch_list", emptyMap()).content.toString()
                } catch (e: Exception) {
                    "Не удалось получить список веток: ${e.message}"
                }

                val combinedStatus = """
                    === GIT BRANCHES ===
                    $branchDeferred
                    
                    === GIT STATUS ===
                    $statusDeferred
                """.trimIndent()

                Pair(toolsStr, combinedStatus)
            }
        } catch (e: Exception) {
            Log.e("ProjectAssistant", "Error fetching git info", e)
            Pair("Инструменты недоступны: ${e.message}", "Статус репозитория неизвестен.")
        }

        // 3. Формируем RAG контекст
        val ragContext = if (ragDocumentChunks.isNotEmpty()) {
            ragDocumentChunks.mapIndexed { index, chunk ->
                "[Doc ${index + 1}] (${chunk.source}): ${chunk.text}"
            }.joinToString("\n\n")
        } else {
            "Нет релевантной документации."
        }

        // 4. Подставляем всё в шаблон
        return rawTemplate
            .replace("{{MCP_TOOLS}}", toolsInfo)
            .replace("{{GIT_STATUS}}", gitStatus)
            .replace("{{RAG_CONTEXT}}", ragContext)
    }

    override fun parseResponse(rawResponse: String): PlainTextResponse {
        return PlainTextResponse(rawResponse)
    }

    // Вырезаем саму команду /help из сообщения пользователя, чтобы в LLM ушел только вопрос
    fun extractQuestion(message: String): String {
        return message.removePrefix("/help").trim()
    }
}