plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.example.aicourse.backend"
version = "0.0.1"

application {
    mainClass.set("com.example.aicourse.backend.ApplicationKt")
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.sse)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.server.cors)

    // Ktor client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    // MCP SDK
    implementation(libs.mcp.kotlin.sdk)

    // Logging
    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test.junit)
}
