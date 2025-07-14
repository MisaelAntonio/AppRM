package com.example.apprm.module.apiService

import com.example.apprm.module.db.model.CharacterResponse
import com.example.apprm.module.db.model.Character
import com.example.apprm.module.db.model.DetailedLocation
import com.example.apprm.module.db.model.Episode
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    @GET(CHARACTER)
    suspend fun getCharacters(
        @Query("page") page: Int,
        @Query("name") name: String? = null, // Añadido
        @Query("status") status: String? = null, // Añadido
        @Query("species") species: String? = null // Añadido
    ): Response<CharacterResponse>

    // searchCharacters puede ser consolidado o mantener su propia lógica si hay diferencias.
    // Por ahora, lo mantenemos separado pero con los mismos filtros.
    @GET(CHARACTER)
    suspend fun searchCharacters(
        @Query("name") name: String?, // Este ya estaba
        @Query("status") status: String? , // Añadido
        @Query("species") species: String? // Añadido
    ): Response<CharacterResponse>

    // CONSUMIR URL COMPLETA
    @GET
    suspend fun getCharactersByUrl(@Url url: String): Response<CharacterResponse>

    @GET(CHARACTER_BY_ID)
    suspend fun getCharacterById(@Path("id") characterId: Int): Response<Character>

    // *** OBTENER UN EPISODIO POR SU URL COMPLETA
    @GET
    suspend fun getEpisodeByUrl(@Url episodeUrl: String): Response<Episode>

    // OBTENER UNA UBICACIÓN DETALLADA POR SU URL
    @GET
    suspend fun getDetailedLocationByUrl(@Url locationUrl: String): Response<DetailedLocation>

}