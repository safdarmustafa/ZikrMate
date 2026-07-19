package com.falahpro.app.dua.repository

import android.content.Context
import com.falahpro.app.dua.json.JsonCategory
import com.falahpro.app.dua.json.JsonDua
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DuaRepository {

    private val gson = Gson()

    fun loadCategories(context: Context): List<JsonCategory> {
        return try {
            val json = context.assets
                .open("dua/categories.json")
                .bufferedReader()
                .use { it.readText() }

            if (json.isBlank()) return emptyList()

            val type = object : TypeToken<List<JsonCategory>>() {}.type
            val categories: List<JsonCategory>? = gson.fromJson(json, type)
            categories.orEmpty().sortedBy { it.order }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadDuas(
        context: Context,
        fileName: String
    ): List<JsonDua> {
        if (fileName.isBlank()) return emptyList()

        return try {
            val json = context.assets
                .open("dua/$fileName")
                .bufferedReader()
                .use { it.readText() }

            if (json.isBlank()) return emptyList()

            val type = object : TypeToken<List<JsonDua>>() {}.type
            val duas: List<JsonDua>? = gson.fromJson(json, type)
            duas.orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
