package com.example.apprm.module.apiService

import com.example.apprm.module.db.model.CharacterResponse
import com.example.apprm.module.db.model.Character
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET(CHARACTER)
    suspend fun getAllCharacters(): Response<CharacterResponse>

    @GET(CHARACTER_BY_ID)
    suspend fun getCharacterById(@Path("id") characterId: Int): Response<Character>

    @GET(CHARACTER)
    suspend fun getCharacterByIds(@Path("id") characterId: List<Int>): Response<Character>

    // Puedes añadir más endpoints según la API de Rick y Morty, por ejemplo, para filtrar:
    @GET("character")
    suspend fun searchCharacters(
        @Query("name") name: String?,
        @Query("status") status: String?,
        @Query("species") species: String?
    ): Response<CharacterResponse>


}