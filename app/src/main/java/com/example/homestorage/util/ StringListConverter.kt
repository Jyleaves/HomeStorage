// StringListConverter.kt
package com.example.homestorage.util

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StringListConverter {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isBlank()) emptyList()
        else json.decodeFromString<List<String>>(value).take(3)
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return json.encodeToString(list.take(3))
    }
}