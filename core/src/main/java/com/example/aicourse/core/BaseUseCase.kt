package com.example.aicourse.core

interface BaseUseCase<INPUT, OUTPUT> {

    suspend operator fun invoke(input: INPUT): Result<OUTPUT>
}