package com.guitarapp.songsbook.data.local

import android.content.Context
import android.content.SharedPreferences
import com.guitarapp.songsbook.utils.NotationSystem

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object UserPreferences {

    private const val PREFS_NAME = "guitar_songbook_prefs"
    private const val KEY_NOTATION = "chord_notation"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_FONT_SIZE = "reader_font_size"
    private const val DEFAULT_FONT_SIZE = 14
    private const val KEY_LANGUAGE = "app_language"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getNotation(context: Context): NotationSystem {
        val value = prefs(context).getString(KEY_NOTATION, NotationSystem.AMERICAN.name)
        return try {
            NotationSystem.valueOf(value ?: NotationSystem.AMERICAN.name)
        } catch (_: IllegalArgumentException) {
            NotationSystem.AMERICAN
        }
    }

    fun setNotation(context: Context, notation: NotationSystem) {
        prefs(context).edit().putString(KEY_NOTATION, notation.name).apply()
    }

    fun getThemeMode(context: Context): ThemeMode {
        val value = prefs(context).getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME, mode.name).apply()
    }

    fun getFontSize(context: Context): Int =
        prefs(context).getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)

    fun setFontSize(context: Context, size: Int) {
        prefs(context).edit().putInt(KEY_FONT_SIZE, size).apply()
    }

    /** Returns the explicitly chosen language code ("en"/"es"), or null if never set by the user. */
    fun getLanguage(context: Context): String? =
        prefs(context).getString(KEY_LANGUAGE, null)

    fun setLanguage(context: Context, code: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, code).apply()
    }
}
