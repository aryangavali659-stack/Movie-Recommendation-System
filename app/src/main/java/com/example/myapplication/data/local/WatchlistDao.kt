package com.example.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE category = 'watchlist' AND userId = :userId AND id = :movieId")
    suspend fun removeFromWatchlist(userId: String, movieId: Int)

    @Query("SELECT * FROM movies WHERE category = 'watchlist' AND userId = :userId ORDER BY id DESC")
    fun getUserWatchlist(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE category = 'watchlist' AND id = :movieId AND userId = :userId)")
    suspend fun isBookmarked(movieId: Int, userId: String): Boolean

    // Offline caching
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun cacheMovies(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies WHERE category = :category AND (:userId = '' OR userId = :userId)")
    suspend fun getCachedMoviesByCategory(category: String, userId: String = ""): List<MovieEntity>
}
