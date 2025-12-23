package com.example.aicourse.rag.di

import android.app.Application
import com.example.aicourse.rag.data.RagRepositoryImp
import com.example.aicourse.rag.domain.RagRepository
import com.example.aicourse.rag.presentation.RagViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val ragModule = module {

    single<RagRepository> { RagRepositoryImp(androidContext()) }

    viewModel { RagViewModel(application = androidContext() as Application) }
}