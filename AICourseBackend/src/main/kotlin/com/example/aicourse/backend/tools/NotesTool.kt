package com.example.aicourse.backend.tools

import com.example.aicourse.backend.services.notes.FALLBACK_USER
import com.example.aicourse.backend.services.notes.NotesService
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

fun Server.registerNotesTools() {
    registerAddNoteTool()
    registerGetRecentNotes()
    registerCompleteNoteTool()
}

private fun Server.registerAddNoteTool() {
    addTool(
        name = "add_note",
        description = "Saves a user's note, task, reminder, or plan to the database. Use this when user asks to remember something.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("text") {
                    put("type", "string")
                    put("description", "The content of the note or task to save")
                }
            },
            required = listOf("text")
        )
    ) { callToolRequest ->
        val task = callToolRequest.arguments?.get("text")?.jsonPrimitive?.content
        val userId = callToolRequest.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER

        val (resultText, isError) = if (task != null) {
            NotesService.addNote(userId, task) to false
        } else {
            "No data found in property: \"text\"" to true
        }

        println("MCP: Adding note for userId='$userId'. Task='$task'")

        CallToolResult(
            content = listOf(TextContent(text = resultText)),
            isError = isError
        )
    }
}

private fun Server.registerGetRecentNotes() {
    addTool(
        name = "get_recent_notes",
        description = "Retrieves all notes. Shows [x] for completed and [ ] for active tasks.",
        inputSchema = ToolSchema(properties = null)
    ) { callToolRequest ->
        val userId = callToolRequest.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER
        val notes = NotesService.getAllNotes(userId)

        val notesText = if (notes.isEmpty()) {
            "Список задач пуст."
        } else {
            notes.joinToString("\n") { note ->
                val status = if (note.isCompleted) "[x]" else "[ ]"
                "$status ${note.text} (от ${java.util.Date(note.timestamp)})"
            }
        }

        CallToolResult(content = listOf(TextContent(text = notesText)))
    }
}

private fun Server.registerCompleteNoteTool() {
    addTool(
        name = "complete_note",
        description = "Marks a task as completed. Use this when the user says they finished something.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("text_snippet") {
                    put("type", "string")
                    put("description", "A part of the task text to identify it (e.g. 'milk' for 'Buy milk')")
                }
            },
            required = listOf("text_snippet")
        )
    ) { callToolRequest ->
        val snippet = callToolRequest.arguments?.get("text_snippet")?.jsonPrimitive?.content
        val userId = callToolRequest.arguments?.get("userId")?.jsonPrimitive?.content ?: FALLBACK_USER

        val (resultText, isError) = if (snippet != null) {
            NotesService.markNoteAsCompleted(userId, snippet) to false
        } else {
            "Argument 'text_snippet' is missing" to true
        }

        CallToolResult(content = listOf(TextContent(text = resultText)), isError = isError)
    }
}