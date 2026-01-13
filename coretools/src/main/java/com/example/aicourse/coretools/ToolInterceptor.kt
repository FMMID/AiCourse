package com.example.aicourse.coretools

interface ToolInterceptor {
    suspend fun intercept(chain: Chain): ToolResult

    interface Chain {
        val tool: Tool
        val args: Map<String, Any>
        suspend fun proceed(args: Map<String, Any>): ToolResult
    }
}