package com.guitarapp.songsbook.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.domain.model.SongVersion

@Entity(
    tableName = "song_versions",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["song_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("song_id")]
)
data class SongVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "song_id")
    val songId: String,
    val name: String,
    val key: String = "",
    val capo: Int = 0,
    val chords: List<String> = emptyList(),
    val notes: String = "",
    val content: List<SongSection> = emptyList()
) {
    fun toDomain() = SongVersion(
        id = id,
        songId = songId,
        name = name,
        key = key,
        capo = capo,
        chords = chords,
        notes = notes,
        content = content
    )

    companion object {
        fun fromDomain(v: SongVersion) = SongVersionEntity(
            id = v.id,
            songId = v.songId,
            name = v.name,
            key = v.key,
            capo = v.capo,
            chords = v.chords,
            notes = v.notes,
            content = v.content
        )
    }
}
