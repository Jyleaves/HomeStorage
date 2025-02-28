// ItemDeserializer.kt
package com.example.homestorage.util

import com.example.homestorage.data.Item
import com.google.gson.*
import java.lang.reflect.Type

class ItemDeserializer : JsonDeserializer<Item> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Item {
        val jsonObject = json.asJsonObject

        val id = if (jsonObject.has("id")) jsonObject.get("id").asInt else 0
        val name = jsonObject.get("name")?.asString ?: ""
        val room = jsonObject.get("room")?.asString ?: ""
        val container = jsonObject.get("container")?.asString ?: ""
        val subContainer = if (jsonObject.has("subContainer") && !jsonObject.get("subContainer").isJsonNull)
            jsonObject.get("subContainer").asString
        else null
        val thirdContainer = if (jsonObject.has("thirdContainer") && !jsonObject.get("thirdContainer").isJsonNull)
            jsonObject.get("thirdContainer").asString
        else null
        val category = jsonObject.get("category")?.asString ?: ""
        val description = jsonObject.get("description")?.asString ?: ""

        // 优先使用新字段 photoUris，如果不存在，再检测旧字段 photoUri
        val photoUris: List<String> = when {
            jsonObject.has("photoUris") && jsonObject.get("photoUris").isJsonArray -> {
                jsonObject.getAsJsonArray("photoUris").mapNotNull {
                    if (!it.isJsonNull) it.asString else null
                }
            }
            jsonObject.has("photoUri") && !jsonObject.get("photoUri").isJsonNull -> {
                listOf(jsonObject.get("photoUri").asString)
            }
            else -> emptyList()
        }

        val productionDate = if (jsonObject.has("productionDate") && !jsonObject.get("productionDate").isJsonNull)
            jsonObject.get("productionDate").asLong else null
        val reminderDays = if (jsonObject.has("reminderDays") && !jsonObject.get("reminderDays").isJsonNull)
            jsonObject.get("reminderDays").asLong else null
        val quantity = if (jsonObject.has("quantity") && !jsonObject.get("quantity").isJsonNull)
            jsonObject.get("quantity").asInt else null
        val timestamp = if (jsonObject.has("timestamp") && !jsonObject.get("timestamp").isJsonNull)
            jsonObject.get("timestamp").asLong else System.currentTimeMillis()
        val expirationDate = if (jsonObject.has("expirationDate") && !jsonObject.get("expirationDate").isJsonNull)
            jsonObject.get("expirationDate").asLong else null

        return Item(
            id = id,
            name = name,
            room = room,
            container = container,
            subContainer = subContainer,
            thirdContainer = thirdContainer,
            category = category,
            description = description,
            photoUris = photoUris,
            productionDate = productionDate,
            reminderDays = reminderDays,
            quantity = quantity,
            timestamp = timestamp,
            expirationDate = expirationDate
        )
    }
}
