package com.example.aicourse.backend.tools

import com.example.aicourse.backend.services.notes.FALLBACK_USER
import com.example.aicourse.backend.services.notes.NotesService
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*

fun Server.registerNotesTools() {
    registerAddBatchNotesTool()
    registerGetNotesTool()
    registerCompleteBatchTool()
}

/**
 * 1. ПАКЕТНОЕ ДОБАВЛЕНИЕ
 * Позволяет добавить сразу список дел за один запрос.
 */
private fun Server.registerAddBatchNotesTool() {
    addTool(
        name = "add_notes_bulk",
        description = "Adds MULTIPLE notes or tasks at once. Expects a JSON array of strings. Use this when user provides a list.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("notes") {
                    put("type", "array")
                    put("description", "List of tasks texts, e.g. [\"Buy milk\", \"Wash car\"]")
                    putJsonObject("items") {
                        put("type", "string")
                    }
                }
            },
            required = listOf("notes")
        )
    ) { request ->
        val userId = request.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER

        // Получаем массив из аргументов
        val notesArray = request.arguments?.get("notes") as? JsonArray

        val count = if (notesArray != null) {
            notesArray.forEach { jsonElement ->
                val text = jsonElement.jsonPrimitive.content
                // Используем существующий сервис
                NotesService.addNote(userId, text)
            }
            notesArray.size
        } else 0

        println("MCP: Added $count notes for $userId")

        CallToolResult(content = listOf(TextContent(text = "Successfully added $count notes.")))
    }
}

/**
 * 2. УМНОЕ ПОЛУЧЕНИЕ ЗАДАЧ
 * Поддерживает фильтрацию: только активные, только выполненные (для отчетов) или все.
 */
private fun Server.registerGetNotesTool() {
    addTool(
        name = "get_notes",
        description = "Retrieves notes. REQUIRED argument 'filter': 'active' (what to do), 'completed' (what is done), or 'all'.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("filter") {
                    put("type", "string")
                    put("enum", buildJsonArray {
                        add("active")
                        add("completed")
                        add("all")
                    })
                    put("description", "Filter mode: 'active' (default), 'completed', or 'all'")
                }
            },
            required = listOf("filter")
        )
    ) { request ->
        val userId = request.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER
        val filter = request.arguments?.get("filter")?.jsonPrimitive?.content ?: "active"

        val allNotes = NotesService.getAllNotes(userId)

        // Логика фильтрации
        val filteredNotes = when (filter) {
            "active" -> allNotes.filter { !it.isCompleted }
            "completed" -> allNotes.filter { it.isCompleted }
            else -> allNotes
        }

        val resultText = if (filteredNotes.isEmpty()) {
            "No notes found for filter: $filter"
        } else {
            // Формируем красивый список
            filteredNotes.joinToString("\n") { note ->
                val status = if (note.isCompleted) "[x]" else "[ ]"
                // Можно добавить дату, если нужно: java.util.Date(note.timestamp)
                "$status ${note.text}"
            }
        }

        println("MCP: Get notes ($filter) for $userId -> found ${filteredNotes.size}")

        CallToolResult(content = listOf(TextContent(text = resultText)))
    }
}

/**
 * 3. ПАКЕТНОЕ ЗАВЕРШЕНИЕ
 * Позволяет отметить сразу несколько задач ("Купил молоко и хлеб")
 */
private fun Server.registerCompleteBatchTool() {
    addTool(
        name = "complete_notes_bulk",
        description = "Marks MULTIPLE tasks as completed. Expects an array of text snippets to identify tasks.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("snippets") {
                    put("type", "array")
                    put("description", "List of keywords to find tasks, e.g. [\"milk\", \"bread\"]")
                    putJsonObject("items") {
                        put("type", "string")
                    }
                }
            },
            required = listOf("snippets")
        )
    ) { request ->
        val userId = request.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER
        val snippets = request.arguments?.get("snippets") as? JsonArray

        val completedCount = snippets?.count { snippet ->
            val textToSearch = snippet.jsonPrimitive.content
            // Вызываем обновленный метод сервиса, который возвращает Boolean
            NotesService.markNoteAsCompleted(userId, textToSearch)
        } ?: 0

        println("MCP: Completed $completedCount tasks for $userId")

        CallToolResult(content = listOf(TextContent(text = "Successfully marked $completedCount tasks as completed.")))
    }
}
