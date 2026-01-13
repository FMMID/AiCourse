package com.example.aicourse.coretools

class ToolExecutor(
    private val registry: ToolRegistry,
    private val interceptors: List<ToolInterceptor>
) {
    /**
     * Выполняет инструмент по имени с заданными аргументами.
     */
    suspend fun execute(toolName: String, args: Map<String, Any>): ToolResult {
        val tool = registry.getTool(toolName)
            ?: return ToolResult.Error("Инструмент '$toolName' не зарегистрирован")

        // Запуск цепочки: первый интерцептор получит этот chain
        val chain = RealToolChain(tool, args, interceptors, 0)
        return chain.proceed(args)
    }
}