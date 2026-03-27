package com.guitarapp.songsbook.presentation

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val READER = "reader/{songId}"

    fun reader(songId: String): String = "reader/$songId"
    fun playlistDetail(playlistId: Long): String = "playlist/$playlistId"
}
