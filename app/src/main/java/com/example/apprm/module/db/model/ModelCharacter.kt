package com.example.apprm.module.db.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterResponse(
    val info: Info,
    val results: List<Character>
)

@Serializable
data class Info(
    val count: Int,
    val pages: Int,
    val next: String?, // Puede ser nulo si no hay más páginas
    val prev: String?  // Puede ser nulo si es la primera página
)

@Serializable
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: Location, // Reutilizamos Location para Origin
    val location: Location,
    val image: String, // URL de la imagen
    val episode: List<String>,
    val url: String,
    val created: String
)

@Serializable
data class Location(
    val name: String,
    val url: String
) : java.io.Serializable