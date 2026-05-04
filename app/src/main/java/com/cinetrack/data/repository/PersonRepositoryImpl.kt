package com.cinetrack.data.repository

import com.cinetrack.data.local.dao.PersonDao
import com.cinetrack.data.local.entity.PersonEntity
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.domain.model.Person
import com.cinetrack.domain.model.Credit
import com.cinetrack.domain.repository.PersonRepository
import com.cinetrack.util.Result
import com.cinetrack.util.safeApiCall
import kotlinx.coroutines.flow.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepositoryImpl @Inject constructor(
    private val personDao: PersonDao,
    private val api: TmdbApiService
) : PersonRepository {

    override fun getPersonById(id: Long): Flow<Person?> {
        return personDao.getById(id).map { it?.toDomainModel() }
    }

    override suspend fun getPersonByTmdbId(tmdbId: Int): Person? {
        return personDao.getByTmdbId(tmdbId)?.toDomainModel()
    }

    override fun getFollowedPersons(): Flow<List<Person>> {
        return personDao.getFollowed().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun syncPersonFromApi(tmdbId: Int): Result<Person> {
        return safeApiCall {
            val response = api.getPersonDetail(tmdbId)
            if (response.isSuccessful) {
                val detail = response.body() ?: throw Exception("Empty response")
                val existing = personDao.getByTmdbId(tmdbId)
                val entity = PersonEntity(
                    id = existing?.id ?: 0,
                    tmdbId = detail.id,
                    name = detail.name,
                    profilePath = detail.profilePath,
                    biography = detail.biography,
                    birthday = detail.birthday,
                    deathday = detail.deathday,
                    placeOfBirth = detail.placeOfBirth,
                    knownForDepartment = detail.knownForDepartment,
                    isFollowed = existing?.isFollowed ?: false,
                    followedAt = existing?.followedAt,
                    lastKnownCreditsJson = existing?.lastKnownCreditsJson
                )
                val personId = personDao.insert(entity)
                entity.copy(id = personId).toDomainModel()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun toggleFollow(personId: Long, follow: Boolean) {
        val person = personDao.getById(personId).first() ?: return
        val creditsResponse = api.getPersonCombinedCredits(person.tmdbId)
        val creditsJson = if (creditsResponse.isSuccessful) {
            creditsResponse.body()?.let { dto ->
            try {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(com.cinetrack.data.remote.dto.CombinedCreditsDto::class.java)
                adapter.toJson(dto)
            } catch (e: Exception) { null }
        }
        } else null
        personDao.updateFollowStatus(
            personId,
            follow,
            if (follow) System.currentTimeMillis() else null,
            creditsJson
        )
    }

    override suspend fun getPersonCredits(tmdbId: Int): Result<List<Credit>> {
        return safeApiCall {
            val response = api.getPersonCombinedCredits(tmdbId)
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Empty response")
                val credits = mutableListOf<Credit>()
                body.cast?.forEach { cast ->
                    credits.add(Credit(
                        id = cast.id,
                        title = cast.title ?: cast.name ?: "",
                        mediaType = cast.mediaType ?: "movie",
                        character = cast.character,
                        job = null,
                        department = cast.department,
                        posterPath = cast.posterPath,
                        releaseDate = cast.releaseDate ?: cast.firstAirDate,
                        voteAverage = cast.voteAverage
                    ))
                }
                body.crew?.forEach { crew ->
                    credits.add(Credit(
                        id = crew.id,
                        title = crew.title ?: crew.name ?: "",
                        mediaType = crew.mediaType ?: "movie",
                        character = null,
                        job = crew.job,
                        department = crew.department,
                        posterPath = crew.posterPath,
                        releaseDate = crew.releaseDate ?: crew.firstAirDate,
                        voteAverage = crew.voteAverage
                    ))
                }
                credits.sortedByDescending { it.releaseDate }
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun checkForNewProjects(personId: Long): List<String> {
        val person = personDao.getById(personId).first() ?: return emptyList()
        if (!person.isFollowed) return emptyList()

        val response = api.getPersonCombinedCredits(person.tmdbId)
        if (!response.isSuccessful) return emptyList()

        val currentCredits = response.body() ?: return emptyList()
        val currentIds = mutableSetOf<Int>()
        currentCredits.cast?.forEach { currentIds.add(it.id) }
        currentCredits.crew?.forEach { currentIds.add(it.id) }

        val previousJson = person.lastKnownCreditsJson ?: return emptyList()
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(com.cinetrack.data.remote.dto.CombinedCreditsDto::class.java)
            val previous = adapter.fromJson(previousJson) ?: return emptyList()
            val previousIds = mutableSetOf<Int>()
            previous.cast?.forEach { previousIds.add(it.id) }
            previous.crew?.forEach { previousIds.add(it.id) }

            val newIds = currentIds - previousIds
            val newTitles = mutableListOf<String>()
            currentCredits.cast?.filter { it.id in newIds }?.forEach { newTitles.add(it.title ?: it.name ?: "") }
            currentCredits.crew?.filter { it.id in newIds }?.forEach { newTitles.add(it.title ?: it.name ?: "") }

            if (newTitles.isNotEmpty()) {
                personDao.updateCredits(personId, try { val moshi2 = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(); moshi2.adapter(com.cinetrack.data.remote.dto.CombinedCreditsDto::class.java).toJson(currentCredits) } catch(e:Exception) { "" })
            }
            newTitles
        } catch (e: Exception) {
            personDao.updateCredits(personId, try { val moshi2 = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(); moshi2.adapter(com.cinetrack.data.remote.dto.CombinedCreditsDto::class.java).toJson(currentCredits) } catch(e:Exception) { "" })
            emptyList()
        }
    }

    private fun PersonEntity.toDomainModel() = Person(
        id = id,
        tmdbId = tmdbId,
        name = name,
        profilePath = profilePath,
        biography = biography,
        birthday = birthday,
        deathday = deathday,
        placeOfBirth = placeOfBirth,
        knownForDepartment = knownForDepartment,
        isFollowed = isFollowed
    )
}
