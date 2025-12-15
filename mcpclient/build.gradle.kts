plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}


dependencies {
    api(libs.mcp.kotlin.sdk)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

//    implementation(libs.mcp.anthropic)  //TODO пока используем для примера
//    implementation(libs.mcp.logging) //TODO пока используем для примера
}