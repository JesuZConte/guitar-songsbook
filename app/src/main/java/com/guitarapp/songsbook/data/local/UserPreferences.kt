package com.guitarapp.songsbook.data.local

import android.content.Context
import android.content.SharedPreferences
import com.guitarapp.songsbook.utils.NotationSystem

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object UserPreferences {

    private const val PREFS_NAME = "guitar_songbook_prefs"
    private const val KEY_NOTATION = "chord_notation"
    private const val KEY_THEME = "theme_mode"

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
}
