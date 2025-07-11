package com.example.apprm.module.db.repository



import com.example.apprm.module.apiService.ApiService
import com.example.apprm.module.db.model.Character
import com.example.apprm.module.db.model.CharacterResponse
import retrofit2.Response

class CharacterRepository(private val apiService: ApiService) {

    suspend fun getAllCharacters(): Response<CharacterResponse> {
        return apiService.getAllCharacters()
    }

    suspend fun getCharacterById(characterId: Int): Response<Character> {
        return apiService.getCharacterById(characterId)
    }

    suspend fun getCharactersByIds(characterIds: List<Int>): Response<Character> {
        return apiService.getCharacterByIds(characterIds)
    }


    // método para la búsqueda y filtrado
    suspend fun searchCharacters(
        name: String?,
        status: String?,
        species: String?
    ): Response<CharacterResponse> {
        // Llama al endpoint de búsqueda/filtrado de la API
        return apiService.searchCharacters(name, status, species)
    }
//    suspend fun getCharactersByPage(page: Int): Response<CharacterResponse> {
//        return apiService.getCharactersByPage(page)
//    }
}