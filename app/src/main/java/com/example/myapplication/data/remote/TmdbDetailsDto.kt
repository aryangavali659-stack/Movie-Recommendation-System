package com.example.myapplication.data.remote

data class VideoResponseDto(
    val results: List<VideoDto>
)

data class VideoDto(
    val id: String,
    val key: String, // YouTube Video Key
    val name: String,
    val site: String,
    val type: String
)

data class CreditsResponseDto(
    val cast: List<CastDto>
)

data class CastDto(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?
)
