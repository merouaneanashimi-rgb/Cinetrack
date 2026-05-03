package com.cinetrack.data.repository

import com.cinetrack.data.local.dao.CustomListDao
import com.cinetrack.data.local.dao.CustomListItemDao
import com.cinetrack.data.local.entity.CustomListEntity
import com.cinetrack.data.local.entity.CustomListItemEntity
import com.cinetrack.data.local.entity.MediaType
import com.cinetrack.domain.model.CustomList
import com.cinetrack.domain.model.CustomListItem
import com.cinetrack.domain.repository.CustomListRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomListRepositoryImpl @Inject constructor(
    private val customListDao: CustomListDao,
    private val customListItemDao: CustomListItemDao
) : CustomListRepository {

    override fun getAllLists(): Flow<List<CustomList>> {
        return customListDao.getAll().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getListById(id: Long): Flow<CustomList?> {
        return customListDao.getById(id).map { it?.toDomainModel() }
    }

    override suspend fun createList(name: String, description: String?, emoji: String?, colorHex: String?): Long {
        val entity = CustomListEntity(
            name = name,
            description = description,
            emoji = emoji,
            colorHex = colorHex,
            sortOrder = (customListDao.getCount()) * 10
        )
        return customListDao.insert(entity)
    }

    override suspend fun updateList(list: CustomList) {
        customListDao.update(list.toEntity())
    }

    override suspend fun deleteList(listId: Long) {
        customListItemDao.deleteByList(listId)
        customListDao.deleteById(listId)
    }

    override fun getListItems(listId: Long): Flow<List<CustomListItem>> {
        return customListItemDao.getByListId(listId).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun addItemToList(listId: Long, mediaId: Long, mediaType: com.cinetrack.domain.model.MediaType) {
        val type = when (mediaType) {
            com.cinetrack.domain.model.MediaType.MOVIE -> MediaType.MOVIE
            com.cinetrack.domain.model.MediaType.SHOW -> MediaType.SHOW
        }
        if (!customListItemDao.existsInList(listId, mediaId, type)) {
            val count = customListItemDao.getCountByList(listId)
            val item = CustomListItemEntity(
                listId = listId,
                mediaId = mediaId,
                mediaType = type,
                sortIndex = count
            )
            customListItemDao.insert(item)
        }
    }

    override suspend fun removeItemFromList(listId: Long, mediaId: Long, mediaType: com.cinetrack.domain.model.MediaType) {
        val type = when (mediaType) {
            com.cinetrack.domain.model.MediaType.MOVIE -> MediaType.MOVIE
            com.cinetrack.domain.model.MediaType.SHOW -> MediaType.SHOW
        }
        customListItemDao.deleteFromList(listId, mediaId, type)
    }

    override suspend fun reorderItems(listId: Long, itemIds: List<Long>) {
        itemIds.forEachIndexed { index, itemId ->
            customListItemDao.updateSortIndex(itemId, index)
        }
    }

    private fun CustomListEntity.toDomainModel() = CustomList(
        id = id,
        name = name,
        description = description,
        emoji = emoji,
        colorHex = colorHex,
        createdAt = createdAt,
        sortOrder = sortOrder,
        filterJson = filterJson,
        isPublic = isPublic
    )

    private fun CustomList.toEntity() = CustomListEntity(
        id = id,
        name = name,
        description = description,
        emoji = emoji,
        colorHex = colorHex,
        createdAt = createdAt,
        sortOrder = sortOrder,
        filterJson = filterJson,
        isPublic = isPublic
    )

    private fun CustomListItemEntity.toDomainModel() = CustomListItem(
        id = id,
        listId = listId,
        mediaId = mediaId,
        mediaType = when (mediaType) {
            MediaType.MOVIE -> com.cinetrack.domain.model.MediaType.MOVIE
            MediaType.SHOW -> com.cinetrack.domain.model.MediaType.SHOW
        },
        addedAt = addedAt,
        sortIndex = sortIndex
    )
}
