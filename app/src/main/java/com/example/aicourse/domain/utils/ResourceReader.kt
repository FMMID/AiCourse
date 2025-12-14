package com.example.aicourse.domain.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object ResourceReader {

    private val mapOfRawResources = mutableMapOf<Int, String>()

    /**
     * Читает содержимое текстового файла из res/raw
     * @param context Android context
     * @param resourceId ID ресурса из R.raw
     * @return содержимое файла в виде строки
     */
    fun readRawResource(context: Context, resourceId: Int): String {
        return mapOfRawResources.getOrPut(resourceId) {
            context.resources.openRawResource(resourceId).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        }
    }
}
