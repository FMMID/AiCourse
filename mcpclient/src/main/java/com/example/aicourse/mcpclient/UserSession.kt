package com.example.aicourse.mcpclient

import java.util.UUID

//TODO убрать после рефакторинга
object UserSession {
    val CURRENT_USER_ID = UUID.randomUUID().toString()
//    const val BASE_URL = "http://10.0.2.2:8080/"
    const val BASE_URL = "https://95.81.96.66.sslip.io"
}