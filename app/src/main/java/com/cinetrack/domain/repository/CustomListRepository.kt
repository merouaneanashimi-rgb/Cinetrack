package com.cinetrack.domain.repository

import com.cinetrack.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CustomListRepository {
    fun getAllLists(): Flow<List<CustomList>>
    fun getListById(id: Long): Flow<CustomList?>
    suspend fun createList(name: String, description: String?, emoji: String?, colorHex: String?): Long
    suspend fun updateList(list: CustomList)
    suspend fun deleteList(listId: Long)
    fun getListItems(listId: Long): Flow<List<CustomListItem>>
    suspend fun addItemToList(listId: Long, mediaId: Long, mediaType: MediaType)
    suspend fun removeItemFromList(listId: Long, mediaId: Long, mediaType: MediaType)
    suspend fun reorderItems(listId: Long, itemIds: List<Long>)
}
