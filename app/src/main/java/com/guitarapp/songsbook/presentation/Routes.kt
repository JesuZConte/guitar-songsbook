package com.guitarapp.songsbook.presentation

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val ADD_SONG = "add_song"
    const val EDIT_SONG = "edit_song/{songId}"
    const val PREVIEW = "preview"
    const val READER = "reader/{songId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    const val ALL_SONGS = "all_songs"

    const val ADD_VERSION = "add_version/{songId}/{sourceVersionId}"
    const val EDIT_VERSION = "edit_version/{versionId}"
    const val SETLIST = "setlist/{playlistId}"

    fun reader(songId: String): String = "reader/$songId"
    fun editSong(songId: String): String = "edit_song/$songId"
    fun playlistDetail(playlistId: Long): String = "playlist/$playlistId"
    fun setlist(playlistId: Long): String = "setlist/$playlistId"
    fun addVersion(songId: String, sourceVersionId: Long): String = "add_version/$songId/$sourceVersionId"
    fun editVersion(versionId: Long): String = "edit_version/$versionId"
}
