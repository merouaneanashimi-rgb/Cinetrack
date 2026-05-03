package com.cinetrack.data.repository

import com.cinetrack.data.local.dao.*
import com.cinetrack.data.local.entity.*
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.data.remote.dto.*
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.MovieRepository
import com.cinetrack.util.Result
import com.cinetrack.util.safeApiCall
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val api: TmdbApiService
) : MovieRepository {

    override fun getMovieById(id: Long): Flow<Movie?> {
        return movieDao.getById(id).map { it?.toDomainModel() }
    }

    override suspend fun getMovieByTmdbId(tmdbId: Int): Movie? {
        return movieDao.getByTmdbId(tmdbId)?.toDomainModel()
    }

    override fun getMoviesByListType(listType: UserListType): Flow<List<Movie>> {
        return movieDao.getByListType(com.cinetrack.data.local.entity.UserListType.valueOf(listType.name))
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavorites().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getWatchedMovies(): Flow<List<Movie>> {
        return movieDao.getWatched().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAllTrackedMovies(): Flow<List<Movie>> {
        return movieDao.getAllTracked().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getTrackedMovieCount(): Flow<Int> {
        return movieDao.getTrackedCount()
    }

    override suspend fun addMovie(movie: Movie, listType: UserListType): Long {
        val entity = movie.toEntity().copy(
            userListType = com.cinetrack.data.local.entity.UserListType.valueOf(listType.name),
            addedAt = System.currentTimeMillis()
        )
        return movieDao.insert(entity)
    }

    override suspend fun updateMovieListType(movieId: Long, listType: UserListType?) {
        movieDao.updateListType(movieId, listType?.let { com.cinetrack.data.local.entity.UserListType.valueOf(it.name) })
    }

    override suspend fun updateMovieFavorite(movieId: Long, isFavorite: Boolean) {
        movieDao.updateFavorite(movieId, isFavorite)
    }

    override suspend fun updateMovieWatched(movieId: Long, watched: Boolean) {
        movieDao.updateWatched(movieId, watched, if (watched) System.currentTimeMillis() else null)
    }

    override suspend fun updateMovieRating(movieId: Long, rating: Double?) {
        movieDao.updateRating(movieId, rating)
    }

    override suspend fun updateMovieNotes(movieId: Long, notes: String?) {
        movieDao.updateNotes(movieId, notes)
    }

    override suspend fun removeMovie(movieId: Long) {
        movieDao.deleteById(movieId)
    }

    override suspend fun syncMovieFromApi(tmdbId: Int): Result<Movie> {
        return safeApiCall {
            val response = api.getMovieDetail(tmdbId)
            if (response.isSuccessful) {
                val detail = response.body() ?: throw Exception("Empty response")
                val existing = movieDao.getByTmdbId(tmdbId)
                val entity = detail.toMovieEntity(existing)
                val movieId = movieDao.insert(entity)
                entity.copy(id = movieId).toDomainModel()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun getCollectionMovies(collectionId: Int, excludeId: Long): List<Movie> {
        return movieDao.getByCollection(collectionId, excludeId).map { it.toDomainModel() }
    }

    override suspend fun getWatchedMovieCount(): Int {
        return movieDao.getWatchedCount()
    }

    override suspend fun isMovieTracked(tmdbId: Int): Boolean {
        return movieDao.isTracked(tmdbId)
    }

    // Mappers
    private fun MovieEntity.toDomainModel() = Movie(
        id = id,
        tmdbId = tmdbId,
        traktId = traktId,
        title = title,
        originalTitle = originalTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genresJson?.split(",") ?: emptyList(),
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        overview = overview,
        status = status,
        budget = budget,
        revenue = revenue,
        productionCompanies = productionCompaniesJson?.split(",") ?: emptyList(),
        userListType = userListType?.let { com.cinetrack.domain.model.UserListType.valueOf(it.name) },
        addedAt = addedAt,
        isWatched = isWatched,
        watchedAt = watchedAt,
        userRating = userRating,
        userNotes = userNotes,
        isFavorite = isFavorite,
        lastSyncedAt = lastSyncedAt,
        collectionId = collectionId,
        collectionName = collectionName
    )

    private fun Movie.toEntity() = MovieEntity(
        id = id,
        tmdbId = tmdbId,
        traktId = traktId,
        title = title,
        originalTitle = originalTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        runtime = runtime,
        genresJson = genres.joinToString(","),
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        overview = overview,
        status = status,
        budget = budget,
        revenue = revenue,
        productionCompaniesJson = productionCompanies.joinToString(","),
        userListType = userListType?.let { com.cinetrack.data.local.entity.UserListType.valueOf(it.name) },
        addedAt = addedAt,
        isWatched = isWatched,
        watchedAt = watchedAt,
        userRating = userRating,
        userNotes = userNotes,
        isFavorite = isFavorite,
        lastSyncedAt = lastSyncedAt,
        collectionId = collectionId,
        collectionName = collectionName
    )

    private fun MovieDetailDto.toMovieEntity(existing: MovieEntity?): MovieEntity = MovieEntity(
        id = existing?.id ?: 0,
        tmdbId = id,
        title = title,
        originalTitle = originalTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        runtime = runtime,
        genresJson = genres?.joinToString(",") { it.name },
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        overview = overview,
        status = status,
        budget = budget,
        revenue = revenue,
        productionCompaniesJson = productionCompanies?.joinToString(",") { it.name },
        userListType = existing?.userListType,
        addedAt = existing?.addedAt ?: System.currentTimeMillis(),
        isWatched = existing?.isWatched ?: false,
        watchedAt = existing?.watchedAt,
        userRating = existing?.userRating,
        userNotes = existing?.userNotes,
        isFavorite = existing?.isFavorite ?: false,
        lastSyncedAt = System.currentTimeMillis(),
        collectionId = belongsToCollection?.id,
        collectionName = belongsToCollection?.name
    )
}
