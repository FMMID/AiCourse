package com.example.aicourse.backend.services

import com.example.aicourse.backend.services.notes.NotesSummarySchedulerService
import com.example.aicourse.backend.services.notification.FirebasePushService

fun startServices() {
    NotesSummarySchedulerService.start()
    FirebasePushService.start()
}