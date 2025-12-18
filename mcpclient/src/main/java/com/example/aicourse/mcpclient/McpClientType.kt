package com.example.aicourse.mcpclient

enum class McpClientType(val impName: String, val impVersion: String, val serverUrl: String) {
    LOCAL_WEATHER(
        impName = "AndroidMcpClient",
        impVersion = "1.0.0",
        serverUrl = "http://10.0.2.2:3000/sse"
    ),
    ANIME_SEARCH(
        impName = "AndroidAnimeClient",
        impVersion = "1.0.0",
        serverUrl = "${UserSession.BASE_URL}/sse"
    )
}