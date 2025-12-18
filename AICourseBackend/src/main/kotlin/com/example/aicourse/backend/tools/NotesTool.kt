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

private fun Server.registerAddBatchNotesTool() {
    addTool(
        name = "add_notes_bulk",
        description = "Adds MULTIPLE notes or tasks at once. Use this when user provides a list of things to do.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("notes") {
                    put("type", "array")
                    put("description", "List of tasks texts")
                    putJsonObject("items") {
                        put("type", "string")
                    }
                }
            },
            required = listOf("notes")
        )
    ) { request ->
        val userId = request.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER

        // Парсим массив строк из JSON
        val notesArray = request.arguments?.get("notes") as? JsonArray

        val count = if (notesArray != null) {
            notesArray.forEach { jsonElement ->
                val text = jsonElement.jsonPrimitive.content
                NotesService.addNote(userId, text)
            }
            notesArray.size
        } else 0

        CallToolResult(content = listOf(TextContent(text = "Successfully added $count notes.")))
    }
}

// 2. УМНОЕ ПОЛУЧЕНИЕ (с фильтром для отчетов)
private fun Server.registerGetNotesTool() {
    addTool(
        name = "get_notes",
        description = "Retrieves notes. Can filter by status: 'active' (default), 'completed_today' (for reports), or 'all'.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("filter") {
                    put("type", "string")
                    put("enum", buildJsonArray { add("active"); add("completed_today"); add("all") })
                    put("description", "Filter mode: 'active', 'completed_today', or 'all'")
                }
            }
        )
    ) { request ->
        val userId = request.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER
        val filter = request.arguments?.get("filter")?.jsonPrimitive?.content ?: "active"

        val allNotes = NotesService.getAllNotes(userId)

        // Логика фильтрации
        val filteredNotes = when (filter) {
            "active" -> allNotes.filter { !it.isCompleted }
            "completed_today" -> { allNotes.filter { it.isCompleted } }
            else -> allNotes
        }

        val resultText = if (filteredNotes.isEmpty()) {
            "Notes list is empty for filter: $filter"
        } else {
            filteredNotes.joinToString("\n") { note ->
                val status = if (note.isCompleted) "[x]" else "[ ]"
                "$status ${note.text}"
            }
        }

        CallToolResult(content = listOf(TextContent(text = resultText)))
    }
}

private fun Server.registerCompleteBatchTool() {
    addTool(
        name = "complete_notes_bulk",
        description = "Marks MULTIPLE tasks as completed by their text snippets.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("snippets") {
                    put("type", "array")
                    putJsonObject("items") { put("type", "string") }
                }
            },
            required = listOf("snippets")
        )
    ) { request ->
        val userId = request.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER
        val snippets = request.arguments?.get("snippets") as? JsonArray

        val completedCount = snippets?.count { snippet ->
            // Тут предполагается, что markNoteAsCompleted возвращает true/false или мы просто дергаем
            // Нужно доработать NotesService, чтобы он возвращал успех операции, но пока так:
            val res = NotesService.markNoteAsCompleted(userId, snippet.jsonPrimitive.content)
            res.contains("отмечена") // Проверка по строке ответа - костыль, но сработает
        } ?: 0

        CallToolResult(content = listOf(TextContent(text = "Marked $completedCount tasks as completed.")))
    }
}