package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateRecommendationUseCaseTest {

    private val useCase = CalculateRecommendationUseCase()

    @Test
    fun execute_ranksMoviesByGenreSimilarity() {
        val targetMovie = Movie(
            id = 1,
            title = "Inception",
            overview = "Sci-fi thriller",
            posterPath = "/path1.jpg",
            rating = 8.8,
            releaseDate = "2010-07-16",
            genres = listOf("Action", "Sci-Fi", "Thriller")
        )

        val movieSciFi = Movie(
            id = 2,
            title = "Interstellar",
            overview = "Space exploration",
            posterPath = "/path2.jpg",
            rating = 8.6,
            releaseDate = "2014-11-07",
            genres = listOf("Action", "Sci-Fi", "Drama")
        )

        val movieRomance = Movie(
            id = 3,
            title = "The Notebook",
            overview = "Romantic drama",
            posterPath = "/path3.jpg",
            rating = 7.8,
            releaseDate = "2004-06-25",
            genres = listOf("Romance", "Drama")
        )

        val candidateMovies = listOf(movieRomance, movieSciFi)

        val recommended = useCase.execute(listOf(targetMovie), candidateMovies)

        // Interstellar shares Action and Sci-Fi with targetMovie, so it should rank first.
        assertEquals(2, recommended.size)
        assertEquals("Interstellar", recommended[0].title)
        assertEquals("The Notebook", recommended[1].title)
    }
}
