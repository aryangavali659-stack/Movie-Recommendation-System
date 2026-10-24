package com.example.myapplication.data.repository

import com.example.myapplication.data.local.MovieEntity
import com.example.myapplication.data.local.WatchlistDao
import com.example.myapplication.data.remote.TmdbApiService
import com.example.myapplication.domain.model.CastMember
import com.example.myapplication.domain.model.Movie
import com.example.myapplication.domain.model.MovieDetailInfo
import com.example.myapplication.utils.ActivityLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MovieRepository(
    private val apiService: TmdbApiService,
    private val watchlistDao: WatchlistDao,
    private val apiKey: String = "695997e1708fa2900f3bcbf100cb846f",
    private val authRepository: AuthRepository = AuthRepository()
) {

    private val genreMap = mapOf(
        28 to "Action", 12 to "Adventure", 16 to "Animation", 35 to "Comedy",
        80 to "Crime", 99 to "Documentary", 18 to "Drama", 10751 to "Family",
        14 to "Fantasy", 36 to "History", 27 to "Horror", 10402 to "Music",
        9648 to "Mystery", 10749 to "Romance", 878 to "Sci-Fi", 10770 to "TV Movie",
        53 to "Thriller", 10752 to "War", 37 to "Western"
    )

    fun getCurrentUserId(): String {
        return authRepository.getCurrentEmail() ?: ""
    }

    fun getWatchlist(userId: String): Flow<List<Movie>> {
        return watchlistDao.getUserWatchlist(userId).map { entities ->
            entities.map { entity ->
                Movie(
                    id = entity.id,
                    title = entity.title,
                    overview = entity.overview,
                    posterPath = entity.posterPath,
                    rating = entity.rating,
                    releaseDate = entity.releaseDate,
                    genres = entity.genresCsv.split(",").filter { it.isNotBlank() }
                )
            }
        }
    }

    suspend fun getMoviesByIndustry(language: String, page: Int = 1): List<Movie> = withContext(Dispatchers.IO) {
        val categoryKey = when (language) {
            "hi" -> "bollywood"
            "te" -> "tollywood"
            else -> "hollywood"
        }

        try {
            val response = apiService.discoverMovies(
                apiKey = apiKey,
                language = language,
                page = page
            )
            val domainMovies = response.results.map { dto ->
                val realGenres = dto.genre_ids?.mapNotNull { genreMap[it] } ?: listOf("Drama")
                Movie(
                    id = dto.id,
                    title = dto.title,
                    overview = dto.overview,
                    posterPath = if (!dto.poster_path.isNullOrEmpty()) {
                        "https://image.tmdb.org/t/p/w500${dto.poster_path}"
                    } else "",
                    rating = dto.vote_average,
                    releaseDate = dto.release_date ?: "N/A",
                    genres = realGenres.ifEmpty { listOf("Drama") }
                )
            }

            if (domainMovies.isNotEmpty()) {
                val activeUserId = getCurrentUserId()
                val entities = domainMovies.map { movie ->
                    MovieEntity(
                        id = movie.id,
                        userId = activeUserId,
                        title = movie.title,
                        overview = movie.overview,
                        posterPath = movie.posterPath,
                        rating = movie.rating,
                        releaseDate = movie.releaseDate,
                        category = categoryKey,
                        genresCsv = movie.genres.joinToString(",")
                    )
                }
                watchlistDao.cacheMovies(entities)
                domainMovies
            } else {
                loadCachedMovies(categoryKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadCachedMovies(categoryKey)
        }
    }

    private suspend fun loadCachedMovies(category: String): List<Movie> {
        val cached = watchlistDao.getCachedMoviesByCategory(category)
        return if (cached.isNotEmpty()) {
            cached.map { entity ->
                Movie(
                    id = entity.id,
                    title = entity.title,
                    overview = entity.overview,
                    posterPath = entity.posterPath,
                    rating = entity.rating,
                    releaseDate = entity.releaseDate,
                    genres = entity.genresCsv.split(",").filter { it.isNotBlank() }
                )
            }
        } else {
            getFallbackMovies(category)
        }
    }

    private fun getFallbackMovies(category: String): List<Movie> {
        return when (category) {
            "bollywood" -> listOf(
                Movie(101, "RRR", "Revolutionaries fight for freedom.", "https://image.tmdb.org/t/p/w500/nEu33A89PTh3o262iBvO1IipP8.jpg", 8.8, "2022-03-24", listOf("Action", "Drama")),
                Movie(102, "3 Idiots", "Two friends search for their companion.", "https://image.tmdb.org/t/p/w500/66A9To3VDVChGDzC2y6a8P2mP4M.jpg", 8.4, "2009-12-25", listOf("Comedy", "Drama"))
            )
            "tollywood" -> listOf(
                Movie(103, "K.G.F: Chapter 2", "Rocky rules the Kolar Gold Fields.", "https://image.tmdb.org/t/p/w500/kh31iA324a3mB41yWd122wH4z2y.jpg", 8.3, "2022-04-14", listOf("Action", "Crime")),
                Movie(104, "Baahubali 2", "Mahendra discovers his heritage.", "https://image.tmdb.org/t/p/w500/29A91K44C3c5iN42v3gD11m122E.jpg", 8.2, "2017-04-27", listOf("Action", "Fantasy"))
            )
            else -> listOf(
                Movie(1, "Inception", "A thief enters dreams to steal secrets.", "https://image.tmdb.org/t/p/w500/oYuLE1311o2RScI1M2CviNSB1L.jpg", 8.8, "2010-07-16", listOf("Sci-Fi", "Action")),
                Movie(2, "Interstellar", "Explorers travel through a wormhole.", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", 8.6, "2014-11-07", listOf("Sci-Fi", "Drama"))
            )
        }
    }

    suspend fun searchMovies(query: String): List<Movie> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val response = apiService.searchMovies(apiKey = apiKey, query = query)
            response.results.map { dto ->
                val realGenres = dto.genre_ids?.mapNotNull { genreMap[it] } ?: listOf("Drama")
                Movie(
                    id = dto.id,
                    title = dto.title,
                    overview = dto.overview,
                    posterPath = if (!dto.poster_path.isNullOrEmpty()) {
                        "https://image.tmdb.org/t/p/w500${dto.poster_path}"
                    } else "",
                    rating = dto.vote_average,
                    releaseDate = dto.release_date ?: "N/A",
                    genres = realGenres.ifEmpty { listOf("Drama") }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMovieDetails(movieId: Int): MovieDetailInfo = withContext(Dispatchers.IO) {
        try {
            val videos = apiService.getMovieVideos(movieId, apiKey)
            val credits = apiService.getMovieCredits(movieId, apiKey)

            val youtubeTrailer = videos.results.firstOrNull { 
                it.site.equals("YouTube", ignoreCase = true) && it.type.equals("Trailer", ignoreCase = true) 
            }?.key ?: videos.results.firstOrNull { it.site.equals("YouTube", ignoreCase = true) }?.key

            val castList = credits.cast.take(10).map { cast ->
                CastMember(
                    name = cast.name,
                    character = cast.character ?: "Actor",
                    profileUrl = if (!cast.profile_path.isNullOrEmpty()) "https://image.tmdb.org/t/p/w200${cast.profile_path}" else ""
                )
            }

            MovieDetailInfo(trailerKey = youtubeTrailer, cast = castList)
        } catch (e: Exception) {
            e.printStackTrace()
            MovieDetailInfo()
        }
    }

    suspend fun isBookmarked(movieId: Int): Boolean {
        val activeUserId = getCurrentUserId()
        return if (activeUserId.isNotBlank()) {
            watchlistDao.isBookmarked(movieId, activeUserId)
        } else {
            false
        }
    }

    suspend fun getPopularMovies(): List<Movie> = getMoviesByIndustry("en")

    suspend fun toggleWatchlist(movie: Movie) = withContext(Dispatchers.IO) {
        val activeUserId = getCurrentUserId()

        if (activeUserId.isNotBlank()) {
            val currentlyBookmarked = watchlistDao.isBookmarked(movie.id, activeUserId)

            if (currentlyBookmarked) {
                watchlistDao.removeFromWatchlist(activeUserId, movie.id)
                ActivityLogger.logUserAction(
                    userId = activeUserId,
                    activityType = "REMOVED_FROM_WATCHLIST",
                    movieTitle = movie.title,
                    email = activeUserId
                )
            } else {
                val entity = MovieEntity(
                    id = movie.id,
                    userId = activeUserId,
                    title = movie.title,
                    overview = movie.overview,
                    posterPath = movie.posterPath,
                    rating = movie.rating,
                    releaseDate = movie.releaseDate,
                    category = "watchlist",
                    genresCsv = movie.genres.joinToString(",")
                )
                watchlistDao.addToWatchlist(entity)
                ActivityLogger.logUserAction(
                    userId = activeUserId,
                    activityType = "ADDED_TO_WATCHLIST",
                    movieTitle = movie.title,
                    email = activeUserId
                )
            }
        }
    }
}
