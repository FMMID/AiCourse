package com.example.aicourse.mcpclient

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

internal class RemoteMcpClientService(mcpClientType: McpClientType) : McpClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        install(SSE)
    }

    private val transport = SseClientTransport(
        client = httpClient,
        urlString = mcpClientType.serverUrl
    )

    private val mcpClient = Client(
        clientInfo = Implementation(
            name = mcpClientType.impName,
            version = mcpClientType.impVersion
        )
    )

    override suspend fun getTools(): List<Tool> {
        val result = mcpClient.listTools()
        return result.tools
    }

    override suspend fun callTool(name: String, arguments: Map<String, Any?>): CallToolResult {
        return mcpClient.callTool(
            CallToolRequest(
                CallToolRequestParams(
                    name = name,
                    arguments = buildJsonObject {
                        arguments.entries.forEach { (key, value) ->
                            put(key, value.toJsonElement())
                        }
                    }
                )
            )
        )
    }

    override suspend fun connect() {
        mcpClient.connect(transport)
    }

    override fun shutdown() {
        scope.cancel()
        httpClient.close()
    }

    fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is JsonElement -> this
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Map<*, *> -> buildJsonObject {
            this@toJsonElement.forEach { (key, value) ->
                put(key.toString(), value.toJsonElement())
            }
        }

        is List<*> -> buildJsonArray {
            this@toJsonElement.forEach { add(it.toJsonElement()) }
        }

        else -> JsonPrimitive(toString())
    }
}
