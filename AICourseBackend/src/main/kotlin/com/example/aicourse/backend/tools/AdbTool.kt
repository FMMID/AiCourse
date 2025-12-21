package com.example.aicourse.backend.tools

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.regex.Pattern

// Храним выбранный девайс в памяти
private var activeDeviceSerial: String? = null
private var activeDeviceModel: String? = null

fun Server.registerAdbTool() {

    val adbExecutable = System.getenv("ADB_PATH") ?: "adb"

    // --- Вспомогательные функции ---
    fun runAdbCommand(args: List<String>): String {
        return try {
            val baseCommand = mutableListOf(adbExecutable)

            // Если устройство выбрано, добавляем флаг -s <serial>
            if (activeDeviceSerial != null) {
                baseCommand.add("-s")
                baseCommand.add(activeDeviceSerial!!)
            } else {
                // Некоторые команды (как devices) не требуют выбора устройства
                // Но остальные должны падать с ошибкой, если девайс не выбран
                if (args.isNotEmpty() && args[0] != "devices") {
                    return "ERROR: No device selected. You MUST use 'adb_select_device' first."
                }
            }

            // Добавляем shell, если это не сервисная команда (типа devices)
            if (args.isNotEmpty() && args[0] != "devices") {
                baseCommand.add("shell")
            }

            baseCommand.addAll(args)

            val process = ProcessBuilder(baseCommand)
                .redirectErrorStream(true)
                .start()

            // Читаем вывод. В реальном high-load коде лучше использовать корутины для неблокирующего чтения
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

    // Парсер границ элемента из XML дампа: "[0,84][1080,268]" -> Pair(x, y) (координаты центра)
    fun getCenterCoordinates(bounds: String): Pair<Int, Int>? {
        try {
            val pattern = Pattern.compile("\\[(\\d+),(\\d+)]\\[(\\d+),(\\d+)]")
            val matcher = pattern.matcher(bounds)
            if (matcher.find()) {
                val x1 = matcher.group(1).toInt()
                val y1 = matcher.group(2).toInt()
                val x2 = matcher.group(3).toInt()
                val y2 = matcher.group(4).toInt()
                return Pair((x1 + x2) / 2, (y1 + y2) / 2)
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    // --- Инструменты управления ---

    // 1. Статус
    addTool(
        name = "adb_get_status",
        description = "Returns the currently selected Android device info or 'None'.",
        inputSchema = ToolSchema(properties = buildJsonObject { })
    ) {
        val status = if (activeDeviceSerial != null) {
            "✅ Active Device: $activeDeviceModel (Serial: $activeDeviceSerial). Ready."
        } else {
            "⚠️ No device selected. Run 'adb_list_devices' then 'adb_select_device'."
        }
        CallToolResult(content = listOf(TextContent(text = status)))
    }

    // 2. Список устройств
    addTool(
        name = "adb_list_devices",
        description = "Lists all connected Android devices and emulators.",
        inputSchema = ToolSchema(properties = buildJsonObject { })
    ) {
        // Используем ProcessBuilder напрямую для 'adb devices -l', так как runAdbCommand добавляет 'shell'
        val process = ProcessBuilder(adbExecutable, "devices", "-l")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        CallToolResult(content = listOf(TextContent(text = output)))
    }

    // 3. Выбор устройства
    addTool(
        name = "adb_select_device",
        description = "Selects a specific device to control by its serial number.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("serial") {
                    put("type", "string")
                    put("description", "Device serial number (e.g. 'emulator-5554')")
                }
                putJsonObject("model_hint") {
                    put("type", "string")
                    put("description", "Device model name for logs")
                }
            }
        )
    ) { request ->
        val serial = request.arguments?.get("serial")?.jsonPrimitive?.content ?: ""
        val model = request.arguments?.get("model_hint")?.jsonPrimitive?.content ?: "Unknown"

        if (serial.isBlank()) {
            return@addTool CallToolResult(content = listOf(TextContent("Error: Serial cannot be empty")))
        }

        activeDeviceSerial = serial
        activeDeviceModel = model
        CallToolResult(content = listOf(TextContent("Target set to: $model ($serial).")))
    }

    // --- Запуск приложения ---
    addTool(
        name = "device_open_app",
        description = "Launches an app by fuzzy searching its package name (e.g. 'youtube', 'settings', 'chrome').",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("app_name") {
                    put("type", "string")
                    put("description", "Common name of the app (e.g. 'YouTube', 'Telegram')")
                }
            }
        )
    ) { request ->
        val appName = request.arguments?.get("app_name")?.jsonPrimitive?.content?.lowercase() ?: ""
        if (appName.isBlank()) return@addTool CallToolResult(content = listOf(TextContent("Error: Empty app name")))

        println("📱 Searching for app package: $appName")

        // 1. Получаем список всех пакетов
        val allPackagesRaw = runAdbCommand(listOf("pm", "list", "packages"))

        // 2. Ищем совпадение
        val matchingPackage = allPackagesRaw.lines()
            .map { it.removePrefix("package:").trim() }
            .firstOrNull { it.contains(appName, ignoreCase = true) }

        if (matchingPackage == null) {
            return@addTool CallToolResult(content = listOf(TextContent("❌ App containing '$appName' not found installed on device.")))
        }

        // 3. Запускаем через monkey (универсальный способ запуска без знания Activity)
        println("📱 Launching package: $matchingPackage")
        runAdbCommand(listOf("monkey", "-p", matchingPackage, "-c", "android.intent.category.LAUNCHER", "1"))

        CallToolResult(content = listOf(TextContent("🚀 Launched app: $matchingPackage")))
    }

    // --- Умный поиск и ввод ---
    addTool(
        name = "device_smart_search",
        description = "Finds a search bar on screen, taps it, and types the query.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("query") {
                    put("type", "string")
                    put("description", "Text to search")
                }
            }
        )
    ) { request ->
        val query = request.arguments?.get("query")?.jsonPrimitive?.content ?: ""
        println("📱 Smart searching for: $query")

        // 1. Делаем дамп UI
        runAdbCommand(listOf("uiautomator", "dump", "/sdcard/window_dump.xml"))

        // 2. Читаем дамп
        val xmlContent = runAdbCommand(listOf("cat", "/sdcard/window_dump.xml"))

        if (xmlContent.contains("ERROR") || xmlContent.isBlank()) {
            return@addTool CallToolResult(content = listOf(TextContent("Error capturing screen UI. Is the device unlocked?")))
        }

        // 3. Ищем поле ввода (EditText или элемент с id 'search')
        // Регулярка ищет class="android.widget.EditText" и захватывает bounds="..."
        val editTextRegex = "<node[^>]*class=\"android.widget.EditText\"[^>]*bounds=\"([^\"]+)\"".toRegex()
        val searchIdRegex = "<node[^>]*resource-id=\"[^\"]*search[^\"]*\"[^>]*bounds=\"([^\"]+)\"".toRegex()

        // Сначала ищем явный EditText, если нет — любой элемент с 'search' в ID
        val bounds = editTextRegex.find(xmlContent)?.groupValues?.get(1)
            ?: searchIdRegex.find(xmlContent)?.groupValues?.get(1)

        if (bounds == null) {
            return@addTool CallToolResult(content = listOf(TextContent("❌ No search bar or input field found on current screen.")))
        }

        val coords = getCenterCoordinates(bounds)
        if (coords == null) {
            return@addTool CallToolResult(content = listOf(TextContent("❌ Failed to parse coordinates: $bounds")))
        }

        // 4. Кликаем и печатаем
        val (x, y) = coords
        println("📱 Found input at ($x, $y). Tapping...")

        runAdbCommand(listOf("input", "tap", "$x", "$y"))
        Thread.sleep(500) // Ждем фокус/клавиатуру

        val formattedText = query.replace(" ", "%s")
        runAdbCommand(listOf("input", "text", formattedText))
        runAdbCommand(listOf("input", "keyevent", "66")) // 66 = KEYCODE_ENTER

        CallToolResult(content = listOf(TextContent("✅ Tapped search bar and typed: '$query'")))
    }

    // --- Базовые навигационные инструменты ---

    addTool(
        name = "device_go_back",
        description = "Presses the system 'Back' button.",
        inputSchema = ToolSchema(properties = buildJsonObject { })
    ) {
        val res = runAdbCommand(listOf("input", "keyevent", "4"))
        CallToolResult(content = listOf(TextContent(res)))
    }

    addTool(
        name = "device_go_home",
        description = "Presses the system 'Home' button.",
        inputSchema = ToolSchema(properties = buildJsonObject { })
    ) {
        val res = runAdbCommand(listOf("input", "keyevent", "3"))
        CallToolResult(content = listOf(TextContent(res)))
    }

    addTool(
        name = "device_type_text",
        description = "Types text into focused field (use only if keyboard is already open).",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("text") { put("type", "string") }
            }
        )
    ) { req ->
        val text = req.arguments?.get("text")?.jsonPrimitive?.content ?: ""
        val formatted = text.replace(" ", "%s")
        val res = runAdbCommand(listOf("input", "text", formatted))
        CallToolResult(content = listOf(TextContent(res)))
    }

    addTool(
        name = "device_open_url",
        description = "Opens a URL (deep link) via default browser/app.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("url") { put("type", "string") }
            }
        )
    ) { req ->
        val url = req.arguments?.get("url")?.jsonPrimitive?.content ?: ""
        val res = runAdbCommand(listOf("am", "start", "-a", "android.intent.action.VIEW", "-d", url))
        CallToolResult(content = listOf(TextContent(res)))
    }
}