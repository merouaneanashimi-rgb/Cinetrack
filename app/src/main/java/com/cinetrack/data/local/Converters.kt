package com.cinetrack.data.local

import androidx.room.TypeConverter
import com.cinetrack.data.local.entity.AirStatus
import com.cinetrack.data.local.entity.MediaType
import com.cinetrack.data.local.entity.UserListType

class Converters {

    @TypeConverter
    fun fromAirStatus(value: AirStatus): String = value.name

    @TypeConverter
    fun toAirStatus(value: String): AirStatus = AirStatus.valueOf(value)

    @TypeConverter
    fun fromUserListType(value: UserListType?): String? = value?.name

    @TypeConverter
    fun toUserListType(value: String?): UserListType? = value?.let { UserListType.valueOf(it) }

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
}
