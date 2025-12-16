package com.example.aicourse.backend.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

fun Server.registerTrackerTool() {
    addTool(
        name = "get_tracker_tasks",
        description = "Get count of open tasks from Tracker",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("status") {
                    put("type", "string")
                    put("description", "Task status filter (optional)")
                }
            },
            required = listOf("status")
        )
    ) { callToRequest ->
        // Здесь пока заглушка, позже заменим на реальный вызов
        val requestedStatus = callToRequest.arguments?.get("status")?.jsonPrimitive?.content ?: "all"
        println("🔧 Tool 'get_tracker_tasks' called with status: $requestedStatus")

        CallToolResult(
            content = listOf(
                TextContent(text = "Found 42 open tasks with status: $requestedStatus")
            )
        )
    }
}