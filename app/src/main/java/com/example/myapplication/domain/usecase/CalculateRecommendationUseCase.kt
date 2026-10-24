package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Movie
import com.example.myapplication.domain.model.UserProfile
import kotlin.math.sqrt

class CalculateRecommendationUseCase {

    fun execute(
        watchlist: List<Movie>,
        candidateMovies: List<Movie>,
        userProfile: UserProfile = UserProfile()
    ): List<Movie> {
        if (candidateMovies.isEmpty()) return emptyList()

        val watchlistIds = watchlist.map { it.id }.toSet()
        val availableCandidates = candidateMovies
            .filter { it.id !in watchlistIds }
            .filter { it.rating >= userProfile.minRatingFilter }

        val userGenreMap = mutableMapOf<String, Double>()

        // Add explicit favorite genres with extra weight
        userProfile.favoriteGenres.forEach { genre ->
            userGenreMap[genre] = (userGenreMap[genre] ?: 0.0) + 3.0
        }

        // Add implicit watchlist genres
        watchlist.forEach { movie ->
            movie.genres.forEach { genre ->
                userGenreMap[genre] = (userGenreMap[genre] ?: 0.0) + 1.0
            }
        }

        if (userGenreMap.isEmpty()) {
            return availableCandidates.sortedByDescending { it.rating }.take(10)
        }

        val rankedCandidates = availableCandidates
            .map { candidate ->
                val similarityScore = calculateCosineSimilarity(userGenreMap, candidate.genres)
                candidate to similarityScore
            }
            .sortedByDescending { it.second }
            .map { it.first }

        return rankedCandidates.ifEmpty { availableCandidates.sortedByDescending { it.rating }.take(10) }
    }

    private fun calculateCosineSimilarity(
        userVector: Map<String, Double>,
        candidateGenres: List<String>
    ): Double {
        if (userVector.isEmpty() || candidateGenres.isEmpty()) return 0.0

        var dotProduct = 0.0
        candidateGenres.forEach { genre ->
            dotProduct += userVector[genre] ?: 0.0
        }

        var userMagnitudeSq = 0.0
        userVector.values.forEach { count ->
            userMagnitudeSq += count * count
        }

        val candidateMagnitudeSq = candidateGenres.size.toDouble()

        val denominator = sqrt(userMagnitudeSq) * sqrt(candidateMagnitudeSq)
        return if (denominator == 0.0) 0.0 else dotProduct / denominator
    }
}
