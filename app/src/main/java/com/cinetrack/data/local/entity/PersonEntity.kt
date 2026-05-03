package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "people",
    indices = [
        Index(value = ["tmdbId"], unique = true),
        Index(value = ["isFollowed"])
    ]
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val name: String,
    val profilePath: String? = null,
    val biography: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val knownForDepartment: String? = null,
    val isFollowed: Boolean = false,
    val lastKnownCreditsJson: String? = null,
    val followedAt: Long? = null
)
