package com.guitarapp.songsbook.presentation

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val ADD_SONG = "add_song"
    const val EDIT_SONG = "edit_song/{songId}"
    const val PREVIEW = "preview"
    const val READER = "reader/{songId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun reader(songId: String): String = "reader/$songId"
    fun editSong(songId: String): String = "edit_song/$songId"
    fun playlistDetail(playlistId: Long): String = "playlist/$playlistId"
}
