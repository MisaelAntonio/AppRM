package com.example.apprm.module.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apprm.module.db.FavoriteCharacter
import com.example.apprm.module.db.repository.CharacterRepository
import com.example.apprm.module.db.model.Character
import com.example.apprm.module.db.model.Episode
import com.example.apprm.module.db.model.Location
import kotlinx.coroutines.launch

class CharacterDetailViewModel(private val repository: CharacterRepository) : ViewModel() {

    private val _character = MutableLiveData<Character?>()
    val character: LiveData<Character?> = _character

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Nuevo LiveData para el estado de favorito del personaje actual
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> = _episodes

    private val _isLoadingEpisodes = MutableLiveData<Boolean>()
    val isLoadingEpisodes: LiveData<Boolean> = _isLoadingEpisodes

    // Almacena el ID del personaje actual
    private var currentCharacterId: Int? = null

    /**
     * Fetches details for a specific character by ID.
     */
    fun fetchCharacterDetails(characterId: Int) {

        if (currentCharacterId == characterId && _character.value != null) {
            // Ya hemos cargado este personaje, no recargar (opcional, para optimización)
            return
        }
        currentCharacterId = characterId

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            _character.value = null // Limpiar el personaje anterior al iniciar una nueva carga
            _episodes.value = emptyList()
            try {
                // Primero, intenta cargar de la API
                val response = repository.getCharacterById(characterId)
                if (response.isSuccessful) {
                    val loadedCharacter = response.body()
                    _character.value = loadedCharacter
                    // Después de cargar el personaje, verificar si es favorito
                    checkIfFavorite(characterId)
                    // después de cargar el personaje, cargar sus episodios
                    loadedCharacter?.episode?.let {
                        fetchEpisodes(it)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorMessage.value =
                        "Error al cargar el personaje: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de conexión al obtener el personaje: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Verifica si el personaje actual es favorito
    private fun checkIfFavorite(characterId: Int, loadFromDbOnError: Boolean = false) {
        viewModelScope.launch {
            val favorite = repository.getFavoriteCharacterById(characterId)
            _isFavorite.value = (favorite != null)

            // Si hubo un error de red y el personaje es favorito, carga sus datos mínimos de la DB
            if (loadFromDbOnError && favorite != null && _character.value == null) {
                // Construye un objeto Character mínimo a partir del FavoriteCharacter para mostrar algo en la UI
                _character.value = Character(
                    id = favorite.id,
                    name = favorite.name,
                    status = favorite.status,
                    species = favorite.species,
                    type = "", // No tenemos el tipo en FavoriteCharacter
                    gender = "", // No tenemos el género
                    origin = Location(name = "Unknown", url = ""),
                    location = Location(name = "Unknown", url = ""),
                    image = favorite.imageUrl,
                    episode = emptyList(),
                    url = "",
                    created = ""
                )
                _errorMessage.value = "Error de red. Mostrando datos offline (favorito)."
                _episodes.value = emptyList()
            }
        }
    }

    fun toggleFavorite() {
        val character = _character.value // Obtiene el personaje actual de la UI
        if (character == null) {
            _errorMessage.value = "No se puede guardar/eliminar favorito, personaje no cargado."
            return
        }

        viewModelScope.launch {
            if (_isFavorite.value == true) {
                val favoriteChar = FavoriteCharacter(
                    id = character.id,
                    name = character.name,
                    imageUrl = character.image,
                    status = character.status,
                    species = character.species,
                    gender = character.gender,
                    originName = character.origin.name,
                    originUrl= character.origin.url,
                    locationName= character.location.name,
                    locationUrl= character.location.url,
                    created = character.created,
                )
                repository.deleteFavoriteCharacter(favoriteChar)
                _isFavorite.value = false
                _errorMessage.value = "${character.name} eliminado de favoritos."
            } else {
                // No es favorito, marcar (insertar)
                val favoriteChar = FavoriteCharacter(
                    id = character.id,
                    name = character.name,
                    imageUrl = character.image,
                    status = character.status,
                    species = character.species,
                    gender = character.gender,
                    originName = character.origin.name,
                    originUrl= character.origin.url,
                    locationName= character.location.name,
                    locationUrl= character.location.url,
                    created = character.created,
                )
                repository.insertFavoriteCharacter(favoriteChar)
                _isFavorite.value = true
                _errorMessage.value = "${character.name} añadido a favoritos."
            }

        }

    }

    // metodo para cargar episodios
    private fun fetchEpisodes(episodeUrls: List<String>) {
        if (episodeUrls.isEmpty()) {
            _episodes.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoadingEpisodes.value = true
            try {
                val fetchedEpisodes = repository.getEpisodesByUrls(episodeUrls)
                _episodes.value = fetchedEpisodes.sortedBy { it.episode } // Opcional: ordenar por número de episodio
            } catch (e: Exception) {
                // Manejo de errores específico para la carga de episodios
                _errorMessage.value = "Error al cargar episodios: ${e.message}"
                _episodes.value = emptyList()
            } finally {
                _isLoadingEpisodes.value = false
            }
        }
    }

}