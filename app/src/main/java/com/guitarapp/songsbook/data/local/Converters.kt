package com.guitarapp.songsbook.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guitarapp.songsbook.domain.model.SongSection

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromSongSections(value: List<SongSection>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSongSections(value: String): List<SongSection> {
        val type = object : TypeToken<List<SongSection>>() {}.type
        return gson.fromJson(value, type)
    }
}