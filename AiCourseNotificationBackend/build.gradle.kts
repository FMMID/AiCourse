plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

group = "com.example.aicourse.backend.notification"
version = "1.0.0"

application {
    mainClass.set("com.example.aicourse.backend.notification.ApplicationKt")
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.sse)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization)


    // MCP SDK
    implementation(libs.mcp.kotlin.sdk)

    implementation(libs.logback.classic)
}