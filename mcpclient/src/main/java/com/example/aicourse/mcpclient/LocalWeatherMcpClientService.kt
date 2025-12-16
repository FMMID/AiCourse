package com.example.aicourse.mcpclient

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json

//TODO первая тестовая имплементация, подвязывается на локальный сервер
internal class LocalWeatherMcpClientService(mcpClientType: McpClientType) : McpClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        install(SSE)
        engine {
            requestTimeout = 30_000
        }
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

    override suspend fun connect() {
        mcpClient.connect(transport)
    }

    override fun shutdown() {
        scope.cancel()
        httpClient.close()
    }
}
