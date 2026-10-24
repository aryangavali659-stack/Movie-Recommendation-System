package com.example.myapplication.domain.model

data class CastMember(
    val name: String,
    val character: String,
    val profileUrl: String
)

data class MovieDetailInfo(
    val trailerKey: String? = null,
    val cast: List<CastMember> = emptyList()
)
