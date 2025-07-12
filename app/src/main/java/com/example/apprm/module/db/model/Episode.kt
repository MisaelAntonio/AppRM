package com.example.apprm.module.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Episode(
    val id: Int,
    val name: String,
    @SerialName("air_date") val airDate: String,
    val episode: String,
    val characters: List<String>,
    val url: String,
    val created: String
)
