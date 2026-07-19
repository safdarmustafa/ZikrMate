package com.falahpro.app.dua.json

data class JsonCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val file: String,
    val count: Int,
    val featured: Boolean,
    val order: Int
)