package com.example.aicourse.mcpclient

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig.Companion.INFINITE_TIMEOUT_MS
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

private object Log {
    fun d(tag: String, msg: String) {
        println("DEBUG: [$tag] $msg")
    }

    fun w(tag: String, msg: String, tr: Throwable? = null) {
        println("WARN: [$tag] $msg")
        tr?.printStackTrace()
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        System.err.println("ERROR: [$tag] $msg")
        tr?.printStackTrace()
    }
}

internal class RemoteMcpClientService(private val mcpClientConfig: McpClientConfig) : McpClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()
    private var mcpClient: Client? = null

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
        install(HttpTimeout) {
            requestTimeoutMillis = INFINITE_TIMEOUT_MS
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = INFINITE_TIMEOUT_MS
        }
    }

    override suspend fun getTools(): List<Tool> {
        return try {
            ensureConnected()
            mcpClient?.listTools()?.tools ?: emptyList()
        } catch (e: Exception) {
            Log.w("RemoteMcpClient", "Error getting tools, retrying connection...", e)
            resetConnection()
            ensureConnected()
            mcpClient?.listTools()?.tools ?: emptyList()
        }
    }

    override suspend fun callTool(name: String, arguments: Map<String, Any?>): CallToolResult {
        return try {
            ensureConnected()
            performCallTool(name, arguments)
        } catch (e: Exception) {
            Log.w("RemoteMcpClient", "Error calling tool, retrying connection...", e)
            resetConnection()
            ensureConnected()
            performCallTool(name, arguments)
        }
    }

    private suspend fun performCallTool(name: String, arguments: Map<String, Any?>): CallToolResult {
        return mcpClient!!.callTool(
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
        ensureConnected()
    }

    private suspend fun ensureConnected() {
        connectionMutex.withLock {
            if (mcpClient != null) return

            try {
                Log.d("RemoteMcpClient", "Creating new connection to ${mcpClientConfig.serverUrl}...")

                val transport = SseClientTransport(
                    client = httpClient,
                    urlString = mcpClientConfig.serverUrl
                )

                val client = Client(
                    clientInfo = Implementation(
                        name = "AndroidAnimeClient",
                        version = "1.0.0"
                    )
                )

                client.connect(transport)

                // Даем транспорту немного времени на инициализацию
                // (иногда connect возвращает управление чуть раньше, чем транспорт готов слать данные)
                delay(500)

                mcpClient = client
                Log.d("RemoteMcpClient", "Connected successfully")
            } catch (e: Exception) {
                Log.e("RemoteMcpClient", "Failed to connect", e)
                mcpClient = null
                throw e
            }
        }
    }

    private suspend fun resetConnection() {
        connectionMutex.withLock {
            try {
                // Если SDK поддерживает close, можно вызвать.
                // В данном случае просто сбрасываем ссылку, чтобы пересоздать.
                mcpClient = null
                Log.d("RemoteMcpClient", "Connection reset")
            } catch (e: Exception) {
                Log.e("RemoteMcpClient", "Error resetting connection", e)
            }
        }
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