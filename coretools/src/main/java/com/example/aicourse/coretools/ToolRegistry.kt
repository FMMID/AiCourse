package com.example.aicourse.coretools

/**
 * Центральное хранилище всех доступных инструментов в приложении.
 * Модули :rag и :mcpclient регистрируют свои тулзы здесь при запуске.
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): Tool? = tools[name]
    fun getAllTools(): List<Tool> = tools.values.toList()
}