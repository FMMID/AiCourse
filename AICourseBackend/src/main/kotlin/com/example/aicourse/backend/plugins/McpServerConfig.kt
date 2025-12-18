package com.example.aicourse.backend.plugins

import com.example.aicourse.backend.tools.registerNotesTools
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities

fun createMcpServer(): Server {
    val server = Server(
        serverInfo = Implementation("BackendMcpServer", "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    server.registerNotesTools()

    return server
}