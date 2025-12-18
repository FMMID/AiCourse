package com.example.aicourse.mcpclient

enum class McpClientType(val impName: String, val impVersion: String, val serverUrl: String) {
    NOTE_SERVICE(
        impName = "AndroidAnimeClient",
        impVersion = "1.0.0",
        serverUrl = "http://10.0.2.2:8080/sse"
    ),
    SEND_INFORMATION_SERVICE(
        impName = "AndroidAnimeClient",
        impVersion = "1.0.0",
        serverUrl = "http://10.0.2.2:8081/sse"
    )
}