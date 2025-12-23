import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProps = Properties()
localProps.load(project.rootProject.file("local.properties").inputStream())

android {
    namespace = "com.example.aicourse"
    compileSdk = 36

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"

            buildConfigField("String", "MCP_NOTE_URL", "\"http://192.168.0.177:8080/sse\"")
            buildConfigField("String", "MCP_NOTIFICATION_URL", "\"http://192.168.0.177:8081/sse\"")
            buildConfigField("String", "REGISTER_FMC_TOKEN_URL", "\"http://192.168.0.177:8080/register-push-token\"")
        }

        create("prod") {
            dimension = "environment"

            buildConfigField("String", "MCP_NOTE_URL", "\"https://95.81.96.66.sslip.io/notes/sse\"")
            buildConfigField("String", "MCP_NOTIFICATION_URL", "\"https://95.81.96.66.sslip.io/notify/sse\"")
            buildConfigField("String", "REGISTER_FMC_TOKEN_URL", "\"https://95.81.96.66.sslip.io/notes/register-push-token\"")
        }
    }

    defaultConfig {
        applicationId = "com.example.aicourse"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GIGACHAT_AUTH_KEY",
            "\"${localProps.getProperty("GIGACHAT_AUTH_KEY")}\""
        )
        buildConfigField(
            "String",
            "HUGGING_FACE_AUTH_KEY",
            "\"${localProps.getProperty("HUGGING_FACE_AUTH_KEY", "")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Network
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)

    //Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    //Notifications
    implementation(libs.firebase.messaging.ktx)

    //MCP client
    implementation(project(":mcpclient"))

    //RAG module
    implementation(project(":rag"))

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}