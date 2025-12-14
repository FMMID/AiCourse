package com.example.aicourse.domain.base

interface BaseUseCase<INPUT, OUTPUT> {

    suspend operator fun invoke(input: INPUT): Result<OUTPUT>
}