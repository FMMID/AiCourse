package com.example.aicourse.coretools

/**
 * Реализация цепочки вызовов.
 * Управляет индексом текущего интерцептора и обеспечивает переход к следующему.
 */
internal class RealToolChain(
    override val tool: Tool,
    override val args: Map<String, Any>,
    private val interceptors: List<ToolInterceptor>,
    private val index: Int
) : ToolInterceptor.Chain {

    override suspend fun proceed(args: Map<String, Any>): ToolResult {
        // Если интерцепторы закончились — вызываем сам инструмент (финальное звено)
        if (index >= interceptors.size) {
            return tool.execute(args)
        }

        // Создаем следующую цепочку с увеличенным индексом
        val next = RealToolChain(tool, args, interceptors, index + 1)

        // Берем текущий интерцептор и передаем ему управление
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}