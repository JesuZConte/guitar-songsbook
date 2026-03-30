package com.guitarapp.songsbook.data.local

import android.content.Context
import android.content.SharedPreferences
import com.guitarapp.songsbook.utils.NotationSystem

object UserPreferences {

    private const val PREFS_NAME = "guitar_songbook_prefs"
    private const val KEY_NOTATION = "chord_notation"

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
}
