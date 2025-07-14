package com.example.apprm.module.db.repository


import com.example.apprm.module.apiService.ApiService
import com.example.apprm.module.db.FavoriteCharacter
import com.example.apprm.module.db.dao.FavoriteCharacterDao
import com.example.apprm.module.db.model.Character
import com.example.apprm.module.db.model.CharacterResponse
import com.example.apprm.module.db.model.DetailedLocation
import com.example.apprm.module.db.model.Episode
import retrofit2.Response
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope // Importar coroutineScope para lanzamientos paralelos
import kotlinx.coroutines.flow.Flow

class CharacterRepository(
    private val apiService: ApiService,
    private val favoriteCharacterDao: FavoriteCharacterDao // Añadir el DAO al constructor
) {

    // *** CAMBIO AQUÍ: Ahora devuelve Response<CharacterResponse> ***
    suspend fun getCharacters(
        page: Int,
        name: String? = null,
        status: String? = null,
        species: String? = null
    ): Response<CharacterResponse> { // <--- Return type changed
        return apiService.getCharacters(page, name, status, species)
    }

    // método searchCharacters separado, también debería devolver CharacterResponse
    /*
    suspend fun searchCharacters(
        name: String,
        page: Int,
        status: String? = null,
        species: String? = null
    ): Response<CharacterResponse> { // <--- Return type changed
        return apiService.searchCharacters(name, page, status, species)
    }
    */

    suspend fun getCharacterById(characterId: Int): Response<Character> {
        return apiService.getCharacterById(characterId)
    }

    // MÉTODO PARA CONSUMIR LA URL COMPLETA DE LA SIGUIENTE PÁGINA
    suspend fun getCharactersByFullUrl(url: String): Response<CharacterResponse> {
        return apiService.getCharactersByUrl(url)
    }

    // --- Métodos para la base de datos Room ---

    suspend fun insertFavoriteCharacter(character: FavoriteCharacter) {
        favoriteCharacterDao.insertFavorite(character)
    }

    suspend fun deleteFavoriteCharacter(character: FavoriteCharacter) {
        favoriteCharacterDao.deleteFavorite(character)
    }

    suspend fun getFavoriteCharacterById(characterId: Int): FavoriteCharacter? {
        return favoriteCharacterDao.getFavoriteById(characterId)
    }

    fun getAllFavoriteCharacters(): Flow<List<FavoriteCharacter>> {
        return favoriteCharacterDao.getAllFavorites()
    }

    //  OBTENER  LISTA DE EPISODIOS POR SUS URLs
    suspend fun getEpisodesByUrls(episodeUrls: List<String>): List<Episode> = coroutineScope {
        val deferredEpisodes = episodeUrls.map { url ->
            async { // async permite ejecutar estas llamadas en paralelo
                try {
                    val response = apiService.getEpisodeByUrl(url)
                    if (response.isSuccessful) {
                        response.body() // Devuelve el objeto Episode si es exitoso
                    } else {
                        // Manejo básico de errores para episodios individuales
                        null // O puedes lanzar una excepción o loggear el error
                    }
                } catch (e: Exception) {
                    // Manejo de errores de red para episodios individuales
                    null // O puedes lanzar una excepción o loggear el error
                }
            }
        }
        deferredEpisodes.awaitAll().filterNotNull() // Espera a que todas las llamadas terminen y filtra los nulos
    }

    // MÉTODO PARA OBTENER LA UBICACIÓN DETALLADA ***
    suspend fun getDetailedLocationByUrl(locationUrl: String): Response<DetailedLocation> {
        return apiService.getDetailedLocationByUrl(locationUrl)
    }


}