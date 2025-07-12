package com.example.apprm.module.db.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetailedLocation(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>, // Lista de URLs de personajes residentes
    val url: String,
    val created: String
)