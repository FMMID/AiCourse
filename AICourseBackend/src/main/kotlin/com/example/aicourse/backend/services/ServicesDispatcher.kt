package com.example.aicourse.backend.services

import com.example.aicourse.backend.services.notes.NotesSummaryScheduler

fun startServices() {
    NotesSummaryScheduler.start()
}