package com.cinetrack.domain.repository

import com.cinetrack.domain.model.Credit
import com.cinetrack.domain.model.Person
import com.cinetrack.util.Result
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getPersonById(id: Long): Flow<Person?>
    suspend fun getPersonByTmdbId(tmdbId: Int): Person?
    fun getFollowedPersons(): Flow<List<Person>>
    suspend fun syncPersonFromApi(tmdbId: Int): Result<Person>
    suspend fun toggleFollow(personId: Long, follow: Boolean)
    suspend fun getPersonCredits(tmdbId: Int): Result<List<Credit>>
    suspend fun checkForNewProjects(personId: Long): List<String>
}
