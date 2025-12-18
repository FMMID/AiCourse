package com.example.aicourse.mcpclient

const val PROD_FMC_TOKEN = "https://95.81.96.66.sslip.io/notes/register-push-token"
const val PROD_NOTE = "https://95.81.96.66.sslip.io/notes/sse"
const val PROD_NOTIFICATION = "https://95.81.96.66.sslip.io/notify/sse"

const val LOCAL_FMC_TOKEN = "http://10.0.2.2:8080/register-push-token"
const val LOCAL_NOTE = "http://10.0.2.2:8080/sse"
const val LOCAL_NOTIFICATION = "http://10.0.2.2:8081/sse"

enum class McpClientType(val impName: String, val impVersion: String, val serverUrl: String) {
    NOTE_SERVICE(
        impName = "AndroidAnimeClient",
        impVersion = "1.0.0",
        serverUrl = PROD_NOTE
    ),
    SEND_INFORMATION_SERVICE(
        impName = "AndroidAnimeClient",
        impVersion = "1.0.0",
        serverUrl = PROD_NOTIFICATION,
    )
}