package com.guitarapp.songsbook.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0
)
