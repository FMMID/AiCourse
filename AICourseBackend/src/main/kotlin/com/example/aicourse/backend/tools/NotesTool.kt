package com.example.aicourse.backend.tools

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
        val userId = callToolRequest.arguments?.get("userId")?.jsonPrimitive?.content ?: "unknown_user"

        val (resultText, isError) = if (task != null) {
            NotesService.addNote(userId, task) to false
        } else {
            "No data found in property: \"text\"" to true
        }

        CallToolResult(
            content = listOf(TextContent(text = resultText)),
            isError = isError
        )
    }
}

private fun Server.registerGetRecentNotes() {
    addTool(
        name = "get_recent_notes",
        description = "Retrieves all saved notes and tasks from the database. Use this to create a summary or remind the user about their plans.",
        inputSchema = ToolSchema(properties = null)
    ) { callToolRequest ->
        val userId = callToolRequest.arguments?.get("userId")?.jsonPrimitive?.content ?: "unknown_user"
        val notes = NotesService.getAllNotes(userId)
        val notesText = if (notes.isEmpty()) {
            "Список заметок пуст."
        } else {
            notes.joinToString("\n") { "- ${it.text} (saved at ${it.timestamp})" }
        }

        CallToolResult(
            content = listOf(TextContent(text = notesText))
        )
    }
}