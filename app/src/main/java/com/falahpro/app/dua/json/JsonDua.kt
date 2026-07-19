package com.falahpro.app.dua.json

data class JsonDua(
    val id: Int,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val whenToRecite: String,
    val reference: JsonReference,
    val repeat: Int
)