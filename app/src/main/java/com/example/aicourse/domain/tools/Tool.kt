package com.example.aicourse.domain.tools

interface Tool<PROCESS_DATA> {

    fun processData(processData: PROCESS_DATA): ToolResult

    fun clear()
}