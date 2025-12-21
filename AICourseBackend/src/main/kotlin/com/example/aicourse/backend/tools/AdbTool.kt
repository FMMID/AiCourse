package com.example.aicourse.backend.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject


private var activeDeviceSerial: String? = null
private var activeDeviceModel: String? = null

fun Server.registerAdbTool() {

    fun runAdbCommand(args: List<String>): String {
        return try {
            val baseCommand = mutableListOf("adb")

            // Если устройство выбрано, добавляем -s <serial>
            if (activeDeviceSerial != null) {
                baseCommand.add("-s")
                baseCommand.add(activeDeviceSerial!!)
            } else {
                // Если команда требует устройство, а его нет - это ошибка (кроме 'devices')
                if (args.isNotEmpty() && args[0] != "devices") {
                    return "ERROR: No device selected. You MUST use 'adb_select_device' first."
                }
            }

            if (args.isNotEmpty() && args[0] != "devices") {
                baseCommand.add("shell")
            }

            baseCommand.addAll(args)

            val process = ProcessBuilder(baseCommand)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                if (output.isBlank()) "Success" else output.trim()
            } else {
                "ADB Error (Code $exitCode): $output"
            }
        } catch (e: Exception) {
            "Execution failed: ${e.message}"
        }
    }

    //Получить статус об активных подключениях
    addTool(
        name = "adb_get_status",
        description = "Returns the currently selected Android device info or 'None' if no device is controlled.",
        inputSchema = ToolSchema(properties = buildJsonObject { })
    ) {
        val status = if (activeDeviceSerial != null) {
            "✅ Active Device: $activeDeviceModel (Serial: $activeDeviceSerial). Ready for commands."
        } else {
            "⚠️ No device selected. You need to run 'adb_list_devices' and then 'adb_select_device'."
        }
        CallToolResult(content = listOf(TextContent(text = status)))
    }

    //Получить список устройств
    addTool(
        name = "adb_list_devices",
        description = "Lists connected devices. Use this to find available emulators or phones.",
        inputSchema = ToolSchema(properties = buildJsonObject { })
    ) {
        // adb devices -l дает больше инфы (модель, usb и т.д.)
        val process = ProcessBuilder("adb", "devices", "-l")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }

        CallToolResult(content = listOf(TextContent(text = output)))
    }

    //Получить список устройств
    addTool(
        name = "adb_select_device",
        description = "Sets the target device for all future commands.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("serial") {
                    put("type", "string")
                    put("description", "Device serial number (e.g. 'emulator-5554')")
                }
                putJsonObject("model_hint") {
                    put("type", "string")
                    put("description", "Human readable model name (just for logs)")
                }
            }
        )
    ) { request ->
        val serial = request.arguments?.get("serial")?.jsonPrimitive?.content ?: ""
        val model = request.arguments?.get("model_hint")?.jsonPrimitive?.content ?: "Unknown"

        if (serial.isBlank()) {
            return@addTool CallToolResult(content = listOf(TextContent(text = "Error: Serial is empty")))
        }

        activeDeviceSerial = serial
        activeDeviceModel = model

        CallToolResult(content = listOf(TextContent(text = "Target set to: $model ($serial). Now you can execute commands.")))
    }

    // Инструмент: Нажатие кнопки "Назад"
    addTool(
        name = "device_go_back",
        description = "Presses the system 'Back' button on the connected Android device/emulator.",
        inputSchema = ToolSchema(
            properties = buildJsonObject { }
        )
    ) {
        println("📱 Executing: BACK button")
        val result = runAdbCommand(listOf("input", "keyevent", "4")) // 4 = KEYCODE_BACK
        CallToolResult(content = listOf(TextContent(text = result)))
    }

    // Инструмент: Нажатие кнопки "Домой"
    addTool(
        name = "device_go_home",
        description = "Presses the system 'Home' button, minimizing all apps.",
        inputSchema = ToolSchema(
            properties = buildJsonObject { }
        )
    ) {
        println("📱 Executing: HOME button")
        val result = runAdbCommand(listOf("input", "keyevent", "3")) // 3 = KEYCODE_HOME
        CallToolResult(content = listOf(TextContent(text = result)))
    }

    // Инструмент: Ввод текста
    addTool(
        name = "device_type_text",
        description = "Types text into the currently focused input field on the device.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("text") {
                    put("type", "string")
                    put("description", "The text to type (avoid special characters if possible)")
                }
            }
        )
    ) { callToolRequest ->
        val text = callToolRequest.arguments?.get("text")?.jsonPrimitive?.content ?: ""
        println("📱 Typing: $text")

        // ADB не любит пробелы в 'input text', их нужно заменять на %s
        val formattedText = text.replace(" ", "%s")
        val result = runAdbCommand(listOf("input", "text", formattedText))

        CallToolResult(content = listOf(TextContent(text = result)))
    }

    // Инструмент: Открытие URL (Deep Link)
    addTool(
        name = "device_open_url",
        description = "Opens a URL or Deep Link on the device (e.g. opens browser or YouTube).",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("url") {
                    put("type", "string")
                    put("description", "URL to open (e.g., https://www.youtube.com)")
                }
            }
        )
    ) { callToolRequest ->
        val url = callToolRequest.arguments?.get("url")?.jsonPrimitive?.content ?: ""
        println("📱 Opening URL: $url")

        // am start -a android.intent.action.VIEW -d <URL>
        val result = runAdbCommand(listOf("am", "start", "-a", "android.intent.action.VIEW", "-d", url))

        CallToolResult(content = listOf(TextContent(text = result)))
    }
}